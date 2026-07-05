package com.koomplo.wizardry.content.blockentity;

import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.setup.registries.EBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Stores the snare's caster so the trap only triggers on valid targets (1.12.2 TileEntityPlayerSave). */
public class SnareBlockEntity extends BlockEntity {

    @Nullable
    private UUID casterUUID;

    public SnareBlockEntity(BlockPos pos, BlockState state) {
        super(EBBlockEntities.SNARE.get(), pos, state);
    }

    public void setCaster(@Nullable LivingEntity caster) {
        this.casterUUID = caster == null ? null : caster.getUUID();
        setChanged();
    }

    @Nullable
    public LivingEntity getCaster() {
        if (casterUUID == null || level == null) return null;
        return EntityUtil.getEntityByUUID(level, casterUUID) instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        casterUUID = tag.hasUUID("caster") ? tag.getUUID("caster") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (casterUUID != null) tag.putUUID("caster", casterUUID);
    }
}
