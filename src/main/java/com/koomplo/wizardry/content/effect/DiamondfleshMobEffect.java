package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class DiamondfleshMobEffect extends MagicMobEffect {

    public DiamondfleshMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        // TODO config (marco 7): fleshSpellsCauseSlowness, diamondFleshArmorBonus/ToughnessBonus (defaults 1.12.2)
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("diamondflesh_movement"), -0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, WizardryMainMod.location("diamondflesh_toughness"), 3.0f, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ARMOR, WizardryMainMod.location("diamondflesh_armor"), 4.0f, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
