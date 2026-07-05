package com.koomplo.wizardry.client.renderer.entity;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.entity.construct.ScaledConstructEntity;
import com.koomplo.wizardry.api.content.util.DrawingUtils;
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
import org.joml.Matrix4f;

/**
 * Renders the radiant/withering totems: a camera-facing flare plus a spinning animated cube
 * (1.12.2 RenderRadiantTotem / RenderWitheringTotem, parameterised by flare tint).
 */
public class TotemRenderer<T extends ScaledConstructEntity> extends EntityRenderer<T> {

    private static final ResourceLocation FLARE_TEXTURE = WizardryMainMod.location("textures/entity/totem/flare.png");
    private static final ResourceLocation[] CUBE_TEXTURES = new ResourceLocation[14];

    static {
        for (int i = 0; i < CUBE_TEXTURES.length; i++) {
            CUBE_TEXTURES[i] = WizardryMainMod.location("textures/entity/totem/cube_" + i + ".png");
        }
    }

    private final java.util.function.Function<T, Integer> flareColour;

    public TotemRenderer(EntityRendererProvider.Context context, java.util.function.Function<T, Integer> flareColour) {
        super(context);
        this.flareColour = flareColour;
    }

    @Override
    public void render(@NotNull T entity, float yaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        poseStack.translate(0, entity.getBbHeight() / 2, 0);

        float s = DrawingUtils.smoothScaleFactor(entity.lifetime, entity.tickCount, partialTicks, 10, 10);
        poseStack.scale(s, s, s);

        drawFlare(poseStack, entity);
        drawCube(poseStack, entity, partialTicks);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void drawFlare(PoseStack poseStack, T entity) {
        poseStack.pushPose();

        // Additive blending stands in for the 1.12.2 GL_ADD texture environment
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, FLARE_TEXTURE);

        // Billboard facing the camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        int colour = flareColour.apply(entity);
        float red = ((colour & 0xFF0000) >> 16) / 255f;
        float green = ((colour & 0xFF00) >> 8) / 255f;
        float blue = (colour & 0xFF) / 255f;

        Matrix4f pose = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float r = 0.5f;
        buffer.addVertex(pose, -r, r, 0).setUv(0, 0).setColor(red, green, blue, 1f);
        buffer.addVertex(pose, r, r, 0).setUv(1, 0).setColor(red, green, blue, 1f);
        buffer.addVertex(pose, r, -r, 0).setUv(1, 1).setColor(red, green, blue, 1f);
        buffer.addVertex(pose, -r, -r, 0).setUv(0, 1).setColor(red, green, blue, 1f);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
    }

    private void drawCube(PoseStack poseStack, T entity, float partialTicks) {
        poseStack.pushPose();

        float age = entity.tickCount + partialTicks;
        float rotationSpeed = 2;

        poseStack.mulPose(Axis.YP.rotationDegrees(age * rotationSpeed / 2));
        poseStack.mulPose(Axis.of(new org.joml.Vector3f(0.7071f, 0, 0.7071f)).rotationDegrees(60));
        poseStack.mulPose(Axis.YP.rotationDegrees(age * rotationSpeed));

        poseStack.scale(0.5f, 0.5f, 0.5f);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, CUBE_TEXTURES[entity.tickCount % CUBE_TEXTURES.length]);

        Matrix4f pose = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        // Unit cube centred at the origin, vertex order matching GeometryUtil.getVertices
        float h = 0.5f;
        float[][] v = {
                {-h, -h, -h}, {h, -h, -h}, {h, -h, h}, {-h, -h, h},
                {-h, h, -h}, {h, h, -h}, {h, h, h}, {-h, h, h}
        };

        drawFace(buffer, pose, v[0], v[1], v[3], v[2], 0.5f, 0, 0.75f, 0.5f); // Bottom
        drawFace(buffer, pose, v[6], v[7], v[2], v[3], 0.75f, 0.5f, 1, 1); // South
        drawFace(buffer, pose, v[5], v[6], v[1], v[2], 0, 0.5f, 0.25f, 1); // East
        drawFace(buffer, pose, v[4], v[5], v[0], v[1], 0.25f, 0.5f, 0.5f, 1); // North
        drawFace(buffer, pose, v[7], v[4], v[3], v[0], 0.5f, 0.5f, 0.75f, 1); // West
        drawFace(buffer, pose, v[5], v[4], v[6], v[7], 0.25f, 0, 0.5f, 0.5f); // Top

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
    }

    private static void drawFace(BufferBuilder buffer, Matrix4f pose, float[] topLeft, float[] topRight, float[] bottomLeft, float[] bottomRight, float u1, float v1, float u2, float v2) {
        buffer.addVertex(pose, topLeft[0], topLeft[1], topLeft[2]).setUv(u1, v1).setColor(1f, 1f, 1f, 1f);
        buffer.addVertex(pose, topRight[0], topRight[1], topRight[2]).setUv(u2, v1).setColor(1f, 1f, 1f, 1f);
        buffer.addVertex(pose, bottomRight[0], bottomRight[1], bottomRight[2]).setUv(u2, v2).setColor(1f, 1f, 1f, 1f);
        buffer.addVertex(pose, bottomLeft[0], bottomLeft[1], bottomLeft[2]).setUv(u1, v2).setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return CUBE_TEXTURES[entity.tickCount % CUBE_TEXTURES.length];
    }
}
