package com.koomplo.wizardry.core.mixin;

import com.koomplo.wizardry.api.content.event.EBClientTickEvent;
import com.koomplo.wizardry.client.SpellGUIDisplay;
import com.koomplo.wizardry.core.event.WizardryEventBus;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void EBWIZARDRY$clientInit(CallbackInfo ci) {
        SpellGUIDisplay.init();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void EBWIZARDRY$clientTick(CallbackInfo ci) {
        Minecraft minecraft = ((Minecraft) (Object) this);
        WizardryEventBus.getInstance().fire(new EBClientTickEvent(minecraft));
    }

    // Sixth sense: living creatures within range glow through walls for the affected player
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    public void EBWIZARDRY$sixthSenseGlow(net.minecraft.world.entity.Entity entity,
                                          org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = ((Minecraft) (Object) this);
        if (minecraft.player == null || entity == minecraft.player
                || !(entity instanceof net.minecraft.world.entity.LivingEntity)) return;

        var effect = minecraft.player.getEffect(com.koomplo.wizardry.setup.registries.EBMobEffects.holder(
                com.koomplo.wizardry.setup.registries.EBMobEffects.SIXTH_SENSE));

        if (effect != null) {
            double radius = com.koomplo.wizardry.setup.registries.Spells.SIXTH_SENSE
                    .property(com.koomplo.wizardry.content.spell.DefaultProperties.EFFECT_RADIUS)
                    * (1 + effect.getAmplifier() * com.koomplo.wizardry.core.config.EBServerConfig.RANGE_INCREASE_PER_LEVEL.get());
            if (entity.distanceTo(minecraft.player) <= radius) cir.setReturnValue(true);
        }
    }
}
