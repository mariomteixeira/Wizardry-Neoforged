package com.koomplo.wizardry.content.spell.necromancy;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.content.entity.living.AbstractWizard;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.core.AllyDesignation;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MindControl extends RaySpell {

    /** The NBT tag name for storing the controlling entity's UUID in the target's persistent data. */
    public static final String NBT_KEY = "controllingEntity";

    @Override
    public boolean canCastByEntity() {
        return false;
    }

    @Override
    public boolean canCastByLocation() {
        return false;
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        Entity target = entityHit.getEntity();
        Level world = ctx.world();

        if (!EntityUtil.isLiving(target)) return false;

        if (!canControl(target)) {
            if (!world.isClientSide && ctx.caster() instanceof Player player) {
                // Adds a message saying that the player/boss entity/wizard resisted mind control
                player.displayClientMessage(Component.translatable("spell.resist", target.getName(),
                        this.getDescriptionFormatted()), true);
            }

        } else if (target instanceof Mob mob) {

            if (!world.isClientSide) {
                if (findMindControlTarget(mob, ctx.caster(), world) == null) {
                    // If no valid target was found, this just acts like mind trick
                    mob.setTarget(null);
                }
            }

            if (target instanceof Sheep sheep && sheep.getColor() == DyeColor.BLUE
                    && EntityUtil.canDamageBlocks(ctx.caster(), world)) {
                if (!world.isClientSide) sheep.setColor(DyeColor.RED); // Wololo!
                world.playSound(null, ctx.caster().getX(), ctx.caster().getY(), ctx.caster().getZ(),
                        SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.PLAYERS, 1, 1);
            }

            if (!world.isClientSide) startControlling(mob, ctx.caster(),
                    (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)));
        }

        if (world.isClientSide) {
            for (int i = 0; i < 10; i++) {
                ParticleBuilder.create(EBParticles.DARK_MAGIC, world.random, target.getX(),
                                target.getY() + target.getEyeHeight(), target.getZ(), 0.25, false)
                        .color(0.8f, 0.2f, 1.0f).spawn(world);
                ParticleBuilder.create(EBParticles.DARK_MAGIC, world.random, target.getX(),
                                target.getY() + target.getEyeHeight(), target.getZ(), 0.25, false)
                        .color(0.2f, 0.04f, 0.25f).spawn(world);
            }
        }

        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    /** Returns true if the given entity can be mind controlled (i.e. is not a player, npc, wizard or boss). */
    public static boolean canControl(Entity target) {
        // TODO config (marco 7): mindControlTargetsBlacklist do 1.12.2
        return target instanceof Mob && !target.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)
                && !(target instanceof Npc) && !(target instanceof AbstractWizard);
    }

    public static void startControlling(Mob target, LivingEntity controller, int duration) {
        if (!target.level().isClientSide) {
            target.getPersistentData().putUUID(NBT_KEY, controller.getUUID());
            target.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.MIND_CONTROL), duration, 0));
        }
    }

    /**
     * Finds the nearest creature to the given target which it is allowed to attack according to the given caster,
     * within the target's follow range. Returns it (already set as the target's attack target), or null if none.
     */
    @Nullable
    public static LivingEntity findMindControlTarget(Mob target, LivingEntity caster, Level world) {

        List<LivingEntity> possibleTargets = EntityUtil.getLivingWithinRadius(
                target.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
                target.getX(), target.getY(), target.getZ(), world);

        possibleTargets.remove(target);
        possibleTargets.remove(target.getVehicle());
        possibleTargets.removeIf(e -> e instanceof ArmorStand);

        LivingEntity newAITarget = null;

        for (LivingEntity possibleTarget : possibleTargets) {
            if (AllyDesignation.isValidTarget(caster, possibleTarget) && (newAITarget == null
                    || target.distanceTo(possibleTarget) < target.distanceTo(newAITarget))) {
                newAITarget = possibleTarget;
            }
        }

        if (newAITarget != null) {
            target.setTarget(newAITarget);
            return newAITarget;
        }

        return null;
    }

    /** Redirects targets chosen by mind-controlled creatures towards the controller's enemies. */
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        // The != null check prevents infinite loops with mind trick
        if (event.getNewAboutToBeSetTarget() == null) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.hasEffect(EBMobEffects.holder(EBMobEffects.MIND_CONTROL)) || !canControl(mob)) return;

        if (mob.getPersistentData().hasUUID(NBT_KEY)) {
            Entity caster = EntityUtil.getEntityByUUID(mob.level(), mob.getPersistentData().getUUID(NBT_KEY));

            if (caster instanceof LivingEntity livingCaster) {
                // If the chosen target is already a valid mind control target, nothing happens
                if (AllyDesignation.isValidTarget(livingCaster, event.getNewAboutToBeSetTarget())) return;

                // Otherwise, look for the nearest valid one (or none)
                List<LivingEntity> possibleTargets = EntityUtil.getLivingWithinRadius(
                        mob.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
                        mob.getX(), mob.getY(), mob.getZ(), mob.level());

                possibleTargets.remove(mob);
                possibleTargets.remove(mob.getVehicle());
                possibleTargets.removeIf(e -> e instanceof ArmorStand);

                LivingEntity newAITarget = null;
                for (LivingEntity possibleTarget : possibleTargets) {
                    if (AllyDesignation.isValidTarget(livingCaster, possibleTarget) && (newAITarget == null
                            || mob.distanceTo(possibleTarget) < mob.distanceTo(newAITarget))) {
                        newAITarget = possibleTarget;
                    }
                }

                event.setNewAboutToBeSetTarget(newAITarget);
                return;
            }
        }

        // If the caster couldn't be found, this just acts like mind trick
        event.setNewAboutToBeSetTarget(null);
    }

    /** Periodically re-targets mind-controlled creatures that have no living target. */
    public static void onLivingTick(com.koomplo.wizardry.api.content.event.EBLivingTick event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity().tickCount % 50 != 0) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!mob.hasEffect(EBMobEffects.holder(EBMobEffects.MIND_CONTROL)) || !canControl(mob)) return;

        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            if (mob.getPersistentData().hasUUID(NBT_KEY)) {
                Entity caster = EntityUtil.getEntityByUUID(mob.level(), mob.getPersistentData().getUUID(NBT_KEY));
                if (caster instanceof LivingEntity livingCaster && findMindControlTarget(mob, livingCaster, mob.level()) != null) {
                    return;
                }
            }
            mob.setTarget(null);
        }
    }

    /** Clears the victim's targets when the effect ends. */
    public static void onEffectEnd(net.neoforged.neoforge.event.entity.living.MobEffectEvent event) {
        if (event.getEffectInstance() != null
                && event.getEffectInstance().getEffect() == EBMobEffects.holder(EBMobEffects.MIND_CONTROL)
                && event.getEntity() instanceof Mob mob) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.NECROMANCY, SpellType.ATTACK, SpellAction.POINT, 40, 10, 150)
                .add(DefaultProperties.RANGE, 8f)
                .add(DefaultProperties.EFFECT_DURATION, 600)
                .build();
    }
}
