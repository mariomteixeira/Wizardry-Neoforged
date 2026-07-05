package com.koomplo.wizardry.content.spell.necromancy;

import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;

public class ShadowWard extends Spell {

    public static final SpellProperty<Float> REFLECTED_FRACTION = SpellProperty.floatProperty("reflected_fraction");

    @Override
    public boolean cast(PlayerCastContext ctx) {
        var caster = ctx.caster();

        if (ctx.world().isClientSide) {
            double dx = -1 + 2 * ctx.world().random.nextFloat();
            double dy = -1 + ctx.world().random.nextFloat();
            double dz = -1 + 2 * ctx.world().random.nextFloat();
            ctx.world().addParticle(ParticleTypes.PORTAL, caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ(), dx, dy, dz);
        }

        if (ctx.castingTicks() % 50 == 0) {
            this.playSound(ctx.world(), caster, ctx.castingTicks(), -1);
        }

        return true;
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    @Override
    protected void playSound(Level world, LivingEntity entity, int castTicks, int duration) {
        this.playSoundLoop(world, entity, castTicks);
    }

    @Override
    protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration) {
        this.playSoundLoop(world, x, y, z, ticksInUse, duration);
    }

    /** Reflects part of any incoming damage back at the attacker while the ward is held (1.12.2 event handler). */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        LivingEntity victim = event.getEntity();
        if (!EntityUtil.isCasting(victim, Spells.SHADOW_WARD)) return;

        // Reflected damage is never reflected again (1.12.2 IElementalDamage#isRetaliatory)
        if (event.getSource() instanceof MagicDamageSource magicSource && magicSource.isRetaliatory()) return;
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        event.setCanceled(true);

        float reflectedFraction = Mth.clamp(Spells.SHADOW_WARD.property(REFLECTED_FRACTION), 0, 1);

        victim.hurt(victim.damageSources().magic(), event.getAmount() * (1 - reflectedFraction));
        attacker.hurt(MagicDamageSource.causeDirectMagicDamage(victim, EBDamageSources.MAGIC, true),
                event.getAmount() * reflectedFraction);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.NECROMANCY, SpellType.DEFENCE, SpellAction.NONE, 10, 0, 0)
                .add(REFLECTED_FRACTION, 0.5f)
                .build();
    }
}
