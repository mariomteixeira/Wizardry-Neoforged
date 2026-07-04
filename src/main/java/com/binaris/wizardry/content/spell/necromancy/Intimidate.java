package com.binaris.wizardry.content.spell.necromancy;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.event.EBLivingTick;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.AreaEffectSpell;
import com.binaris.wizardry.content.spell.abstr.BuffSpell;
import com.binaris.wizardry.setup.registries.EBMobEffects;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Intimidate extends AreaEffectSpell {

    /** The NBT tag name for storing the feared entity's UUID in the target's tag compound. */
    public static final String NBT_KEY = "fearedEntity";

    // These aren't spell properties because they're part of the actual potion effect, not the spell itself.
    private static final double BASE_AVOID_DISTANCE = 16;
    private static final double AVOID_DISTANCE_PER_LEVEL = 4;

    public Intimidate() {
        super();
        this.alwaysSucceed(true);
    }

    @Override
    public boolean canCastByLocation() {
        return false;
    }

    @Override
    protected boolean affectEntity(CastContext ctx, Vec3 origin, LivingEntity target, int targetCount) {
        if (ctx.caster() != null && target instanceof PathfinderMob creature) {
            int bonusAmplifier = BuffSpell.getStandardBonusAmplifier(ctx.modifiers().get(SpellModifiers.POTENCY));

            if (!ctx.world().isClientSide) {
                CompoundTag entityNBT = creature.getPersistentData();
                entityNBT.putUUID(NBT_KEY, ctx.caster().getUUID());

                creature.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.FEAR),
                        (int) (property(DefaultProperties.EFFECT_DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)),
                        property(DefaultProperties.EFFECT_STRENGTH) + bonusAmplifier));
            }
        }
        return true;
    }

    @Override
    protected void spawnParticleEffect(CastContext ctx, Vec3 origin, double radius) {
        if (ctx.caster() != null) origin = ctx.caster().getEyePosition();

        for (int i = 0; i < 30; i++) {
            double x = origin.x - 1 + ctx.world().random.nextDouble() * 2;
            double y = origin.y - 0.25 + ctx.world().random.nextDouble() * 0.5;
            double z = origin.z - 1 + ctx.world().random.nextDouble() * 2;
            ParticleBuilder.create(EBParticles.DARK_MAGIC).pos(x, y, z).color(0.9f, 0.1f, 0f).spawn(ctx.world());
        }
    }

    /**
     * Finds a random position away from the caster and paths the target there. Used by the fear effect tick below.
     */
    public static boolean runAway(PathfinderMob target, LivingEntity caster, double distance) {
        if (target.distanceTo(caster) < distance) {
            Vec3 away = DefaultRandomPos.getPosAway(target, (int) distance, (int) (distance / 2), caster.position());

            if (away == null) return false;

            // Don't change direction every tick unless the current path leads towards the caster
            boolean flag = true;
            if (!target.getNavigation().isDone() && target.getNavigation().getPath() != null) {
                Node point = target.getNavigation().getPath().getEndNode();
                if (point != null) flag = caster.distanceToSqr(point.x, point.y, point.z) < distance * distance;
            }
            // Has a built-in mind trick effect because for whatever reason this makes it work with skeletons.
            target.setTarget(null);

            if (flag) return target.getNavigation().moveTo(away.x, away.y, away.z, 1.25);
        }
        return false;
    }

    public static void onLivingTick(EBLivingTick event) {
        LivingEntity entity = event.getEntity();

        // No need to do this every tick either
        if (entity.tickCount % 50 == 0 && entity instanceof PathfinderMob creature
                && creature.hasEffect(EBMobEffects.holder(EBMobEffects.FEAR))) {

            CompoundTag entityNBT = creature.getPersistentData();
            if (entityNBT.hasUUID(NBT_KEY)) {
                Entity caster = EntityUtil.getEntityByUUID(creature.level(), entityNBT.getUUID(NBT_KEY));

                if (caster instanceof LivingEntity living) {
                    MobEffectInstance fear = creature.getEffect(EBMobEffects.holder(EBMobEffects.FEAR));
                    double distance = BASE_AVOID_DISTANCE + AVOID_DISTANCE_PER_LEVEL * (fear == null ? 0 : fear.getAmplifier());
                    runAway(creature, living, distance);
                }
            }
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.NECROMANCY, SpellType.ALTERATION, SpellAction.SUMMON, 20, 0, 100)
                .add(DefaultProperties.EFFECT_RADIUS, 8)
                .add(DefaultProperties.EFFECT_DURATION, 600)
                .add(DefaultProperties.EFFECT_STRENGTH, 0)
                .build();
    }
}
