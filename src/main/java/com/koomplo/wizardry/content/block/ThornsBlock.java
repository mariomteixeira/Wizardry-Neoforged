package com.koomplo.wizardry.content.block;

import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.blockentity.ThornsBlockEntity;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.core.AllyDesignation;
import com.koomplo.wizardry.setup.registries.EBBlockEntities;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.Spells;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThornsBlock extends BaseEntityBlock {

    public static final MapCodec<ThornsBlock> CODEC = simpleCodec(properties -> new ThornsBlock());

    public static final int GROWTH_STAGES = 8;
    public static final int GROWTH_STAGE_DURATION = 2;

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public ThornsBlock() {
        super(Properties.of().sound(SoundType.GRASS).strength(4).noLootTable().noOcclusion()
                .noCollission().offsetType(OffsetType.XZ));
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE, 7));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HALF, AGE);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    public void placeAt(Level level, BlockPos lowerPos, int flags) {
        level.setBlock(lowerPos, defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE, 0), flags);
        level.setBlock(lowerPos.above(), defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, 0), flags);
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (applyThornDamage(level, pos, state, entity)) {
            entity.makeStuckInBlock(state, new Vec3(0.25, 0.05F, 0.25)); // Cobweb-style slow (1.12.2 setInWeb)
        }
    }

    public static boolean applyThornDamage(Level level, BlockPos pos, BlockState state, Entity target) {
        DamageSource source = level.damageSources().cactus();
        float damage = Spells.FOREST_OF_THORNS.property(DefaultProperties.DAMAGE);

        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;

        if (level.getBlockEntity(lowerPos) instanceof ThornsBlockEntity thorns) {
            damage *= thorns.damageMultiplier;

            LivingEntity caster = thorns.getCaster();
            if (!AllyDesignation.isValidTarget(caster, target)) return false; // Don't attack or slow allies

            if (caster != null) {
                source = MagicDamageSource.causeDirectMagicDamage(caster, EBDamageSources.MAGIC);
            }
        }

        if (target.tickCount % 20 == 0) {
            EntityUtil.attackEntityWithoutKnockback(target, source, damage);
        }
        return true;
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return level.getBlockState(pos.below()).is(this);
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                              @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        // Either half breaking removes the other
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y
                && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)
                && (!neighborState.is(this) || neighborState.getValue(HALF) == half)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ThornsBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return null;
        return createTickerHelper(type, EBBlockEntities.THORNS.get(), (lvl, pos, st, be) -> ThornsBlockEntity.tick(lvl, pos, st, be));
    }
}
