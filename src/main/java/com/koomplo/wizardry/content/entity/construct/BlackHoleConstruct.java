package com.koomplo.wizardry.content.entity.construct;

import com.koomplo.wizardry.api.content.entity.construct.ScaledConstructEntity;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.EBSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Black hole that sucks entities in and crushes whatever reaches the centre (1.12.2 EntityBlackHole). */
public class BlackHoleConstruct extends ScaledConstructEntity {

    private static final double SUCTION_STRENGTH = 0.075;

    /** Client-visual randomisers for the ray renderer; each side generates its own, like 1.12.2. */
    public int[] randomiser;
    public int[] randomiser2;

    public BlackHoleConstruct(EntityType<?> type, Level level) {
        super(type, level);
        randomiser = new int[30];
        for (int i = 0; i < randomiser.length; i++) randomiser[i] = this.random.nextInt(10);
        randomiser2 = new int[30];
        for (int i = 0; i < randomiser2.length; i++) randomiser2[i] = this.random.nextInt(10);
    }

    public BlackHoleConstruct(Level level) {
        this(EBEntities.BLACK_HOLE.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        // Particle effect finishes 40 ticks before the end so the particles disappear at the same time
        if (level().isClientSide && this.tickCount + 40 < this.lifetime) {
            for (int i = 0; i < 5; i++) {
                level().addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5D) * 4.0D, (random.nextDouble() - 0.5D) * 4.0D - 1,
                        (random.nextDouble() - 0.5D) * 4.0D);
            }
        }

        if (this.lifetime - this.tickCount == 75) {
            this.playSound(EBSounds.ENTITY_BLACK_HOLE_VANISH.get(), 1.5f, 1.0f);
        } else if (this.tickCount % 80 == 1 && this.tickCount + 80 < this.lifetime) {
            this.playSound(EBSounds.ENTITY_BLACK_HOLE_AMBIENT.get(), 1.5f, 1.0f);
        }

        if (!level().isClientSide) {

            double radius = 2 * getBbHeight() * getSizeMultiplier();

            // TODO charm_black_hole (marco 6/7): suga blocos soltos também (EntityLevitatingBlock)

            List<Entity> targets = EntityUtil.getEntitiesWithinRadius(radius, getX(), getY(), getZ(), level(), Entity.class);

            targets.removeIf(t -> !(t instanceof LivingEntity));

            for (Entity target : targets) {

                if (this.isValidTarget(target)) {

                    // If the target can't be moved, it isn't sucked in but is still damaged if it gets too close
                    boolean anchored = target instanceof Player player
                            && ((getCaster() instanceof Player && !EBServerConfig.PLAYERS_MOVE_EACH_OTHER.get())
                            || ArtifactChannel.isEquipped(player, EBItems.AMULET_ANCHORING.get()));

                    if (!anchored) {

                        EntityUtil.undoGravity(target);

                        // Sucks the target in
                        Vec3 motion = target.getDeltaMovement();
                        double mx = motion.x, my = motion.y, mz = motion.z;

                        if (this.getX() > target.getX() && mx < 1) mx += SUCTION_STRENGTH;
                        else if (this.getX() < target.getX() && mx > -1) mx -= SUCTION_STRENGTH;

                        if (this.getY() > target.getY() && my < 1) my += SUCTION_STRENGTH;
                        else if (this.getY() < target.getY() && my > -1) my -= SUCTION_STRENGTH;

                        if (this.getZ() > target.getZ() && mz < 1) mz += SUCTION_STRENGTH;
                        else if (this.getZ() < target.getZ() && mz > -1) mz -= SUCTION_STRENGTH;

                        target.setDeltaMovement(mx, my, mz);

                        // Player motion is handled on that player's client so needs packets
                        if (target instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
                        }
                    }

                    if (this.distanceTo(target) <= 2) {
                        // Damages the target if it is close enough
                        if (this.getCaster() != null) {
                            target.hurt(MagicDamageSource.causeIndirectMagicDamage(this, getCaster(), EBDamageSources.MAGIC),
                                    2 * damageMultiplier);
                        } else {
                            target.hurt(damageSources().magic(), 2 * damageMultiplier);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("randomiser")) randomiser = tag.getIntArray("randomiser");
        if (tag.contains("randomiser2")) randomiser2 = tag.getIntArray("randomiser2");
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putIntArray("randomiser", randomiser);
        tag.putIntArray("randomiser2", randomiser2);
    }
}
