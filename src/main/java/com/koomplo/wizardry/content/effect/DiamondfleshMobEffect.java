package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.core.config.EBServerConfig;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class DiamondfleshMobEffect extends MagicMobEffect {

    public DiamondfleshMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        // Curvas leem a config na aplicação do efeito (defaults 1.12.2: armor 4, toughness 3)
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("diamondflesh_movement"),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                amp -> EBServerConfig.FLESH_SPELLS_CAUSE_SLOWNESS.get() ? -0.1 : 0);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, WizardryMainMod.location("diamondflesh_toughness"),
                AttributeModifier.Operation.ADD_VALUE,
                amp -> EBServerConfig.DIAMOND_FLESH_TOUGHNESS_BONUS.get());
        this.addAttributeModifier(Attributes.ARMOR, WizardryMainMod.location("diamondflesh_armor"),
                AttributeModifier.Operation.ADD_VALUE,
                amp -> EBServerConfig.DIAMOND_FLESH_ARMOR_BONUS.get());
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
