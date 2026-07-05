package com.binaris.wizardry.client.renderer.entity;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.client.model.HammerModel;
import com.binaris.wizardry.content.entity.construct.HammerConstruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class HammerRenderer extends EntityRenderer<HammerConstruct> {
    private static final ResourceLocation TEXTURE = WizardryMainMod.location("textures/entity/lightning_hammer.png");
    private final HammerModel model;

    public HammerRenderer(Context context) {
        super(context);
        this.model = new HammerModel(context.bakeLayer(HammerModel.LAYER_LOCATION));
    }

    @Override
    public void render(HammerConstruct entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull HammerConstruct construct) {
        return TEXTURE;
    }
}
