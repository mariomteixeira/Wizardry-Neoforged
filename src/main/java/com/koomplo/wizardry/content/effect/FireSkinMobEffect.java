package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.api.content.event.EBLivingHurtEvent;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FireSkinMobEffect extends MagicMobEffect {
    public FireSkinMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
    }

    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;

        if (event.getDamagedEntity().hasEffect(EBMobEffects.holder(EBMobEffects.FIRESKIN)) &&
                !MagicDamageSource.isEntityImmune(EBDamageSources.FIRE, event.getDamagedEntity())) {
            attacker.igniteForSeconds(Spells.FIRE_BREATH.property(DefaultProperties.EFFECT_DURATION) * 20);
        }
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {
        world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int i) {
        livingEntity.clearFire();
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }
}
