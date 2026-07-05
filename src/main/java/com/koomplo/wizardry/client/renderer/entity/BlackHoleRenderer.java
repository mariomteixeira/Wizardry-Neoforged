package com.koomplo.wizardry.client.renderer.entity;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.util.DrawingUtils;
import com.koomplo.wizardry.content.entity.construct.BlackHoleConstruct;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/** Spinning darkness rays around a black centre (1.12.2 RenderBlackHole). */
public class BlackHoleRenderer extends EntityRenderer<BlackHoleConstruct> {

    private static final ResourceLocation RAY_TEXTURE = WizardryMainMod.location("textures/entity/black_hole/ray.png");
    private static final ResourceLocation CENTRE_TEXTURE = WizardryMainMod.location("textures/entity/black_hole/centre.png");

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull BlackHoleConstruct entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, RAY_TEXTURE);

        float s = DrawingUtils.smoothScaleFactor(entity.lifetime, entity.tickCount, partialTicks, 10, 10);
        poseStack.scale(s, s, s);

        Matrix4f pose = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        for (int j = 0; j < 30; j++) {

            float radius = 3.0f * entity.getSizeMultiplier();

            int a = entity.randomiser[j];
            int b = entity.randomiser2[j];

            int sliceAngle = 20 + a;

            double x1 = radius * Mth.sin((entity.tickCount + 40 * j) * ((float) Math.PI / 180f));
            double z1 = radius * Mth.cos((entity.tickCount + 40 * j) * ((float) Math.PI / 180f));

            double x2 = radius * Mth.sin((entity.tickCount + 40 * j - sliceAngle) * ((float) Math.PI / 180f));
            double z2 = radius * Mth.cos((entity.tickCount + 40 * j - sliceAngle) * ((float) Math.PI / 180f));

            float rayX1 = (float) (x1 * Mth.cos(31 * b));
            float rayY1 = (float) (z1 * Mth.sin(31 * a) + x1 * Mth.cos(31 * a) * Mth.sin(31 * b));
            float rayZ1 = (float) (z1 * Mth.cos(31 * a));

            float rayX2 = (float) (x2 * Mth.cos(31 * b));
            float rayY2 = (float) (z2 * Mth.sin(31 * a) + x2 * Mth.cos(31 * a) * Mth.sin(31 * b));
            float rayZ2 = (float) (z2 * Mth.cos(31 * a));

            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);

            buffer.addVertex(pose, 0, 0, 0).setUv(0, 0);
            buffer.addVertex(pose, 0, 0, 0).setUv(0, 1);
            buffer.addVertex(pose, rayX1, rayY1, rayZ1).setUv(1, 0);
            buffer.addVertex(pose, rayX2, rayY2, rayZ2).setUv(1, 1);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        // Centre aura billboard
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        RenderSystem.setShaderTexture(0, CENTRE_TEXTURE);

        Matrix4f centrePose = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.addVertex(centrePose, -0.4f, 0.4f, 0).setUv(0, 0);
        buffer.addVertex(centrePose, 0.4f, 0.4f, 0).setUv(1, 0);
        buffer.addVertex(centrePose, 0.4f, -0.4f, 0).setUv(1, 1);
        buffer.addVertex(centrePose, -0.4f, -0.4f, 0).setUv(0, 1);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BlackHoleConstruct entity) {
        return RAY_TEXTURE;
    }
}
