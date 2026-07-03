package com.binaris.wizardry.content.effect;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class OakFleshMobEffect extends MagicMobEffect {

    public OakFleshMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("oakflesh_movement"), -0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.MAX_HEALTH, WizardryMainMod.location("oakflesh_health"), 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(Attributes.ARMOR, WizardryMainMod.location("oakflesh_armor"), 3.0f, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        return true;
    }
}
