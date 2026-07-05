package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class IronfleshMobEffect extends MagicMobEffect {

    public IronfleshMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        // TODO config (marco 7): fleshSpellsCauseSlowness, ironFleshArmorBonus (defaults 1.12.2)
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("ironflesh_movement"), -0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, WizardryMainMod.location("ironflesh_knockback"), 0.3f, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ARMOR, WizardryMainMod.location("ironflesh_armor"), 4.0f, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
