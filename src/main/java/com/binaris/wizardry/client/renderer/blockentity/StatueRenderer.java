package com.binaris.wizardry.client.renderer.blockentity;

import com.binaris.wizardry.content.blockentity.StatueBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Draws the creature frozen inside a statue (1.12.2 RenderStatue). Only the bottom block renders; the pose is
 * frozen by passing partialTicks 0 to the entity renderer.
 * TODO marco 6: overlay de textura de pedra para petrificados (1.12.2 LayerStone); hoje a criatura aparece normal.
 */
public class StatueRenderer implements BlockEntityRenderer<StatueBlockEntity> {

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

        // partialTicks 0 freezes all animation
        Minecraft.getInstance().getEntityRenderDispatcher().render(creature, 0, 0, 0, 0, 0,
                poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull StatueBlockEntity statue) {
        return true; // The creature can poke out of the block bounds
    }
}
