package com.binaris.wizardry.capabilities;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.data.MinionData;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.network.MinionSyncPacketS2C;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class MinionDataHolder implements INBTSerializable<CompoundTag>, MinionData {
    private final Mob provider;
    private int lifetime = -1;
    private boolean summoned = false;
    private boolean shouldDeleteGoals = false;
    private boolean shouldFollowOwner = false;
    private @Nullable UUID ownerUUID = null;
    private boolean shouldRestartGoals;
    private boolean searchNearbyTargets = true;

    public MinionDataHolder(Mob mob) {
        this.provider = mob;
    }

    private void sync() {
        if (!this.provider.level().isClientSide()) {
            CompoundTag tag = this.serializeNBT(this.provider.level().registryAccess());

            MinionSyncPacketS2C packet = new MinionSyncPacketS2C(this.provider.getId(), tag);
            Services.NETWORK_HELPER.sendToTracking(this.provider, packet);
        }
    }

    @Override
    public Mob getProvider() {
        return this.provider;
    }

    @Override
    public void tick() {
        if (!summoned) return;

        if (goalRestart()) {
            updateGoals();
            markGoalRestart(false);
        }


        if (provider.tickCount > this.getLifetime() && this.getLifetime() > 0) {
            this.provider.discard();
        }

        if (provider.level().isClientSide && provider.level().random.nextInt(8) == 0) {
            ParticleBuilder.create(EBParticles.DARK_MAGIC).pos(provider.xo, provider.yo + (provider.level().random.nextDouble() * 1.5 + 1), provider.zo).color(0.1f, 0.0f, 0.0f).spawn(provider.level());
        }

        sync();
    }

    @Override
    public int getLifetime() {
        return lifetime;
    }

    @Override
    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public boolean isSummoned() {
        return summoned;
    }

    @Override
    public void setSummoned(boolean summoned) {
        this.summoned = summoned;
        sync();
    }

    @Override
    public boolean shouldDeleteGoals() {
        return shouldDeleteGoals;
    }

    @Override
    public void setShouldDeleteGoals(boolean shouldDeleteGoals) {
        this.shouldDeleteGoals = shouldDeleteGoals;
    }

    @Override
    public boolean shouldFollowOwner() {
        return shouldFollowOwner;
    }

    @Override
    public void setShouldFollowOwner(boolean shouldFollowOwner) {
        this.shouldFollowOwner = shouldFollowOwner;
    }

    @Override
    public boolean searchNearbyTargets() {
        return searchNearbyTargets;
    }

    @Override
    public void setSearchNearbyTargets(boolean searchNearbyTargets) {
        this.searchNearbyTargets = searchNearbyTargets;
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        sync();
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        if (ownerUUID == null) return null;
        if (provider.level().isClientSide()) return null;

        return provider.getServer().getLevel(provider.level().dimension()).getEntity(ownerUUID) instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    public void setOwner(LivingEntity owner) {
        this.ownerUUID = owner.getUUID();
        sync();
    }

    @Override
    public void markGoalRestart(boolean shouldRestartGoals) {
        this.shouldRestartGoals = shouldRestartGoals;
    }

    @Override
    public boolean goalRestart() {
        return this.shouldRestartGoals;
    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("lifetime", lifetime);
        tag.putBoolean("summoned", summoned);
        tag.putBoolean("shouldDeleteGoals", shouldDeleteGoals);
        tag.putBoolean("shouldFollowOwner", shouldFollowOwner);
        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        this.summoned = tag.getBoolean("summoned");
        this.shouldDeleteGoals = tag.getBoolean("shouldDeleteGoals");
        this.shouldFollowOwner = tag.getBoolean("shouldFollowOwner");
        if (tag.contains("ownerUUID")) {
            this.ownerUUID = tag.getUUID("ownerUUID");
        }
    }
}
