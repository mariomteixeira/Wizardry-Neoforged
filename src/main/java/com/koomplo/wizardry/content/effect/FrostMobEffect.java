package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class FrostMobEffect extends MagicMobEffect {
    public FrostMobEffect() {
        super(MobEffectCategory.HARMFUL, 0);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("frost_movement"), -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int i) {
        if (livingEntity.isOnFire()) {
            if (livingEntity.hasEffect(holder())) {
                livingEntity.removeEffect(holder());
                livingEntity.clearFire();
            }
        }

        livingEntity.setIsInPowderSnow(true);

        // need to check the -1, being the infinite duration case
        if (livingEntity.hasEffect(holder()) && livingEntity.getEffect(holder()).getDuration() <= 1 && livingEntity.getEffect(holder()).getDuration() != -1) {
            livingEntity.setIsInPowderSnow(false);
        }
        return super.applyEffectTick(livingEntity, i);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return true;
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {
        ParticleBuilder.create(EBParticles.SNOW).pos(x, y, z).time(15 + world.random.nextInt(5)).spawn(world);
    }
}
