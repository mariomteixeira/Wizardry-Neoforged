package com.koomplo.wizardry.content.spell.healing;

import com.koomplo.wizardry.api.content.event.EBLivingHurtEvent;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class Transience extends Spell {

    @Override
    public boolean cast(PlayerCastContext ctx) {
        if (ctx.caster().hasEffect(EBMobEffects.holder(EBMobEffects.TRANSIENCE))) return false;

        if (!ctx.world().isClientSide) {
            int duration = (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION));
            ctx.caster().addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.TRANSIENCE), duration, 0));
            ctx.caster().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, false));
            this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), duration);
        }
        return true;
    }

    @Override
    public boolean requiresPacket() {
        return true;
    }

    // Transient entities neither take blockable damage nor deal any (1.12.2 Transience handlers)
    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;

        if (event.getDamagedEntity().hasEffect(EBMobEffects.holder(EBMobEffects.TRANSIENCE))
                && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            event.setCanceled(true);
            return;
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.hasEffect(EBMobEffects.holder(EBMobEffects.TRANSIENCE))) {
            event.setCanceled(true);
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.HEALING, SpellType.BUFF, SpellAction.POINT_UP, 50, 15, 100)
                .add(DefaultProperties.EFFECT_DURATION, 400)
                .build();
    }
}
