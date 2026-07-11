package com.koomplo.wizardry.client.renderer.blockentity;

import com.koomplo.wizardry.content.blockentity.StatueBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Draws the creature frozen inside a statue (1.12.2 RenderStatue). Only the bottom block renders; the pose is
 * frozen by passing partialTicks 0 to the entity renderer. The whole model renders with vanilla stone tiled
 * over it (1.12.2 LayerStone/LayerTiledOverlay), so the creature reads as a statue.
 */
public class StatueRenderer implements BlockEntityRenderer<StatueBlockEntity> {

    private static final ResourceLocation STONE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");

    public StatueRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StatueBlockEntity statue, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (statue.position != 1) return;

        Mob creature = statue.getOrCreateDisplayCreature();
        if (creature == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-creature.yBodyRot));

        // Every layer the entity renderer requests is redirected to a stone cutout buffer with UVs
        // regenerated from the vertex position (same mechanism as the block crumbling decal), which
        // tiles the 16px stone texture over the model like the 1.12.2 LayerTiledOverlay did.
        VertexConsumer stoneBuffer = new SheetedDecalTextureGenerator(
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(STONE_TEXTURE)), poseStack.last(), 1f);
        MultiBufferSource stoneSource = type -> stoneBuffer;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false); // A sombra usa RenderType próprio que o redirect distorceria
        // partialTicks 0 freezes all animation
        dispatcher.render(creature, 0, 0, 0, 0, 0, poseStack, stoneSource, packedLight);
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull StatueBlockEntity statue) {
        return true; // The creature can poke out of the block bounds
    }
}
