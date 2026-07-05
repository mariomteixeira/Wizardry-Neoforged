package com.binaris.wizardry.content.block;

import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.content.blockentity.StatueBlockEntity;
import com.binaris.wizardry.setup.registries.EBBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Statue block for creatures frozen solid (ice) or petrified (stone) — 1.12.2 BlockStatue. A statue is a
 * column of 1-3 of these blocks whose bottom block entity stores the victim; breaking any part releases it.
 */
public class StatueBlock extends BaseEntityBlock {

    public static final MapCodec<StatueBlock> CODEC = simpleCodec(properties -> new StatueBlock(false));

    /** The NBT tag name for storing the petrified flag (used for rendering) in the target's persistent data. */
    public static final String PETRIFIED_NBT_KEY = "petrified";
    /** The NBT tag name for storing the frozen flag (used for rendering) in the target's persistent data. */
    public static final String FROZEN_NBT_KEY = "frozen";

    public final boolean isIce;

    public StatueBlock(boolean isIce) {
        super(isIce
                ? Properties.of().mapColor(MapColor.ICE).friction(0.98f).sound(SoundType.GLASS).strength(0.5f).noOcclusion().noLootTable()
                : Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(1.5f).noOcclusion().noLootTable());
        this.isIce = isIce;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // Ice statues render the translucent ice block with the frozen creature inside (drawn by the BER);
        // petrified creatures are drawn by the BER alone
        return this.isIce ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (!this.isIce && level.getBlockEntity(pos) instanceof StatueBlockEntity statue && statue.parts > 0) {
            // Block bounds match the width and height of the entity, clamped to within 1 block
            double minXZ = Math.max(0.5 - statue.entityWidth / 2, 0);
            double maxXZ = Math.min(0.5 + statue.entityWidth / 2, 1);
            double maxY = statue.position == statue.parts
                    ? Math.min(statue.entityHeight - statue.parts + 1, 1) : 1;
            return Shapes.box(minXZ, 0, minXZ, maxXZ, Math.max(maxY, 0.0625), maxXZ);
        }
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new StatueBlockEntity(pos, state, isIce);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, EBBlockEntities.STATUE.get(),
                (world, pos, blockState, be) -> ((StatueBlockEntity) be).serverTick());
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {

            if (level.getBlockEntity(pos) instanceof StatueBlockEntity statue) {
                // Chain-destroy the other parts of the statue (self-terminating: already-broken parts are air)
                if (statue.parts == 2) {
                    if (statue.position == 2) destroyPart(level, pos.below());
                    else destroyPart(level, pos.above());
                } else if (statue.parts == 3) {
                    if (statue.position == 3) {
                        destroyPart(level, pos.below());
                        destroyPart(level, pos.below(2));
                    } else if (statue.position == 2) {
                        destroyPart(level, pos.below());
                        destroyPart(level, pos.above());
                    } else {
                        destroyPart(level, pos.above());
                        destroyPart(level, pos.above(2));
                    }
                }

                // Only the bottom block releases the creature (destroyPart calls this method for the others)
                if (statue.position == 1) {
                    statue.releaseCreature();
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static void destroyPart(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof StatueBlock) {
            level.destroyBlock(pos, false);
        }
    }

    /**
     * Turns the given entity into a statue. The type of statue depends on the block instance this method was
     * invoked on (1.12.2 BlockStatue#convertToStatue).
     *
     * @return True if the entity was successfully turned into a statue, false if something was in the way.
     */
    public boolean convertToStatue(Mob target, @Nullable LivingEntity caster, int duration) {

        if (target.deathTime > 0 || target.level().isClientSide) return false;

        BlockPos pos = target.blockPosition();
        Level world = target.level();

        target.hurtTime = 0; // Stops the entity looking red while frozen and the resulting z-fighting
        target.clearFire();

        int parts;
        if (target.getBbHeight() < 1.2 || target.isBaby()) parts = 1; // Short mobs such as spiders and pigs
        else if (target.getBbHeight() < 2.5) parts = 2; // Normal sized mobs like zombies and skeletons
        else parts = 3; // Tall mobs like endermen

        for (int i = 0; i < parts; i++) {
            if (!BlockUtil.canBlockBeReplaced(world, pos.above(i)) || !BlockUtil.canPlaceBlock(caster, world, pos.above(i)))
                return false;
        }

        target.getPersistentData().putBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);

        for (int i = 0; i < parts; i++) {
            world.setBlockAndUpdate(pos.above(i), this.defaultBlockState());
            if (world.getBlockEntity(pos.above(i)) instanceof StatueBlockEntity statue) {
                statue.setCreatureAndPart(target, i + 1, parts);
                if (i == 0) statue.setLifetime(duration);
            }
        }

        target.discard();
        return true;
    }
}
