package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.core.config.EBServerConfig;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class OakFleshMobEffect extends MagicMobEffect {

    public OakFleshMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0);
        // Curvas leem a config na aplicação do efeito (default 1.12.2: armor 3)
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, WizardryMainMod.location("oakflesh_movement"),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                amp -> EBServerConfig.FLESH_SPELLS_CAUSE_SLOWNESS.get() ? -0.1 : 0);
        this.addAttributeModifier(Attributes.MAX_HEALTH, WizardryMainMod.location("oakflesh_health"), 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(Attributes.ARMOR, WizardryMainMod.location("oakflesh_armor"),
                AttributeModifier.Operation.ADD_VALUE,
                amp -> EBServerConfig.OAK_FLESH_ARMOR_BONUS.get());
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        return true;
    }
}
