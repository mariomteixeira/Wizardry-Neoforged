package com.koomplo.wizardry.client.renderer.entity;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.client.model.PhoenixModel;
import com.koomplo.wizardry.content.entity.living.Phoenix;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PhoenixRenderer extends MobRenderer<Phoenix, PhoenixModel> {

    private static final ResourceLocation TEXTURE = WizardryMainMod.location("textures/entity/phoenix.png");

    public PhoenixRenderer(EntityRendererProvider.Context context) {
        super(context, new PhoenixModel(context.bakeLayer(PhoenixModel.LAYER_LOCATION)), 1.0f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Phoenix entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(@NotNull Phoenix entity, @NotNull BlockPos pos) {
        return 15; // Full-bright, like the 1.12.2 renderer
    }

    @Override
    protected void setupRotations(@NotNull Phoenix entity, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale) {
        poseStack.translate(0.0F, -0.1F, 0.0F);
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick, scale);
    }
}
