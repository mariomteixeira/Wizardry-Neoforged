package com.koomplo.wizardry.client.renderer.entity;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.model.IceBarrierModel;
import com.koomplo.wizardry.content.entity.construct.IceBarrierConstruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class IceBarrierRenderer extends EntityRenderer<IceBarrierConstruct> {
    private static final ResourceLocation TEXTURE = WizardryMainMod.location("textures/entity/ice_barrier.png");
    private final IceBarrierModel model;

    public IceBarrierRenderer(Context context) {
        super(context);
        this.model = new IceBarrierModel(context.bakeLayer(IceBarrierModel.LAYER_LOCATION));
    }

    @Override
    public void render(IceBarrierConstruct entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() / 2, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(0, -entity.getBbHeight() / 2 - 0.3, 0);

        float s = entity.getSizeMultiplier();
        poseStack.scale(s, s, s);

        this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull IceBarrierConstruct construct) {
        return TEXTURE;
    }
}
