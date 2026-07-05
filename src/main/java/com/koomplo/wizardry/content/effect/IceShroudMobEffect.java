package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.api.content.event.EBLivingHurtEvent;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Spells;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class IceShroudMobEffect extends MagicMobEffect {
    public IceShroudMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof LivingEntity attacker
                && event.getDamagedEntity().hasEffect(EBMobEffects.holder(EBMobEffects.ICE_SHROUD))
                && !MagicDamageSource.isEntityImmune(EBDamageSources.FROST, attacker)
                && !event.getDamagedEntity().level().isClientSide) {
            attacker.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.FROST),
                    Spells.ICE_SHROUD.property(DefaultProperties.EFFECT_DURATION),
                    Spells.ICE_SHROUD.property(DefaultProperties.EFFECT_STRENGTH)));
        }
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {
        float brightness = 0.5f + (world.random.nextFloat() / 2);
        ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).color(brightness, brightness + 0.1f, 1.0f).gravity(true).spawn(world);
        ParticleBuilder.create(EBParticles.SNOW).pos(x, y, z).spawn(world);
    }
}
