package com.koomplo.wizardry.content.spell.lightning;

import com.koomplo.wizardry.api.content.event.EBLivingHurtEvent;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import org.jetbrains.annotations.NotNull;

public class LightningBoltSpell extends RaySpell {

    /** The NBT key used to store the UUID of the entity that summoned the lightning bolt. */
    public static final String SUMMONER_NBT_KEY = "summoner";
    /** The NBT key used to store the damage modifier for the lightning bolt. */
    public static final String DAMAGE_MODIFIER_NBT_KEY = "damageModifier";
    /** Marks an entity already damaged by the spell so vanilla's follow-up lightning damage is cancelled. */
    public static final String IMMUNE_TO_LIGHTNING_NBT_KEY = "immuneToLightning";

    public LightningBoltSpell() {
        this.ignoreLivingEntities(true);
    }

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        var pos = blockHit.getBlockPos();

        if (ctx.world().canSeeSky(pos.above())) {
            if (!ctx.world().isClientSide) {
                // 1.12.2: desliga doFireTick durante o strike quando playerBlockDamage está off
                var fireTickRule = ctx.world().getGameRules().getRule(GameRules.RULE_DOFIRETICK);
                boolean suppressFire = fireTickRule.get() && !EBServerConfig.PLAYER_BLOCK_DAMAGE.get();
                if (suppressFire) fireTickRule.set(false, ctx.world().getServer());

                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(ctx.world());
                if (lightning != null) {
                    lightning.moveTo(Vec3.atBottomCenterOf(pos));
                    if (ctx.caster() != null) lightning.getPersistentData().putUUID(SUMMONER_NBT_KEY, ctx.caster().getUUID());
                    lightning.getPersistentData().putFloat(DAMAGE_MODIFIER_NBT_KEY, ctx.modifiers().get(SpellModifiers.POTENCY));
                    ctx.world().addFreshEntity(lightning);
                }

                if (suppressFire) fireTickRule.set(true, ctx.world().getServer());
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    // Replaces vanilla lightning damage with the spell's shock damage attributed to the summoner,
    // keeping the other vanilla strike effects (fire, creeper charging, pig conversion).
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        float damageModifier = event.getLightning().getPersistentData().getFloat(DAMAGE_MODIFIER_NBT_KEY);

        Entity summoner = null;
        if (event.getLightning().getPersistentData().hasUUID(SUMMONER_NBT_KEY)) {
            summoner = EntityUtil.getEntityByUUID(event.getLightning().level(),
                    event.getLightning().getPersistentData().getUUID(SUMMONER_NBT_KEY));
            if (!(summoner instanceof LivingEntity)) summoner = null;
        }

        if (damageModifier > 0 || summoner != null) {
            DamageSource source = summoner == null ? event.getEntity().damageSources().lightningBolt()
                    : MagicDamageSource.causeIndirectMagicDamage(event.getLightning(), summoner, EBDamageSources.SHOCK);
            float damage = Spells.LIGHTNING_BOLT.property(DefaultProperties.DAMAGE) * damageModifier;

            EntityUtil.attackEntityWithoutKnockback(event.getEntity(), source, damage);
            event.getEntity().getPersistentData().putBoolean(IMMUNE_TO_LIGHTNING_NBT_KEY, true);
        }
    }

    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.getDamagedEntity().getPersistentData().contains(IMMUNE_TO_LIGHTNING_NBT_KEY)
                && event.getSource().is(DamageTypes.LIGHTNING_BOLT)) {
            event.setCanceled(true);
            event.getDamagedEntity().getPersistentData().remove(IMMUNE_TO_LIGHTNING_NBT_KEY);
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.LIGHTNING, SpellType.ATTACK, SpellAction.POINT, 40, 10, 80)
                .add(DefaultProperties.RANGE, 40f)
                .add(DefaultProperties.DAMAGE, 5f)
                .build();
    }
}
