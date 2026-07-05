package com.koomplo.wizardry.client.renderer.blockentity;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.util.DrawingUtils;
import com.koomplo.wizardry.content.blockentity.MagicLightBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Random;

public class MagicLightRenderer implements BlockEntityRenderer<MagicLightBlockEntity> {

    private static final ResourceLocation FLARE_TEXTURE = WizardryMainMod.location("textures/entity/light/flare.png");

    public MagicLightRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MagicLightBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float s = DrawingUtils.smoothScaleFactor(blockEntity.getLifetime(), blockEntity.timer, partialTick, 10, 10);
        poseStack.scale(s, s, s);

        // Camera-facing flare
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer flare = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));

        int light = LightTexture.FULL_BRIGHT;
        flare.addVertex(matrix, -0.6f, 0.6f, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        flare.addVertex(matrix, -0.6f, -0.6f, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        flare.addVertex(matrix, 0.6f, -0.6f, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        flare.addVertex(matrix, 0.6f, 0.6f, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);

        poseStack.popPose();

        // Shimmering rays (ImbuementAltarRenderer pattern)
        float t = blockEntity.getLevel() == null ? 0 : (blockEntity.getLevel().getGameTime() + partialTick);
        Random random = new Random(blockEntity.getBlockPos().asLong());
        VertexConsumer rays = bufferSource.getBuffer(RenderType.lightning());

        for (int j = 0; j < 12; j++) {
            int m = random.nextInt(10);
            int n = random.nextInt(10);
            int sliceAngle = 20 + m;
            float scale = 0.35f;

            poseStack.pushPose();

            float rotationVariation = Mth.sin((t + j * 10) * 0.01f) * 5;
            poseStack.mulPose(Axis.XP.rotationDegrees(31 * m + rotationVariation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(31 * n + rotationVariation));

            Matrix4f rayMatrix = poseStack.last().pose();

            double x1 = scale * Mth.sin((t + 40 * j) * ((float) Math.PI / 180));
            double z1 = scale * Mth.cos((t + 40 * j) * ((float) Math.PI / 180));
            double x2 = scale * Mth.sin((t + 40 * j - sliceAngle) * ((float) Math.PI / 180));
            double z2 = scale * Mth.cos((t + 40 * j - sliceAngle) * ((float) Math.PI / 180));

            rays.addVertex(rayMatrix, 0, 0, 0).setColor(255, 255, 255, 200);
            rays.addVertex(rayMatrix, 0, 0, 0).setColor(255, 255, 255, 200);
            rays.addVertex(rayMatrix, (float) x1, 0, (float) z1).setColor(255, 255, 220, 90);
            rays.addVertex(rayMatrix, (float) x2, 0, (float) z2).setColor(255, 255, 220, 90);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
