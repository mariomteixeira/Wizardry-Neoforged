package com.binaris.wizardry.content.entity.construct;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.entity.construct.MagicConstructEntity;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.core.networking.s2c.ScreenShakeS2C;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.EBSounds;
import com.binaris.wizardry.setup.registries.Spells;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Lightning hammer that falls from the sky, zaps nearby enemies and vanishes (1.12.2 EntityHammer). */
public class HammerConstruct extends MagicConstructEntity {

    private static final EntityDataAccessor<Boolean> SPIN =
            SynchedEntityData.defineId(HammerConstruct.class, EntityDataSerializers.BOOLEAN);

    /** How long the hammer has been falling for. */
    public int fallTime;

    public HammerConstruct(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false; // Unlike other constructs, the hammer has real physics
    }

    public HammerConstruct(Level level) {
        this(EBEntities.LIGHTNING_HAMMER_ENTITY.get(), level);
    }

    public boolean isSpinning() {
        return entityData.get(SPIN);
    }

    public void setSpinning(boolean spin) {
        entityData.set(SPIN, spin);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPIN, false);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void tick() {
        // Lifetime end goes through despawn() so the explosion effect plays (the base class discards silently)
        if (this.tickCount > lifetime && lifetime != -1) {
            this.despawn();
            return;
        }

        super.tick();

        if (level().isClientSide && this.tickCount % 3 == 0) {
            ParticleBuilder.create(EBParticles.SPARK)
                    .pos(getX() - 0.5d + random.nextDouble(), getY() + 2 * random.nextDouble(), getZ() - 0.5d + random.nextDouble())
                    .spawn(level());
        }

        this.xOld = getX();
        this.yOld = getY();
        this.zOld = getZ();
        ++this.fallTime;
        this.setDeltaMovement(getDeltaMovement().add(0, -0.04, 0));
        this.move(MoverType.SELF, getDeltaMovement());
        this.setDeltaMovement(getDeltaMovement().scale(0.98));

        if (this.onGround()) {

            Vec3 motion = getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.7, motion.y * -0.5, motion.z * 0.7);

            this.setXRot(0);
            this.setSpinning(false);

            double seekerRange = Spells.LIGHTNING_HAMMER.property(DefaultProperties.EFFECT_RADIUS);

            List<LivingEntity> targets = EntityUtil.getLivingWithinRadius(seekerRange, getX(), getY() + 1, getZ(), level());

            int maxTargets = Spells.LIGHTNING_HAMMER.property(DefaultProperties.SECONDARY_MAX_TARGETS);
            while (targets.size() > maxTargets) targets.remove(targets.size() - 1);

            for (LivingEntity target : targets) {

                if (EntityUtil.isLiving(target) && this.isValidTarget(target)
                        && target.tickCount % Spells.LIGHTNING_HAMMER.property(DefaultProperties.ATTACK_INTERVAL) == 0) {

                    if (level().isClientSide) {
                        ParticleBuilder.create(EBParticles.LIGHTNING).pos(getX(), getY() + getBbHeight() - 0.1, getZ())
                                .target(target).spawn(level());
                        ParticleBuilder.spawnShockParticles(level(), target.getX(),
                                target.getY() + target.getBbHeight(), target.getZ());
                    }

                    target.playSound(EBSounds.ENTITY_HAMMER_ATTACK.get(), 1.0F, random.nextFloat() * 0.4F + 1.5F);

                    float damage = Spells.LIGHTNING_HAMMER.property(DefaultProperties.SPLASH_DAMAGE) * damageMultiplier;

                    if (this.getCaster() != null) {
                        EntityUtil.attackEntityWithoutKnockback(target,
                                MagicDamageSource.causeIndirectMagicDamage(this, getCaster(), EBDamageSources.SHOCK), damage);
                        EntityUtil.applyStandardKnockback(this, target, 0.4f);
                    } else {
                        target.hurt(damageSources().magic(), damage);
                    }
                }
            }

        } else {

            if (isSpinning()) this.setXRot(this.getXRot() + 15);

            List<Entity> collided = level().getEntities(this, this.getBoundingBox(), e -> e instanceof LivingEntity);

            float damage = Spells.LIGHTNING_HAMMER.property(DefaultProperties.DIRECT_DAMAGE) * damageMultiplier;

            for (Entity entity : collided) {
                entity.hurt(MagicDamageSource.causeIndirectMagicDamage(this, getCaster(), EBDamageSources.SHOCK), damage);
            }
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        if (onGround && this.fallDistance > 0) {

            if (level().isClientSide) {
                BlockState below = level().getBlockState(BlockPos.containing(getX(), getY() - 0.5, getZ()).below());
                for (int i = 0; i < 40; i++) {
                    double particleX = getX() - 1.0d + 2 * random.nextDouble();
                    double particleZ = getZ() - 1.0d + 2 * random.nextDouble();
                    if (!below.isAir()) {
                        level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, below),
                                particleX, getY(), particleZ, particleX - getX(), 0, particleZ - getZ());
                    }
                }
            } else {
                // Only if the hammer has actually fallen from the sky, rather than the block under it being broken
                if (this.fallDistance > 10) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
                    if (bolt != null) {
                        bolt.moveTo(getX(), getY(), getZ());
                        level().addFreshEntity(bolt);
                    }

                    EntityUtil.getEntitiesWithinRadius(10, getX(), getY(), getZ(), level(), ServerPlayer.class)
                            .forEach(p -> Services.NETWORK_HELPER.sendTo(p, new ScreenShakeS2C(6f, 18)));
                }

                this.playSound(EBSounds.ENTITY_HAMMER_LAND.get(), 1.0F, 0.6f);
            }
        }

        super.checkFallDamage(y, onGround, state, pos);
    }

    @Override
    public void despawn() {
        this.playSound(EBSounds.ENTITY_HAMMER_EXPLODE.get(), 1.0F, 1.0f);

        if (level().isClientSide) {
            level().addParticle(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 0, 0, 0);
        }

        super.despawn();
    }

    // TODO ring_hammer (marco 6/7): dono com o anel e mao vazia pega o martelo como arma

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fallTime = tag.getByte("Time") & 255;
        setSpinning(tag.getBoolean("Spin"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Time", (byte) this.fallTime);
        tag.putBoolean("Spin", isSpinning());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
