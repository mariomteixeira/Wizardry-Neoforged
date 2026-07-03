package com.binaris.wizardry.api.content.effect;

import com.binaris.wizardry.api.content.event.EBLivingTick;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public abstract class MagicMobEffect extends MobEffect implements CustomMobEffectParticles {
    public MagicMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * Returns the {@link Holder} that backs this effect in the effect registry. 1.21 effect APIs
     * ({@link net.minecraft.world.entity.LivingEntity#hasEffect}/{@code getEffect}/{@code removeEffect}) take a
     * {@code Holder<MobEffect>} rather than the raw effect.
     */
    protected Holder<MobEffect> holder() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
    }

    public static void onLivingTick(EBLivingTick event) {
        if (!event.getLevel().isClientSide) return;
        for (MobEffectInstance effect : event.getEntity().getActiveEffects()) {
            if (effect.getEffect().value() instanceof CustomMobEffectParticles particles) {
                double x = event.getEntity().getX()
                        + (event.getLevel().random.nextDouble() - 0.5) * event.getEntity().getBbWidth();
                double y = event.getEntity().getY()
                        + event.getLevel().random.nextDouble() * event.getEntity().getBbHeight();
                double z = event.getEntity().getZ()
                        + (event.getLevel().random.nextDouble() - 0.5) * event.getEntity().getBbWidth();

                particles.spawnCustomParticle(event.getLevel(), x, y, z);
            }
        }
    }
}
