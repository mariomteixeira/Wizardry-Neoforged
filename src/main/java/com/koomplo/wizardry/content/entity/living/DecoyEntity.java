package com.koomplo.wizardry.content.entity.living;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Illusory copy of the caster that wanders around drawing aggro (1.12.2 EntityDecoy). The caster UUID is
 * synched so the client renderer can use the caster's skin.
 */
public class DecoyEntity extends PathfinderMob {

    private static final EntityDataAccessor<Optional<UUID>> CASTER_UUID =
            SynchedEntityData.defineId(DecoyEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public int lifetime = 600;

    public DecoyEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public DecoyEntity(Level level) {
        this(EBEntities.DECOY.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    public void setCaster(@Nullable LivingEntity caster) {
        this.entityData.set(CASTER_UUID, Optional.ofNullable(caster == null ? null : caster.getUUID()));
    }

    @Nullable
    public LivingEntity getCaster() {
        return this.entityData.get(CASTER_UUID)
                .map(uuid -> EntityUtil.getEntityByUUID(level(), uuid))
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .orElse(null);
    }

    @Nullable
    public UUID getCasterUUID() {
        return this.entityData.get(CASTER_UUID).orElse(null);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CASTER_UUID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        // Decoys just wander around aimlessly, watching anything living
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, LivingEntity.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (this.tickCount > lifetime || this.getCaster() == null || !this.getCaster().isAlive()) {
                this.discard();
            }
        }
    }

    @Override
    public void onClientRemoval() {
        // Dissolves into dust when it goes away (1.12.2 onDespawn)
        for (int i = 0; i < 20; i++) {
            ParticleBuilder.create(EBParticles.DUST)
                    .pos(this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + this.random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth())
                    .time(40)
                    .color(0.2f, 1.0f, 0.8f)
                    .shaded(true)
                    .spawn(level());
        }
    }

    @Override
    public boolean isShiftKeyDown() {
        return false; // Decoys can't sneak
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("casterUUID")) this.entityData.set(CASTER_UUID, Optional.of(tag.getUUID("casterUUID")));
        this.lifetime = tag.getInt("lifetime");
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID uuid = getCasterUUID();
        if (uuid != null) tag.putUUID("casterUUID", uuid);
        tag.putInt("lifetime", lifetime);
    }
}
