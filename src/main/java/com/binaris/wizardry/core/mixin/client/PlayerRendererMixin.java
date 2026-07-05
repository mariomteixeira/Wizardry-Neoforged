package com.binaris.wizardry.core.mixin.client;

import com.binaris.wizardry.content.spell.earth.Glide;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pitches the player model horizontal while casting glide/flight, reproducing the vanilla elytra branch of
 * {@code setupRotations} (which is gated on the fall-flying shared flag — forcing that flag would also swap
 * the movement physics, so the rotation is replicated here instead).
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("TAIL"))
    private void EBWIZARDRY$flightSpellRotations(AbstractClientPlayer entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        // If vanilla already applied the elytra or swimming rotation, don't rotate twice
        if (entity.isFallFlying() || entity.getSwimAmount(partialTick) > 0) return;
        if (!Glide.isCastingFlightSpell(entity)) return;

        float ticks = entity.getTicksUsingItem() + partialTick;
        float ramp = Mth.clamp(ticks * ticks / 100.0F, 0.0F, 1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(ramp * (-90.0F - entity.getViewXRot(partialTick))));

        // Banking when strafing, same as the vanilla elytra math
        Vec3 look = entity.getViewVector(partialTick);
        Vec3 motion = entity.getDeltaMovementLerped(partialTick);
        double d0 = motion.horizontalDistanceSqr();
        double d1 = look.horizontalDistanceSqr();
        if (d0 > 0.0 && d1 > 0.0) {
            double d2 = (motion.x * look.x + motion.z * look.z) / Math.sqrt(d0 * d1);
            double d3 = motion.x * look.z - motion.z * look.x;
            poseStack.mulPose(Axis.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
        }
    }
}
