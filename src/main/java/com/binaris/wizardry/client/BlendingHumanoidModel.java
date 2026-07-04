package com.binaris.wizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class BlendingHumanoidModel extends HumanoidModel<LivingEntity> {
    public BlendingHumanoidModel(ModelPart root) {
        super(root);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        // Translucency comes from the vertex alpha alone. Never touch RenderSystem globals from inside a
        // buffered entity render: the old code "restored" the global shader color to alpha 0.5, leaving every
        // subsequently drawn GUI translucent (transparent Arcane Workbench bug).
        super.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.color(128, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color)));
    }
}
