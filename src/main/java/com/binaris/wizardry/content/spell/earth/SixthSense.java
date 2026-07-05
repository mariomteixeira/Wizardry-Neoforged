package com.binaris.wizardry.content.spell.earth;

import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.core.config.EBServerConfig;
import com.binaris.wizardry.setup.registries.EBMobEffects;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

public class SixthSense extends Spell {

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        if (!ctx.world().isClientSide) {
            // The amplifier encodes extra detection radius from range upgrades (1.12.2)
            ctx.caster().addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.SIXTH_SENSE),
                    (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)),
                    (int) ((ctx.modifiers().get(SpellModifiers.RANGE) - 1f) / EBServerConfig.RANGE_INCREASE_PER_LEVEL.get())));
        }

        // TODO shader/overlay do sixth_sense do 1.12.2 (sem loader no port — marco 6)

        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.EARTH, SpellType.BUFF, SpellAction.POINT_UP, 20, 0, 100)
                .add(DefaultProperties.EFFECT_DURATION, 400)
                .add(DefaultProperties.EFFECT_RADIUS, 20)
                .build();
    }
}
