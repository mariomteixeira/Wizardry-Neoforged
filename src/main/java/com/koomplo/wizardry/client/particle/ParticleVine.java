package com.koomplo.wizardry.client.particle;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.client.particle.ParticleTargeted;
import com.koomplo.wizardry.api.client.particle.ParticleWizardry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParticleVine extends ParticleTargeted {

    /** Half the width of the vine. */
    private static final float THICKNESS = 0.02f;
    private static final float LEAF_SPACING = 0.5f;
    private static final float SEGMENT_LENGTH = 1;

    private static final ResourceLocation STEM_TEXTURE = WizardryMainMod.location("textures/particle/vine.png");
    private static final ResourceLocation[] LEAF_TEXTURES = new ResourceLocation[5];

    static {
        for (int i = 0; i < LEAF_TEXTURES.length; i++) {
            LEAF_TEXTURES[i] = WizardryMainMod.location("textures/particle/vine_leaf_" + i + ".png");
        }
    }

    public ParticleVine(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider) {
        super(world, x, y, z, spriteProvider, false);
        this.setColor(0.2f, 0.65f, 0f);
        this.setLifetime(0);
        this.quadSize = 1;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        // Immediate-mode drawing must never happen inside a sheet batch (it corrupts the shared Tesselator)
        return ParticleRenderType.CUSTOM;
    }

    @Override
    protected boolean shouldApplyOriginOffset() {
        return false;
    }

    @Override
    protected void draw(PoseStack stack, Tesselator tesselator, float length, float tickDelta) {
        random.setSeed(seed); // Reset the random so we get the same sequence of numbers each frame

        float scale = this.quadSize;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, STEM_TEXTURE);

        // Everything is drawn back-to-front so it looks like the vine is growing from the origin, not the endpoint
        float i = 0;
        while (i + SEGMENT_LENGTH < length) {
            drawShearedBox(stack, tesselator, length - i, length - i - SEGMENT_LENGTH, THICKNESS * scale);
            i += SEGMENT_LENGTH;
        }
        drawShearedBox(stack, tesselator, length - i, 0, THICKNESS * scale);

        for (double l = length; l > 0; l -= LEAF_SPACING) {
            stack.pushPose();
            stack.mulPose(Axis.ZP.rotationDegrees(random.nextInt(4) * 90));

            RenderSystem.setShaderTexture(0, LEAF_TEXTURES[random.nextInt(LEAF_TEXTURES.length)]);

            float w = 16 * THICKNESS * scale;
            float colourVariation = 0.3f;
            float r = Mth.clamp(rCol + (random.nextFloat() - 0.5f) * colourVariation, 0, 1);
            float g = Mth.clamp(gCol + (random.nextFloat() - 0.5f) * colourVariation, 0, 1);
            float b = Mth.clamp(bCol + (random.nextFloat() - 0.5f) * colourVariation, 0, 1);

            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex(stack.last().pose(), 0, 0, (float) l).setUv(0, 0).setColor(r, g, b, alpha);
            buffer.addVertex(stack.last().pose(), w, 0, (float) l).setUv(1, 0).setColor(r, g, b, alpha);
            buffer.addVertex(stack.last().pose(), w, w, (float) l).setUv(1, 1).setColor(r, g, b, alpha);
            buffer.addVertex(stack.last().pose(), 0, w, (float) l).setUv(0, 1).setColor(r, g, b, alpha);
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            stack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    /** Draws a single box for one segment of the vine, from z1 to z2 along the particle's axis, with given half-width. */
    private void drawShearedBox(PoseStack stack, Tesselator tesselator, float z1, float z2, float width) {
        float u1 = 0;
        float u2 = (z1 - z2) / SEGMENT_LENGTH;
        float v1 = 0;
        // width * 8 gives the total 'circumference' of the box
        float v2 = 0.0625f;
        float v3 = 0.125f;
        float v4 = 0.1875f;
        float v5 = 0.25f;

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.addVertex(stack.last().pose(), -width, -width, z1).setUv(u1, v1).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), -width, -width, z2).setUv(u2, v1).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), -width, width, z1).setUv(u1, v2).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), -width, width, z2).setUv(u2, v2).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), width, width, z1).setUv(u1, v3).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), width, width, z2).setUv(u2, v3).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), width, -width, z1).setUv(u1, v4).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), width, -width, z2).setUv(u2, v4).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), -width, -width, z1).setUv(u1, v5).setColor(rCol, gCol, bCol, alpha);
        buffer.addVertex(stack.last().pose(), -width, -width, z2).setUv(u2, v5).setColor(rCol, gCol, bCol, alpha);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static class VineProvider implements ParticleProvider<SimpleParticleType> {
        static SpriteSet spriteProvider;

        public VineProvider(SpriteSet sprite) {
            spriteProvider = sprite;
        }

        public static ParticleWizardry createParticle(ClientLevel clientWorld, Vec3 vec3d) {
            return new ParticleVine(clientWorld, vec3d.x, vec3d.y, vec3d.z, spriteProvider);
        }

        @Nullable
        @Override
        public Particle createParticle(@NotNull SimpleParticleType parameters, @NotNull ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new ParticleVine(world, x, y, z, spriteProvider);
        }
    }
}
