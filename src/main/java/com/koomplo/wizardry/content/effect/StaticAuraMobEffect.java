package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.api.content.event.EBLivingHurtEvent;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.EBSounds;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.Level;

public class StaticAuraMobEffect extends MagicMobEffect {
    public StaticAuraMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;
        DamageSource source = event.getSource();

        if (source.getEntity() != null && event.getDamagedEntity().hasEffect(EBMobEffects.holder(EBMobEffects.STATIC_AURA))) {
            source.getEntity().hurt(MagicDamageSource.causeDirectMagicDamage(event.getDamagedEntity(), EBDamageSources.SHOCK),
                    event.getAmount() / 2);
            source.getEntity().playSound(EBSounds.SPELL_STATIC_AURA_RETALIATE.get(), 1.0F,
                    event.getDamagedEntity().level().random.nextFloat() * 0.4F + 1.5F);
        }
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {
        ParticleBuilder.create(EBParticles.SPARK).pos(x, y, z).spawn(world);
    }
}
