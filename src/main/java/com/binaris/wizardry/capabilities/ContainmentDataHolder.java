package com.binaris.wizardry.capabilities;

import com.binaris.wizardry.api.content.data.ContainmentData;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.network.ContainmentSyncPacketS2C;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class ContainmentDataHolder implements INBTSerializable<CompoundTag>, ContainmentData {
    private final LivingEntity provider;
    private BlockPos containmentPos = null;

    public ContainmentDataHolder(LivingEntity entity) {
        this.provider = entity;
    }

    private void sync() {
        if (!this.provider.level().isClientSide()) {
            CompoundTag tag = this.serializeNBT(this.provider.level().registryAccess());
            ContainmentSyncPacketS2C packet = new ContainmentSyncPacketS2C(this.provider.getId(), tag);
            if (this.provider instanceof ServerPlayer serverPlayer) {
                Services.NETWORK_HELPER.sendTo(serverPlayer, packet);
            } else {
                Services.NETWORK_HELPER.sendToTracking(this.provider, packet);
            }
        }
    }

    @Override
    public LivingEntity getProvider() {
        return this.provider;
    }

    @Override
    public @Nullable BlockPos getContainmentPos() {
        return containmentPos;
    }

    @Override
    public void setContainmentPos(@Nullable BlockPos pos) {
        this.containmentPos = pos;
        sync();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (containmentPos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", containmentPos.getX());
            posTag.putInt("y", containmentPos.getY());
            posTag.putInt("z", containmentPos.getZ());
            tag.put("containmentPos", posTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("containmentPos")) {
            CompoundTag posTag = tag.getCompound("containmentPos");
            int x = posTag.getInt("x");
            int y = posTag.getInt("y");
            int z = posTag.getInt("z");
            this.containmentPos = new BlockPos(x, y, z);
        } else {
            this.containmentPos = null;
        }
    }
}
