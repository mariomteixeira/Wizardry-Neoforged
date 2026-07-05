package com.binaris.wizardry.content.spell.healing;

import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import com.binaris.wizardry.content.entity.ShieldEntity;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Shield extends Spell {

    @Override
    public boolean cast(PlayerCastContext ctx) {
        var caster = ctx.caster();
        Level world = ctx.world();

        if (!world.isClientSide) {
            caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10,
                    property(DefaultProperties.EFFECT_STRENGTH), false, false));

            WizardDataHolder data = caster.getData(EBAttachments.WIZARD_DATA);

            if (data.getShieldEntity() == null || !data.getShieldEntity().isAlive()) {
                ShieldEntity shield = new ShieldEntity(world, caster);
                data.setShieldEntity(shield);
                world.addFreshEntity(shield);
            }
        }

        this.playSound(world, caster, ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    @Override
    protected void playSound(Level world, LivingEntity entity, int castTicks, int duration) {
        this.playSoundLoop(world, entity, castTicks);
    }

    @Override
    protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration) {
        this.playSoundLoop(world, x, y, z, ticksInUse, duration);
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.HEALING, SpellType.DEFENCE, SpellAction.NONE, 5, 0, 0)
                .add(DefaultProperties.EFFECT_STRENGTH, 0)
                .build();
    }
}
