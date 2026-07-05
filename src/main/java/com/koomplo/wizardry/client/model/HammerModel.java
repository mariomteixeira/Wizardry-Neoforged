package com.koomplo.wizardry.client.model;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.content.entity.construct.HammerConstruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jetbrains.annotations.NotNull;

/** 1:1 port of the 1.12.2 ModelHammer. */
public class HammerModel extends EntityModel<HammerConstruct> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WizardryMainMod.location("lightning_hammer"), "main");

    private final ModelPart root;

    public HammerModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hammer_head",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0F, 0F, 0F, 20, 12, 12),
                PartPose.offset(-10F, 12F, -6F));

        partdefinition.addOrReplaceChild("handle",
                CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0F, 0F, 0F, 4, 14, 4),
                PartPose.offset(-2F, -2F, -2F));

        partdefinition.addOrReplaceChild("handle_end",
                CubeListBuilder.create().texOffs(0, 49).mirror().addBox(0F, 0F, 0F, 5, 5, 5),
                PartPose.offset(-2.5F, -7F, -2.5F));

        partdefinition.addOrReplaceChild("handle_base",
                CubeListBuilder.create().texOffs(0, 42).mirror().addBox(0F, 0F, 0F, 5, 2, 5),
                PartPose.offset(-2.5F, 10F, -2.5F));

        partdefinition.addOrReplaceChild("ring1",
                CubeListBuilder.create().texOffs(20, 24).mirror().addBox(0F, 0F, 0F, 2, 14, 14),
                PartPose.offset(-8F, 11F, -7F));

        partdefinition.addOrReplaceChild("ring2",
                CubeListBuilder.create().texOffs(20, 24).mirror().addBox(0F, 0F, 0F, 2, 14, 14),
                PartPose.offset(6F, 11F, -7F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull HammerConstruct entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
