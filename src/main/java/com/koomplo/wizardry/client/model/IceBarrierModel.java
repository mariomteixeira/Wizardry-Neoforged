package com.koomplo.wizardry.client.model;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.content.entity.construct.IceBarrierConstruct;
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

/** 1:1 port of the 1.12.2 ModelIceBarrier (Blockbench). */
public class IceBarrierModel extends EntityModel<IceBarrierConstruct> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WizardryMainMod.location("ice_barrier"), "main");

    private final ModelPart mainGroup;

    public IceBarrierModel(ModelPart root) {
        this.mainGroup = root.getChild("mainGroup");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition mainGroup = partdefinition.addOrReplaceChild("mainGroup",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-14.5F, -14.5F, -1.5F, 29, 29, 3)
                        .texOffs(0, 32).addBox(-9.0F, -9.0F, -4.0F, 18, 18, 8),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull IceBarrierConstruct entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        mainGroup.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
