package com.koomplo.wizardry.client.renderer.entity;

import com.koomplo.wizardry.content.entity.projectile.LightningDiscEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Flat spinning disc (1.12.2 RenderLightningDisc): a horizontal quad with the lightning sigil texture. */
public class LightningDiscRenderer extends EntityRenderer<LightningDiscEntity> {

    private static final float SCALE = 2.0f;
    private final ResourceLocation texture;

    public LightningDiscRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public void render(@NotNull LightningDiscEntity entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        poseStack.translate(0, entity.getBbHeight() / 2, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTicks) * 30f));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float half = SCALE / 2;
        // Horizontal quad, both faces
        buffer.addVertex(poseStack.last().pose(), -half, 0, -half).setUv(0, 0);
        buffer.addVertex(poseStack.last().pose(), -half, 0, half).setUv(0, 1);
        buffer.addVertex(poseStack.last().pose(), half, 0, half).setUv(1, 1);
        buffer.addVertex(poseStack.last().pose(), half, 0, -half).setUv(1, 0);

        buffer.addVertex(poseStack.last().pose(), half, 0, -half).setUv(1, 0);
        buffer.addVertex(poseStack.last().pose(), half, 0, half).setUv(1, 1);
        buffer.addVertex(poseStack.last().pose(), -half, 0, half).setUv(0, 1);
        buffer.addVertex(poseStack.last().pose(), -half, 0, -half).setUv(0, 0);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LightningDiscEntity entity) {
        return texture;
    }
}
