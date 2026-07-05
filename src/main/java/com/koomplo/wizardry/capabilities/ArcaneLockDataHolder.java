package com.koomplo.wizardry.capabilities;

import com.koomplo.wizardry.api.content.data.ArcaneLockData;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.network.ArcaneLockSyncPacketS2C;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ArcaneLockDataHolder implements INBTSerializable<CompoundTag>, ArcaneLockData {
    private final BlockEntity provider;
    private UUID ownerUUID = null;

    public ArcaneLockDataHolder(BlockEntity provider) {
        this.provider = provider;
    }

    private void sync() {
        if (provider.getLevel() != null && !this.provider.getLevel().isClientSide()) {
            CompoundTag tag = this.serializeNBT(this.provider.getLevel().registryAccess());
            ArcaneLockSyncPacketS2C packet = new ArcaneLockSyncPacketS2C(this.provider.getBlockPos(), tag);
            Services.NETWORK_HELPER.sendToTracking((ServerLevel) this.provider.getLevel(), this.provider.getBlockPos(), packet);
        }
    }

    @Override
    public boolean isArcaneLocked() {
        return ownerUUID != null;
    }

    @Override
    public void setArcaneLockOwner(String ownerUUID) {
        if (ownerUUID == null) {
            this.ownerUUID = null;
        } else {
            this.ownerUUID = UUID.fromString(ownerUUID);
        }
        sync();
    }

    @Override
    public void clearArcaneLockOwner() {
        this.ownerUUID = null;
        sync();
    }

    @Override
    public @Nullable UUID getArcaneLockOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (this.ownerUUID != null) {
            tag.putString(NBT_KEY, this.ownerUUID.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(NBT_KEY)) {
            this.ownerUUID = UUID.fromString(tag.getString(NBT_KEY));
        } else {
            this.ownerUUID = null;
        }
    }
}
