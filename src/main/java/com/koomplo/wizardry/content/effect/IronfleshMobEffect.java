package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.core.config.EBServerConfig;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class IronfleshMobEffect extends MagicMobEffect {

    public IronfleshMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        // Curvas leem a config na aplicação do efeito (default 1.12.2: armor 4)
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("ironflesh_movement"),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                amp -> EBServerConfig.FLESH_SPELLS_CAUSE_SLOWNESS.get() ? -0.1 : 0);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, WizardryMainMod.location("ironflesh_knockback"), 0.3f, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.ARMOR, WizardryMainMod.location("ironflesh_armor"),
                AttributeModifier.Operation.ADD_VALUE,
                amp -> EBServerConfig.IRON_FLESH_ARMOR_BONUS.get());
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
