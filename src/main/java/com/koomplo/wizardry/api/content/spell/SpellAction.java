package com.koomplo.wizardry.api.content.spell;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.util.GeometryUtil;
import com.koomplo.wizardry.setup.registries.EBAttachments;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpellAction {
    public static final SpellAction POINT;
    public static SpellAction POINT_UP;
    public static SpellAction POINT_DOWN;
    public static SpellAction SUMMON;
    public static SpellAction THRUST;
    public static SpellAction IMBUE;
    public static SpellAction GRAPPLE;
    public static SpellAction FLYING;
    public static SpellAction NONE;
    static List<SpellAction> spellActions = new ArrayList<>();

    static {
        POINT = new SpellAction(WizardryMainMod.location("point")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                float pitch = (float) Math.toRadians(entity.getXRot());
                float yaw = (float) Math.toRadians(entity.yHeadRot - entity.yBodyRot);

                float x = -((float) Math.PI / 2) + pitch + 0.2f;
                float y = (hand == InteractionHand.MAIN_HAND ? -0.25f : 0.25f) + yaw;

                if (hand == InteractionHand.MAIN_HAND) model.rightArm.setRotation(x, y, 0);
                else model.leftArm.setRotation(x, y, 0);
            }
        };

        POINT_UP = new SpellAction(WizardryMainMod.location("point_up")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                if (hand == InteractionHand.MAIN_HAND) model.rightArm.setRotation(-2.2f, 0.2F, 0);
                else model.leftArm.setRotation(-2.2f, -0.2F, 0);
            }
        };

        POINT_DOWN = new SpellAction(WizardryMainMod.location("point_down")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                float tick = Math.abs(entity.getUseItemRemainingTicks());
                float y = Math.min(0.4f + tick * 0.05f, 0.7f);
                if (hand == InteractionHand.MAIN_HAND) model.rightArm.setRotation(-0.2f, y, 0);
                else model.leftArm.setRotation(-0.2f, -y, 0);
            }
        };

        SUMMON = new SpellAction(WizardryMainMod.location("summon")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                float tick = Math.abs(entity.getUseItemRemainingTicks());
                float x = -Math.min(0.4f + tick * 0.2f, 2f);
                model.rightArm.setRotation(x, 1.2f, 0);
                model.leftArm.setRotation(x, -1.2f, 0);
            }
        };

        IMBUE = new SpellAction(WizardryMainMod.location("imbue")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                float tick = Math.abs(entity.getUseItemRemainingTicks());
                float z = Math.max(1.5f - tick * 0.1f, 0.8f);
                if (hand == InteractionHand.MAIN_HAND) model.rightArm.setRotation(-1.2f, -0.2f, z);
                else model.leftArm.setRotation(-1.2f, 0.2f, -z);

                InteractionHand opposite = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                if (!entity.getItemInHand(opposite).isEmpty()) {
                    if (hand == InteractionHand.OFF_HAND) model.rightArm.setRotation(-0.8f, 0.3f, 0);
                    else model.leftArm.setRotation(-0.8f, 0.3f, 0);
                }
            }
        };


        THRUST = new SpellAction(WizardryMainMod.location("thrust")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                if (hand == InteractionHand.MAIN_HAND) model.rightArm.setRotation(-1.2f, -0.6f, 0);
                else model.leftArm.setRotation(-1.2f, 0.6f, 0);

                InteractionHand opposite = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                if (entity.getItemInHand(opposite).isEmpty()) {
                    if (opposite == InteractionHand.MAIN_HAND) model.rightArm.setRotation(-1.2f, -0.6f, 0);
                    else model.leftArm.setRotation(-1.2f, 0.6f, 0);
                }
            }
        };

        GRAPPLE = new SpellAction(WizardryMainMod.location("grapple")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                if (!(entity instanceof Player player)) return;

                HitResult hit = player.getData(EBAttachments.WIZARD_DATA).getGrappleTarget();
                if (hit == null || hit.getType() == HitResult.Type.MISS) return;

                Vec3 target = hit.getLocation();
                if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
                    // The entity will have moved since the original hit position was recorded
                    target = GeometryUtil.getCentre(living);
                }

                Vec3 direction = target.subtract(player.getEyePosition(1));

                float pitch = (float) Mth.atan2(Math.sqrt(direction.x * direction.x + direction.z * direction.z), direction.y);
                float x = pitch - (float) Math.PI * 0.9f;
                float y = (float) -Math.toRadians(player.yBodyRot) - (float) Mth.atan2(direction.x, direction.z);
                y += hand == InteractionHand.MAIN_HAND ? -0.25f : 0.25f;

                if (hand == InteractionHand.MAIN_HAND) {
                    if (Math.abs(pitch) < 0.2f) y = model.rightArm.yRot;
                    model.rightArm.setRotation(x, y, 0);
                } else {
                    if (Math.abs(pitch) < 0.2f) y = model.leftArm.yRot;
                    model.leftArm.setRotation(x, y, 0);
                }
            }
        };

        FLYING = new SpellAction(WizardryMainMod.location("flying")) {
            @Override
            public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {
                // The renderer pitches the body horizontal; straight up in model space reads as forward in flight,
                // and the free arm is pinned at the side (trailing) instead of the walking swing — only legs animate
                if (hand == InteractionHand.MAIN_HAND) {
                    model.rightArm.setRotation(-(float) Math.PI + 0.2f, 0.2f, 0);
                    model.leftArm.setRotation(0, 0, 0);
                } else {
                    model.leftArm.setRotation(-(float) Math.PI + 0.2f, -0.2f, 0);
                    model.rightArm.setRotation(0, 0, 0);
                }
            }
        };

        NONE = new SpellAction(WizardryMainMod.location("none"));
    }

    public ResourceLocation location;


    public SpellAction(ResourceLocation location) {
        this.location = location;
        spellActions.add(this);
    }

    public static void register() {
    }

    public static @Nullable SpellAction get(ResourceLocation location) {
        for (SpellAction action : spellActions) {
            if (action.location.equals(location)) {
                return action;
            }
        }
        return null;
    }

    public void renderArms(LivingEntity entity, HumanoidModel<?> model, InteractionHand hand) {

    }

    public boolean shouldRender(LivingEntity entity, Spell spell, ItemStack stack, InteractionHand hand) {
        return spell != null && spell.getAction() != SpellAction.NONE && entity.isUsingItem();
    }
}
