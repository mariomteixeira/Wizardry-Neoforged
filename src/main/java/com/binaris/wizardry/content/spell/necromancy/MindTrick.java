package com.binaris.wizardry.content.spell.necromancy;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.event.EBLivingHurtEvent;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBMobEffects;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MindTrick extends RaySpell {

    public MindTrick() {
        this.soundValues(0.7f, 1, 0.4f);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return false;

        if (!ctx.world().isClientSide) {
            int duration = (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION));

            if (target instanceof Player) {
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
            } else if (target instanceof Mob mob) {
                mob.setTarget(null);
                mob.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.MIND_TRICK), duration, 0));
            }
        } else {
            for (int i = 0; i < 10; i++) {
                ParticleBuilder.create(EBParticles.DARK_MAGIC, ctx.world().random, target.getX(),
                                target.getY() + target.getEyeHeight(), target.getZ(), 0.25, false)
                        .color(0.8f, 0.2f, 1.0f).spawn(ctx.world());
            }
        }
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    // Being attacked by a living entity dispels the trick (potions/drowning/cacti don't affect targeting)
    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (event.getSource().getEntity() instanceof LivingEntity
                && event.getDamagedEntity().hasEffect(EBMobEffects.holder(EBMobEffects.MIND_TRICK))
                && !event.getDamagedEntity().level().isClientSide) {
            event.getDamagedEntity().removeEffect(EBMobEffects.holder(EBMobEffects.MIND_TRICK));
        }
    }

    // Tricked (or feared, see Intimidate) mobs cannot acquire attack targets
    public static void onLivingChangeTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() != null
                && event.getEntity() instanceof Mob
                && (event.getEntity().hasEffect(EBMobEffects.holder(EBMobEffects.MIND_TRICK))
                    || event.getEntity().hasEffect(EBMobEffects.holder(EBMobEffects.FEAR)))) {
            event.setCanceled(true);
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.NOVICE, Elements.NECROMANCY, SpellType.ATTACK, SpellAction.POINT, 10, 0, 40)
                .add(DefaultProperties.RANGE, 8f)
                .add(DefaultProperties.EFFECT_DURATION, 300)
                .build();
    }
}
