package com.binaris.wizardry.content.spell.earth;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.EntityCastContext;
import com.binaris.wizardry.api.content.spell.internal.LocationCastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.api.content.util.GeometryUtil;
import com.binaris.wizardry.api.content.util.RayTracer;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.core.integrations.ArtifactChannel;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Grapple extends Spell {

    /** The speed at which the vine extends/retracts from the caster, in blocks per tick. */
    public static final SpellProperty<Float> EXTENSION_SPEED = SpellProperty.floatProperty("extension_speed");
    /** The speed at which the vine reels in the caster or target, in blocks per tick. */
    public static final SpellProperty<Float> REEL_SPEED = SpellProperty.floatProperty("reel_speed");

    /** The distance from the target position at which the spell will stop reeling in entities. */
    private static final double MINIMUM_REEL_DISTANCE = 3;
    /** The acceleration with which the vine reels in the caster or target. */
    private static final double REEL_ACCELERATION = 0.3;
    /** The speed at which the caster or target is lowered when the caster is sneaking. */
    private static final double PAYOUT_SPEED = 0.25;
    /** Once attached, the vine can stretch beyond the maximum range by this factor before breaking. */
    private static final double STRETCH_LIMIT = 1.5;
    /** The distance between spawned particles. */
    private static final double PARTICLE_SPACING = 1.5;
    /** The maximum jitter (random position offset) for spawned particles. */
    private static final double PARTICLE_JITTER = 0.04;
    /** The vertical offset from the caster's eye position to the visual origin of the vine (same as RaySpell). */
    private static final double Y_OFFSET = 0.25;

    @Override
    public boolean cast(PlayerCastContext ctx) {
        Player caster = ctx.caster();
        Level world = ctx.world();
        WizardDataHolder data = caster.getData(EBAttachments.WIZARD_DATA);

        Vec3 origin = caster.getEyePosition(1);

        float extensionSpeed = property(EXTENSION_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY);
        float range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

        // castingTicks == 0 always re-targets, so a stored hit from an interrupted cast can never go stale
        // (1.12.2 achieved this with a WizardData variable ticker)
        HitResult hit = ctx.castingTicks() == 0 ? null : data.getGrappleTarget();

        // Initial targeting
        if (hit == null) {
            hit = findTarget(world, caster, origin, caster.getLookAngle(), range);
            data.setGrappleTarget(hit);
            caster.swing(ctx.hand());
            // This condition prevents the sound playing every tick after a missed shot has finished extending
            if (hit.getType() != HitResult.Type.MISS || ctx.castingTicks() * extensionSpeed < range) {
                this.playSound(world, caster, "shoot");
            }
        }

        // Dead or removed entity targets end the spell
        if (hit instanceof EntityHitResult entityHit && !entityHit.getEntity().isAlive()) {
            data.setGrappleTarget(null);
            return false;
        }

        Vec3 target = hit.getLocation();

        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity) {
            // If the target is an entity, we need to use the entity's centre rather than the original hit position
            // because the entity will have moved!
            target = GeometryUtil.getCentre(entityHit.getEntity());
        }

        double distance = origin.distanceTo(target);
        Vec3 direction = target.subtract(origin).normalize();

        double maxLength = range * STRETCH_LIMIT;

        // If the vine stretched too far
        if (distance > maxLength) {
            if (world.isClientSide && (ctx.castingTicks() - 1) * extensionSpeed < distance) {
                spawnLeafParticles(world, origin.subtract(0, Y_OFFSET, 0), direction, distance);
            }
            data.setGrappleTarget(null);
            return false; // The spell is finished
        }

        boolean extending = ctx.castingTicks() * extensionSpeed < distance;

        if (extending) {
            // Extension
            if (world.isClientSide) {
                // world.getGameTime() - castingTicks generates a constant but unique seed each time the spell is cast
                ParticleBuilder.create(EBParticles.VINE).entity(caster).pos(0, caster.getEyeHeight() - Y_OFFSET, 0)
                        .target(origin.add(direction.scale(ctx.castingTicks() * extensionSpeed))).tvel(direction.scale(extensionSpeed))
                        .seed(world.getGameTime() - ctx.castingTicks()).spawn(world);
            }

        } else {
            // Retraction
            Vec3 velocity = direction.scale(property(REEL_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY));

            int retractTime = ctx.castingTicks() - (int) (distance / extensionSpeed);

            switch (hit.getType()) {

                case BLOCK -> {
                    // Payout
                    if (caster.isShiftKeyDown() && ArtifactChannel.isEquipped(caster, EBItems.CHARM_ABSEILING.get()))
                        velocity = new Vec3(velocity.x, distance < maxLength - 1 ? -PAYOUT_SPEED : distance - maxLength + 1, velocity.z);

                    // Reel the caster towards the block hit
                    Vec3 motion = caster.getDeltaMovement();
                    caster.addDeltaMovement(new Vec3((velocity.x - motion.x) * REEL_ACCELERATION,
                            (velocity.y - motion.y) * REEL_ACCELERATION, (velocity.z - motion.z) * REEL_ACCELERATION));

                    // Reset fall distance if the caster moves upwards
                    // TODO config (marco 7): 1.12.2 pulava este reset com replaceVanillaFallDamage (sistema que o port não tem)
                    if (caster.getDeltaMovement().y > 0) caster.fallDistance = 0;

                    if (world.isClientSide) {
                        ParticleBuilder.create(EBParticles.VINE).entity(caster).pos(0, caster.getEyeHeight() - Y_OFFSET, 0)
                                .target(target).seed(world.getGameTime() - ctx.castingTicks()).spawn(world);
                    }

                    if (retractTime == 1) { // Just hit
                        this.playSound(world, caster, "pull");
                        this.playSound(world, hit.getLocation(), "attach");
                    }
                }

                case ENTITY -> {
                    Entity entity = ((EntityHitResult) hit).getEntity();

                    // Payout
                    if (caster.isShiftKeyDown() && ArtifactChannel.isEquipped(caster, EBItems.CHARM_ABSEILING.get()))
                        velocity = new Vec3(velocity.x, distance < maxLength - 1 ? PAYOUT_SPEED : maxLength - 1 - distance, velocity.z);

                    // Reel the entity hit towards the caster
                    if (distance > MINIMUM_REEL_DISTANCE) {
                        reelEntity(entity, velocity);
                    }

                    if (world.isClientSide) {
                        ParticleBuilder.create(EBParticles.VINE).entity(caster).pos(0, caster.getEyeHeight() - Y_OFFSET, 0)
                                .target(entity).seed(world.getGameTime() - ctx.castingTicks()).spawn(world);
                    }

                    if (retractTime == 1) { // Just hit
                        this.playSound(world, caster, "pull");
                        this.playSound(world, entity.position(), "attach");
                    }
                }

                default -> {
                    // Missed
                    if (world.isClientSide && (ctx.castingTicks() - 1) * extensionSpeed < distance) {
                        spawnLeafParticles(world, origin.subtract(0, Y_OFFSET, 0), direction, distance);
                    }
                    data.setGrappleTarget(null);
                    return false; // The spell is finished
                }
            }
        }

        return true;
    }

    @Override
    public boolean cast(EntityCastContext ctx) {
        LivingEntity target = ctx.target();
        if (target == null) return false;

        Level world = ctx.world();
        LivingEntity caster = ctx.caster();

        Vec3 origin = caster.getEyePosition(1);

        // If the target is an entity, we need to use the entity's centre rather than the original hit position
        // because the entity will have moved!
        Vec3 targetVec = GeometryUtil.getCentre(target);

        float range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

        HitResult hit = findTarget(world, caster, origin, targetVec.subtract(origin).normalize(), range);

        if (!(hit instanceof EntityHitResult entityHit) || entityHit.getEntity() != target)
            return false; // Something was in the way

        double distance = origin.distanceTo(targetVec);

        // Can't cast the spell at all if the target is too far away
        if (ctx.castingTicks() <= 1 && distance > range) return false;

        Vec3 vec = targetVec.subtract(origin).normalize();

        float extensionSpeed = property(EXTENSION_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY);

        // If the vine stretched too far
        if (distance > range * STRETCH_LIMIT) {
            if (world.isClientSide && (ctx.castingTicks() - 1) * extensionSpeed < distance) {
                spawnLeafParticles(world, origin.subtract(0, Y_OFFSET, 0), vec, distance);
            }
            return false;
        }

        Vec3 hookPosition;

        if (ctx.castingTicks() * extensionSpeed < distance) {
            // Extension
            hookPosition = origin.add(vec.scale(ctx.castingTicks() * extensionSpeed));

        } else {
            // Retraction
            Vec3 velocity = vec.scale(property(REEL_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY));

            if (distance > MINIMUM_REEL_DISTANCE) {
                reelEntity(target, velocity);
            }

            hookPosition = targetVec;
        }

        if (world.isClientSide) {
            ParticleBuilder.create(EBParticles.VINE).pos(origin).target(hookPosition).tvel(vec.scale(extensionSpeed))
                    .seed(world.getGameTime() - ctx.castingTicks()).spawn(world);
        }

        return true;
    }

    @Override
    public boolean cast(LocationCastContext ctx) {
        Level world = ctx.world();
        Vec3 origin = ctx.vec3();
        Vec3 direction = new Vec3(ctx.direction().step());

        float range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

        HitResult result = findTarget(world, null, origin, direction, range);

        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity entity) {

            Vec3 target = GeometryUtil.getCentre(entity);

            double distance = origin.distanceTo(target);
            Vec3 vec = target.subtract(origin).normalize();

            float extensionSpeed = property(EXTENSION_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY);

            // If the vine stretched too far
            if (distance > range * STRETCH_LIMIT) {
                if (world.isClientSide && (ctx.castingTicks() - 1) * extensionSpeed < distance) {
                    spawnLeafParticles(world, origin.subtract(0, Y_OFFSET, 0), vec, distance);
                }
                return false;
            }

            Vec3 hookPosition;

            if (ctx.castingTicks() * extensionSpeed < distance) {
                // Extension
                hookPosition = origin.add(vec.scale(ctx.castingTicks() * extensionSpeed));

            } else {
                // Retraction
                Vec3 velocity = vec.scale(property(REEL_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY));

                if (distance > MINIMUM_REEL_DISTANCE) {
                    reelEntity(entity, velocity);
                }

                hookPosition = target;
            }

            if (world.isClientSide) {
                ParticleBuilder.create(EBParticles.VINE).pos(origin).target(hookPosition)
                        .seed(world.getGameTime() - ctx.castingTicks()).spawn(world);
            }

            return true;
        }

        return false;
    }

    @Override
    public void endCast(CastContext ctx) {
        Vec3 origin = null;
        Vec3 target = null;

        LivingEntity caster = ctx.caster();

        if (caster != null) {

            origin = caster.getEyePosition(1);

            if (caster instanceof Player player) {
                WizardDataHolder data = player.getData(EBAttachments.WIZARD_DATA);
                HitResult hit = data.getGrappleTarget();
                if (hit != null) target = hit.getLocation();
                data.setGrappleTarget(null);
            } else if (caster instanceof Mob mob && mob.getTarget() != null) {
                target = GeometryUtil.getCentre(mob.getTarget());
            }

            this.playSound(ctx.world(), caster, "release");

        } else if (ctx instanceof LocationCastContext locationCtx) {

            origin = locationCtx.vec3();
            Vec3 direction = new Vec3(locationCtx.direction().step());
            float range = property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);
            target = findTarget(ctx.world(), null, origin, direction, range).getLocation();

            this.playSound(ctx.world(), origin, "release");
        }

        if (ctx.world().isClientSide && origin != null && target != null) {
            Vec3 direction = target.subtract(origin).normalize();
            float extensionSpeed = property(EXTENSION_SPEED) * ctx.modifiers().get(SpellModifiers.POTENCY);
            double distance = Math.min(target.subtract(origin).length(), ctx.castingTicks() * extensionSpeed);
            spawnLeafParticles(ctx.world(), origin, direction, distance);
        }
    }

    /** Reels the given entity towards the vine's origin; pulled players need a velocity packet like any knockback. */
    private static void reelEntity(Entity entity, Vec3 velocity) {
        Vec3 motion = entity.getDeltaMovement();
        entity.addDeltaMovement(new Vec3((-velocity.x - motion.x) * REEL_ACCELERATION,
                (-velocity.y - motion.y) * REEL_ACCELERATION, (-velocity.z - motion.z) * REEL_ACCELERATION));

        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(entity));
        }
    }

    private void spawnLeafParticles(Level world, Vec3 origin, Vec3 direction, double distance) {
        for (double d = PARTICLE_SPACING; d <= distance; d += PARTICLE_SPACING) {
            double x = origin.x + d * direction.x;
            double y = origin.y + d * direction.y;
            double z = origin.z + d * direction.z;
            ParticleBuilder.create(EBParticles.LEAF, world.random, x, y, z, PARTICLE_JITTER, true)
                    .time(25 + world.random.nextInt(5)).spawn(world);
        }
    }

    private HitResult findTarget(Level world, @Nullable LivingEntity caster, Vec3 origin, Vec3 direction, double range) {
        Vec3 endpoint = origin.add(direction.scale(range));

        HitResult result = RayTracer.rayTrace(world, caster, origin, endpoint, 0, false,
                Entity.class, RayTracer.ignoreEntityFilter(caster));

        // Immovable entities count as misses, but the endpoint is the hit location instead
        // (non-solid blocks already pass through: the clip uses COLLIDER shapes, and a clip miss keeps the endpoint)
        if (result instanceof EntityHitResult entityHit && !entityHit.getEntity().isPushable()) {
            return BlockHitResult.miss(result.getLocation(), Direction.DOWN, BlockPos.containing(endpoint));
        }

        return result;
    }

    private void playSound(Level world, LivingEntity entity, String suffix) {
        if (!entity.isSilent()) playSound(world, entity.getX(), entity.getY(), entity.getZ(), suffix);
    }

    private void playSound(Level world, Vec3 pos, String suffix) {
        playSound(world, pos.x, pos.y, pos.z, suffix);
    }

    private void playSound(Level world, double x, double y, double z, String suffix) {
        SoundEvent sound = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                getLocation().getNamespace(), "spell." + getLocation().getPath() + "." + suffix));
        world.playSound(null, x, y, z, sound, SoundSource.PLAYERS, getVolume(),
                getPitch() + getPitchVariation() * (world.random.nextFloat() - 0.5f));
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    @Override
    public boolean canCastByEntity() {
        return true;
    }

    @Override
    public boolean canCastByLocation() {
        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.EARTH, SpellType.UTILITY, SpellAction.GRAPPLE, 5, 0, 0)
                .add(DefaultProperties.RANGE, 20f)
                .add(EXTENSION_SPEED, 3.5f)
                .add(REEL_SPEED, 1f)
                .build();
    }
}
