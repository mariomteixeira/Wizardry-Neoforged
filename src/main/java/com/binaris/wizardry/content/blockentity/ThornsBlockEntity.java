package com.binaris.wizardry.content.blockentity;

import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.content.block.ThornsBlock;
import com.binaris.wizardry.setup.registries.EBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 1.12.2 TileEntityThorns: tracks caster, lifetime and damage multiplier, and drives growth. Unlike 1.12.2
 * (where age lived in the tile entity and was synced manually), growth updates the AGE blockstate directly.
 */
public class ThornsBlockEntity extends BlockEntity {

    private int ticksExisted = 0;
    private int lifetime = 600;
    public float damageMultiplier = 1;
    @Nullable
    private UUID casterUUID;

    public ThornsBlockEntity(BlockPos pos, BlockState state) {
        super(EBBlockEntities.THORNS.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ThornsBlockEntity be) {
        be.ticksExisted++;

        if (level.isClientSide) return;

        if (be.ticksExisted > be.lifetime) {
            level.destroyBlock(pos, false);
            return;
        }

        int age = state.getValue(ThornsBlock.AGE);
        if (be.ticksExisted % ThornsBlock.GROWTH_STAGE_DURATION == 0 && age < ThornsBlock.GROWTH_STAGES - 1) {
            level.setBlock(pos, state.setValue(ThornsBlock.AGE, age + 1), 3);
            BlockState upper = level.getBlockState(pos.above());
            if (upper.is(state.getBlock()) && upper.getValue(ThornsBlock.HALF) == DoubleBlockHalf.UPPER) {
                level.setBlock(pos.above(), upper.setValue(ThornsBlock.AGE, age + 1), 3);
            }
        }
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
        setChanged();
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
        ticksExisted = tag.getInt("timer");
        lifetime = tag.getInt("maxTimer");
        damageMultiplier = tag.getFloat("damageMultiplier");
        casterUUID = tag.hasUUID("caster") ? tag.getUUID("caster") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("timer", ticksExisted);
        tag.putInt("maxTimer", lifetime);
        tag.putFloat("damageMultiplier", damageMultiplier);
        if (casterUUID != null) tag.putUUID("caster", casterUUID);
    }
}
