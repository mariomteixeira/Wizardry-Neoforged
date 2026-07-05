package com.binaris.wizardry.client.renderer.entity;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.content.entity.ShieldEntity;
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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * Bevelled force-shield panel with the pulsing texture, port of the 1.12.2 RenderShield effect (the pulse
 * was a texture-matrix scale in 1.12.2; here the UVs are computed per frame instead).
 */
public class ShieldRenderer extends EntityRenderer<ShieldEntity> {

    private static final ResourceLocation TEXTURE = WizardryMainMod.location("textures/entity/shield.png");

    private static final float WIDTH_OUTER = 0.6f;
    private static final float HEIGHT_OUTER = 0.7f;
    private static final float WIDTH_INNER = 0.3f;
    private static final float HEIGHT_INNER = 0.4f;
    private static final float DEPTH = 0.2f;

    public ShieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull ShieldEntity entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        poseStack.translate(0, 0.35, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.translate(0, 0, 0.7);

        // Texture pulse (1.12.2 scaled the texture matrix around its centre every 5 ticks)
        float s = 1 - ((entity.tickCount + partialTicks) % 5) / 5 * 0.52f;
        s = s * s;

        Matrix4f pose = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

        bevel(buffer, pose, s, -WIDTH_OUTER, HEIGHT_INNER, -DEPTH, 0, 0.2f, true);
        bevel(buffer, pose, s, -WIDTH_INNER, HEIGHT_INNER, 0, 0.2f, 0.2f, false);
        bevel(buffer, pose, s, -WIDTH_INNER, HEIGHT_OUTER, -DEPTH, 0.2f, 0, true);
        bevel(buffer, pose, s, -WIDTH_INNER, HEIGHT_INNER, 0, 0.2f, 0.2f, false);

        bevel(buffer, pose, s, WIDTH_INNER, HEIGHT_OUTER, -DEPTH, 0.8f, 0, true);
        bevel(buffer, pose, s, WIDTH_INNER, HEIGHT_INNER, 0, 0.8f, 0.2f, false);
        bevel(buffer, pose, s, WIDTH_OUTER, HEIGHT_INNER, -DEPTH, 1, 0.2f, true);
        bevel(buffer, pose, s, WIDTH_INNER, HEIGHT_INNER, 0, 0.8f, 0.2f, false);

        bevel(buffer, pose, s, WIDTH_OUTER, -HEIGHT_INNER, -DEPTH, 1, 0.8f, true);
        bevel(buffer, pose, s, WIDTH_INNER, -HEIGHT_INNER, 0, 0.8f, 0.8f, false);
        bevel(buffer, pose, s, WIDTH_INNER, -HEIGHT_OUTER, -DEPTH, 0.8f, 1, true);
        bevel(buffer, pose, s, WIDTH_INNER, -HEIGHT_INNER, 0, 0.8f, 0.8f, false);

        bevel(buffer, pose, s, -WIDTH_INNER, -HEIGHT_OUTER, -DEPTH, 0.2f, 1, true);
        bevel(buffer, pose, s, -WIDTH_INNER, -HEIGHT_INNER, 0, 0.2f, 0.8f, false);
        bevel(buffer, pose, s, -WIDTH_OUTER, -HEIGHT_INNER, -DEPTH, 0, 0.8f, true);
        bevel(buffer, pose, s, -WIDTH_INNER, -HEIGHT_INNER, 0, 0.2f, 0.8f, false);

        bevel(buffer, pose, s, -WIDTH_OUTER, HEIGHT_INNER, -DEPTH, 0, 0.2f, true);
        bevel(buffer, pose, s, -WIDTH_INNER, HEIGHT_INNER, 0, 0.2f, 0.2f, false);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

        bevel(buffer, pose, s, -WIDTH_INNER, HEIGHT_INNER, 0, 0.2f, 0.2f, false);
        bevel(buffer, pose, s, WIDTH_INNER, HEIGHT_INNER, 0, 0.8f, 0.2f, false);
        bevel(buffer, pose, s, -WIDTH_INNER, -HEIGHT_INNER, 0, 0.2f, 0.8f, false);
        bevel(buffer, pose, s, WIDTH_INNER, -HEIGHT_INNER, 0, 0.8f, 0.8f, false);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    /** Adds one vertex, applying the pulse in texture space (u' = 0.5 + (u-0.5)*s). Edge vertices are black. */
    private static void bevel(BufferBuilder buffer, Matrix4f pose, float pulse, float x, float y, float z, float u, float v, boolean edge) {
        float u1 = 0.5f + (u - 0.5f) * pulse;
        float v1 = 0.5f + (v - 0.5f) * pulse;
        if (edge) buffer.addVertex(pose, x, y, z).setUv(u1, v1).setColor(0, 0, 0, 255);
        else buffer.addVertex(pose, x, y, z).setUv(u1, v1).setColor(200, 200, 255, 255);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ShieldEntity entity) {
        return TEXTURE;
    }
}
