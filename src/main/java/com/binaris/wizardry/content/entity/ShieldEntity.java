package com.binaris.wizardry.content.entity;

import com.binaris.wizardry.api.content.item.ICastItem;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.EBSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Force shield hovering in front of the caster while the shield spell is held (1.12.2 EntityShield).
 * Being collidable and pickable, projectiles strike it and are deflected ({@link #hurt} always returns false).
 */
public class ShieldEntity extends Entity {

    @Nullable
    public WeakReference<Player> player; // Server-side only; the client copy just follows the synced position

    public ShieldEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ShieldEntity(Level level, Player player) {
        this(EBEntities.SHIELD.get(), level);
        this.player = new WeakReference<>(player);
        Vec3 look = player.getLookAngle();
        this.moveTo(player.getX() + look.x, player.getY() + 1 + look.y, player.getZ() + look.z,
                player.yHeadRot, player.getXRot());
    }

    @Override
    public void tick() {
        Player owner = player != null ? player.get() : null;

        if (owner != null) {
            Vec3 look = owner.getLookAngle();
            this.moveTo(owner.getX() + look.x * 0.3, owner.getY() + 1 + look.y * 0.3,
                    owner.getZ() + look.z * 0.3, owner.yHeadRot, owner.getXRot());

            if (!owner.isUsingItem() || !(owner.getUseItem().getItem() instanceof ICastItem)) {
                owner.getData(EBAttachments.WIZARD_DATA).setShieldEntity(null);
                this.discard();
            }
        } else if (!level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof Projectile projectile) {
            level().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                    EBSounds.ENTITY_SHIELD_DEFLECT.get(), SoundSource.PLAYERS, 0.3f, 1.3f);
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

    @Override
    public @NotNull SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }
}
