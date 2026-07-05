package com.binaris.wizardry.client.renderer.entity;

import com.binaris.wizardry.content.entity.construct.ForcefieldConstruct;
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

/** Pulsing translucent UV sphere (1.12.2 RenderForcefield). */
public class ForcefieldRenderer extends EntityRenderer<ForcefieldConstruct> {

    private static final float EXPANSION_TIME = 3;

    public ForcefieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull ForcefieldConstruct entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        float latStep = (float) Math.PI / 20;
        float longStep = (float) Math.PI / 20;

        float pulse = Mth.sin((entity.tickCount + partialTicks) / 10f);

        float r = 0.35f, g = 0.55f + 0.05f * pulse, b = 1;

        float radius = entity.getRadius();
        float a = 0.5f;

        if (entity.tickCount > entity.lifetime - EXPANSION_TIME) {
            radius *= 1 + 0.2f * (entity.tickCount + partialTicks - (entity.lifetime - EXPANSION_TIME)) / EXPANSION_TIME;
            a *= Math.max(0, 1 - (entity.tickCount + partialTicks - (entity.lifetime - EXPANSION_TIME)) / EXPANSION_TIME);
        } else if (entity.tickCount < EXPANSION_TIME) {
            radius *= 1 - (EXPANSION_TIME - entity.tickCount - partialTicks) / EXPANSION_TIME;
            a *= 1 - (EXPANSION_TIME - entity.tickCount - partialTicks) / EXPANSION_TIME;
        }

        Matrix4f pose = poseStack.last().pose();

        // Draw the inside first
        drawSphere(pose, radius - 0.1f - 0.025f * pulse, latStep, longStep, true, r, g, b, a);
        drawSphere(pose, radius - 0.1f - 0.025f * pulse, latStep, longStep, false, 1, 1, 1, a);
        drawSphere(pose, radius, latStep, longStep, false, r, g, b, 0.7f * a);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    /** Draws a lat/long sphere as a single triangle strip, exactly like the 1.12.2 version. */
    private static void drawSphere(Matrix4f pose, float radius, float latStep, float longStep, boolean inside, float r, float g, float b, float a) {

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        boolean goingUp = inside;

        buffer.addVertex(pose, 0, goingUp ? -radius : radius, 0).setColor(r, g, b, a); // Start at the north pole

        for (float longitude = -(float) Math.PI; longitude <= (float) Math.PI; longitude += longStep) {

            // Leave the poles out since they only have a single point per stack instead of two
            for (float theta = (float) Math.PI / 2 - latStep; theta >= -(float) Math.PI / 2 + latStep; theta -= latStep) {

                float latitude = goingUp ? -theta : theta;

                float hRadius = radius * Mth.cos(latitude);
                float vy = radius * Mth.sin(latitude);
                float vx = hRadius * Mth.sin(longitude);
                float vz = hRadius * Mth.cos(longitude);

                buffer.addVertex(pose, vx, vy, vz).setColor(r, g, b, a);

                vx = hRadius * Mth.sin(longitude + longStep);
                vz = hRadius * Mth.cos(longitude + longStep);

                buffer.addVertex(pose, vx, vy, vz).setColor(r, g, b, a);
            }

            // The next pole
            buffer.addVertex(pose, 0, goingUp ? radius : -radius, 0).setColor(r, g, b, a);

            goingUp = !goingUp;
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ForcefieldConstruct entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
