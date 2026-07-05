package com.koomplo.wizardry.content.entity.construct;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.entity.ICustomHitbox;
import com.koomplo.wizardry.api.content.entity.construct.MagicConstructEntity;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.GeometryUtil;
import com.koomplo.wizardry.core.ClientSpellSoundManager;
import com.koomplo.wizardry.setup.registries.EBSounds;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Spherical force field that bounces entities and projectiles off its surface (1.12.2 EntityForcefield). */
public class ForcefieldConstruct extends MagicConstructEntity implements ICustomHitbox {

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(ForcefieldConstruct.class, EntityDataSerializers.FLOAT);

    /** Extra radius to search around the forcefield for incoming entities. */
    private static final double SEARCH_BORDER_SIZE = 4;

    private static final float BOUNCINESS = 0.2f;

    public ForcefieldConstruct(EntityType<?> type, Level level) {
        super(type, level);
    }

    public ForcefieldConstruct(Level level) {
        this(com.koomplo.wizardry.setup.registries.EBEntities.FORCEFIELD.get(), level);
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
        this.setBoundingBox(new AABB(getX() - radius, getY() - radius, getZ() - radius,
                getX() + radius, getY() + radius, getZ() + radius));
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RADIUS, 3f);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (RADIUS.equals(key)) setRadius(this.entityData.get(RADIUS));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount == 1 && level().isClientSide) {
            ClientSpellSoundManager.playMovingSound(this, EBSounds.ENTITY_FORCEFIELD_AMBIENT.get(),
                    SoundSource.PLAYERS, 0.5f, 1, true);
        }

        float radius = getRadius();

        // Searches for all entities near the forcefield and determines where they will be next tick.
        // If they will be inside the forcefield next tick, they bounce off with impact effects where they hit.
        List<Entity> targets = EntityUtil.getEntitiesWithinRadius(radius + SEARCH_BORDER_SIZE, getX(), getY(), getZ(), level(), Entity.class);

        targets.remove(this);
        targets.removeIf(t -> t instanceof ExperienceOrb); // Gets annoying since they're attracted to the player
        // TODO ring_defender (marco 6/7): deixa o dono atirar através do próprio campo

        for (Entity target : targets) {

            if (this.isValidTarget(target)) {

                Vec3 currentPos = Arrays.stream(GeometryUtil.getVertices(target.getBoundingBox()))
                        .min(Comparator.comparingDouble(v -> v.distanceTo(this.position())))
                        .orElse(target.position());

                double currentDistance = currentPos.distanceTo(this.position());

                // Estimate the target's position next tick (assumes the same vertex stays closest)
                Vec3 nextTickPos = currentPos.add(target.getDeltaMovement());
                double nextTickDistance = nextTickPos.distanceTo(this.position());

                boolean flag;

                if (EntityUtil.isLiving(target)) {
                    // Non-allied living entities shouldn't be inside at all
                    flag = nextTickDistance <= radius;
                } else {
                    // Non-living entities bounce off if they cross the surface within the next tick, either way
                    flag = (currentDistance > radius && nextTickDistance <= radius)
                            || (currentDistance < radius && nextTickDistance >= radius);
                }

                if (flag) {
                    // TODO ring_interdiction (marco 6/7): dano leve a cada repulsão

                    Vec3 targetRelativePos = currentPos.subtract(this.position());

                    double nudgeVelocity = this.contains(target) ? -0.1 : 0.1;
                    if (EntityUtil.isLiving(target)) nudgeVelocity = 0.25;
                    Vec3 extraVelocity = targetRelativePos.normalize().scale(nudgeVelocity);

                    if (!level().isClientSide) {
                        // ...make it bounce off!
                        Vec3 motion = target.getDeltaMovement();
                        target.setDeltaMovement(motion.x * -BOUNCINESS + extraVelocity.x,
                                motion.y * -BOUNCINESS + extraVelocity.y,
                                motion.z * -BOUNCINESS + extraVelocity.z);

                        // Prevents the forcefield bouncing things into the floor
                        if (target.onGround() && target.getDeltaMovement().y < 0) {
                            target.setDeltaMovement(target.getDeltaMovement().x, 0.1, target.getDeltaMovement().z);
                        }

                        // How far the target needs to move towards the centre (negative means away from the centre)
                        double distanceTowardsCentre = -(targetRelativePos.length() - radius) - (radius - nextTickDistance);
                        Vec3 targetNewPos = target.position().add(targetRelativePos.normalize().scale(distanceTowardsCentre));
                        target.setPos(targetNewPos.x, targetNewPos.y, targetNewPos.z);

                        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                                EBSounds.ENTITY_FORCEFIELD_DEFLECT.get(), SoundSource.PLAYERS, 0.3f, 1.3f);

                        // Player motion is handled on that player's client so needs packets
                        if (target instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
                        }

                        // Repeated blocking wears the forcefield down
                        if (this.tickCount % 5 == 0) {
                            this.lifetime = (int) (lifetime * 0.99);
                        }

                    } else {
                        // Prevents the impact visual being spammed when an allied player is inside (1.12.2 quirk kept)
                        if (target instanceof Player && target.position().distanceTo(this.position()) < radius) return;

                        Vec3 relativeImpactPos = targetRelativePos.normalize().scale(radius);

                        float yaw = (float) Math.atan2(relativeImpactPos.x, -relativeImpactPos.z);
                        float pitch = (float) Math.asin(relativeImpactPos.y / radius);

                        ParticleBuilder.create(EBParticles.FLASH).pos(this.position().add(relativeImpactPos))
                                .time(6).face((float) (yaw * 180 / Math.PI), (float) (pitch * 180 / Math.PI))
                                .color(0.9f, 0.95f, 1f).spawn(level());

                        for (int i = 0; i < 12; i++) {

                            float yaw1 = yaw + 0.3f * (random.nextFloat() - 0.5f) - (float) Math.PI / 2;
                            float pitch1 = pitch + 0.3f * (random.nextFloat() - 0.5f);

                            float brightness = random.nextFloat();

                            double r = radius + 0.05;
                            double x = this.getX() + r * Mth.cos(yaw1) * Mth.cos(pitch1);
                            double y = this.getY() + r * Mth.sin(pitch1);
                            double z = this.getZ() + r * Mth.sin(yaw1) * Mth.cos(pitch1);

                            ParticleBuilder.create(EBParticles.DUST).pos(x, y, z).time(6 + random.nextInt(6))
                                    .face((float) (yaw1 * 180 / Math.PI) + 90, (float) (pitch1 * 180 / Math.PI)).scale(1.5f)
                                    .color(0.7f + 0.3f * brightness, 0.85f + 0.15f * brightness, 1f).spawn(level());
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean contains(Vec3 vec) {
        return vec.distanceTo(this.position()) < getRadius(); // The surface counts as outside
    }

    /** Returns true if the given bounding box is completely inside this forcefield (the surface counts as outside). */
    public boolean contains(AABB box) {
        return Arrays.stream(GeometryUtil.getVertices(box)).allMatch(this::contains);
    }

    /** Returns true if the given entity is completely inside this forcefield (the surface counts as outside). */
    public boolean contains(Entity entity) {
        return contains(entity.getBoundingBox());
    }

    @Nullable
    @Override
    public Vec3 calculateIntercept(Vec3 origin, Vec3 endpoint, float fuzziness) {
        // Intercept between the line and the sphere: find the point where the line is closest to the centre,
        // then use a bit of geometry to find the intercept
        Vec3 line = endpoint.subtract(origin);
        double t = -origin.subtract(this.position()).dot(line) / line.lengthSqr();
        Vec3 closestPoint = origin.add(line.scale(t));
        double dsquared = closestPoint.distanceToSqr(this.position());
        double rsquared = Math.pow(getRadius() + fuzziness, 2);
        if (dsquared > rsquared) return null;
        return closestPoint.subtract(line.normalize().scale(Math.sqrt(rsquared - dsquared)));
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setRadius(nbt.getFloat("radius"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("radius", getRadius());
    }

    // ======= Cross-boundary protections (1.12.2 event handlers) =======

    @Nullable
    private static ForcefieldConstruct getSurroundingForcefield(Level world, Vec3 vec) {
        double searchRadius = 20;
        List<ForcefieldConstruct> forcefields = EntityUtil.getEntitiesWithinRadius(searchRadius, vec.x, vec.y, vec.z, world, ForcefieldConstruct.class);
        forcefields.removeIf(f -> !f.contains(vec));
        return forcefields.stream().min(Comparator.comparingDouble(f -> vec.distanceToSqr(f.position()))).orElse(null);
    }

    @Nullable
    private static ForcefieldConstruct getSurroundingForcefield(Level world, AABB box, Vec3 vec) {
        double searchRadius = 20;
        List<ForcefieldConstruct> forcefields = EntityUtil.getEntitiesWithinRadius(searchRadius, vec.x, vec.y, vec.z, world, ForcefieldConstruct.class);
        forcefields.removeIf(f -> !f.contains(box));
        return forcefields.stream().min(Comparator.comparingDouble(f -> vec.distanceToSqr(f.position()))).orElse(null);
    }

    @Nullable
    private static ForcefieldConstruct getSurroundingForcefield(Entity entity) {
        return getSurroundingForcefield(entity.level(), entity.getBoundingBox(), entity.position());
    }

    /** Cancels attacks that cross a forcefield boundary (either way). Hooked from the NeoForge game bus. */
    public static boolean shouldBlockAttack(@Nullable Entity attacker, @Nullable Entity victim, @Nullable Entity directSource) {
        if (attacker == null || victim == null) return false;
        if (directSource instanceof ForcefieldConstruct) return false; // Damage from a forcefield itself is fine
        // This is false if both entities are outside a forcefield or both are in the same one
        return getSurroundingForcefield(victim) != getSurroundingForcefield(attacker);
    }

    /** Cancels interactions that cross a forcefield boundary. Hooked from the NeoForge game bus. */
    public static boolean shouldBlockInteraction(Level world, Player player, AABB targetBox) {
        return getSurroundingForcefield(world, targetBox.getCenter())
                != getSurroundingForcefield(world, player.position());
    }

    /** Filters explosion effects so they don't cross forcefield boundaries. Hooked from the NeoForge game bus. */
    public static void filterExplosion(Level world, Vec3 explosionPos, List<net.minecraft.core.BlockPos> blocks, List<Entity> entities) {
        ForcefieldConstruct forcefield = getSurroundingForcefield(world, explosionPos);
        blocks.removeIf(p -> getSurroundingForcefield(world, Vec3.atCenterOf(p)) != forcefield);
        entities.removeIf(e -> getSurroundingForcefield(e) != forcefield);
    }
}
