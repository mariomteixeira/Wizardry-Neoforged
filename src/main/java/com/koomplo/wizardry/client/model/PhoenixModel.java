package com.koomplo.wizardry.client.model;

import com.koomplo.wizardry.content.entity.living.Phoenix;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import static com.koomplo.wizardry.WizardryMainMod.MOD_ID;

/** 1:1 port of the 1.12.2 ModelPhoenix. */
public class PhoenixModel extends EntityModel<Phoenix> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "phoenix"), "main");

    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart tail;
    private final ModelPart tailFeathers;
    private final ModelPart neck;

    public PhoenixModel(ModelPart root) {
        // 1.12.2 rendered the phoenix blended (soft wing edges in the texture)
        super(RenderType::entityTranslucent);
        this.body = root.getChild("body");
        this.rightWing = body.getChild("right_wing");
        this.leftWing = body.getChild("left_wing");
        this.tail = body.getChild("tail");
        this.tailFeathers = tail.getChild("tail_feathers");
        this.neck = root.getChild("neck");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 34).mirror()
                        .addBox(0F, 0F, -3F, 6, 15, 6),
                PartPose.offsetAndRotation(-3F, 0F, -5F, 0.296706F, 0F, 0F));

        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-27F, -27F, 0F, 27, 34, 0),
                PartPose.offsetAndRotation(0F, 5F, 0F, 0.1745329F, 0F, 0F));

        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(0F, -27F, 0F, 27, 34, 0),
                PartPose.offsetAndRotation(6F, 5F, 0F, 0.1745329F, 0F, 0F));

        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(20, 55).mirror()
                        .addBox(-1F, 0F, -1F, 2, 7, 2),
                PartPose.offsetAndRotation(3F, 15F, 2F, 0.4014257F, 0F, 0F));

        tail.addOrReplaceChild("tail_feathers",
                CubeListBuilder.create().texOffs(0, 57).mirror()
                        .addBox(-5F, 0F, 0F, 10, 7, 0),
                PartPose.offsetAndRotation(0F, 7F, 1F, 0.5235988F, 0F, 0F));

        PartDefinition neck = partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(24, 44).mirror()
                        .addBox(-1F, -4F, -1F, 2, 4, 2),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.2443461F, 0F, 0F));

        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(24, 34).mirror()
                        .addBox(-2F, -4F, -5F, 4, 4, 6),
                PartPose.offset(0F, -4F, 0F));

        head.addOrReplaceChild("plume",
                CubeListBuilder.create().texOffs(28, 50).mirror()
                        .addBox(-0.03333334F, 1F, 0F, 0, 5, 5),
                PartPose.offset(0F, -7F, 0F));

        head.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(32, 44).mirror()
                        .addBox(-0.5F, 4F, -1F, 1, 2, 3),
                PartPose.offsetAndRotation(0F, -5F, -8F, 0.2792527F, 0F, 0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull Phoenix entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.neck.xRot = headPitch * ((float) Math.PI / 180F);
        this.neck.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.neck.zRot = 0.0F;
        this.body.xRot = 0.3f + Mth.cos(ageInTicks * 0.1F) * 0.15F;
        this.body.yRot = 0.0F;
        this.tail.xRot = this.body.xRot * 1.1f;
        this.tailFeathers.xRot = this.body.xRot * 1.2f;
        this.rightWing.yRot = Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.15F;
        this.leftWing.yRot = -this.rightWing.yRot;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        neck.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
