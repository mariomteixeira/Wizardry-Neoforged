package com.koomplo.wizardry.content.blockentity;

import com.koomplo.wizardry.setup.registries.EBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Timer for the conjured magic light (1.12.2 TileEntityMagicLight). Lifetime -1 means permanent (lantern charm).
 * The tick counter also drives the renderer's fade in/out.
 */
public class MagicLightBlockEntity extends BlockEntity {

    private int lifetime = 600;
    public int timer;

    public MagicLightBlockEntity(BlockPos pos, BlockState state) {
        super(EBBlockEntities.MAGIC_LIGHT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagicLightBlockEntity be) {
        be.timer++;
        if (!level.isClientSide && be.lifetime >= 0 && be.timer >= be.lifetime) {
            level.removeBlock(pos, false);
        }
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lifetime = tag.getInt("lifetime");
        timer = tag.getInt("timer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("lifetime", lifetime);
        tag.putInt("timer", timer);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
