package com.binaris.wizardry.content.block;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.item.ICastItem;
import com.binaris.wizardry.api.content.util.GeometryUtil;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.EBBlocks;
import com.binaris.wizardry.setup.registries.Spells;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransportationStoneBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 6, 11);

    public TransportationStoneBlock() {
        super(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE)
                .strength(0.3f).lightLevel(state -> 7).noOcclusion());
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        // Pops off (dropping itself) when the supporting block is removed, like a torch
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(stack.getItem() instanceof ICastItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        WizardDataHolder data = player.getData(EBAttachments.WIZARD_DATA);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos1 = pos.offset(x, 0, z);
                if (testForCircle(level, pos1)) {
                    // TODO charm_transportation (marco 6): 1.12.2 lembra até 4 círculos com o charm ativo
                    data.setTransportationLocation(pos1, level.dimension().location().toString());

                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.translatable("block.ebwizardry.transportation_stone.confirm",
                                Spells.TRANSPORTATION.getDescriptionFormatted()), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("block.ebwizardry.transportation_stone.invalid"), true);
        } else {
            BlockPos centre = findMostLikelyCircle(level, pos);
            if (centre != null) {
                // Displays particles in the required shape
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) continue;
                        ParticleBuilder.create(EBParticles.PATH)
                                .pos(GeometryUtil.getCentre(centre).add(x, -0.3125, z)).color(0x86ff65)
                                .time(200).scale(2).spawn(level);
                    }
                }
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Returns whether the specified location is surrounded by a complete circle of 8 transportation stones. */
    public static boolean testForCircle(Level level, BlockPos pos) {
        if (level.getBlockState(pos).blocksMotion() || level.getBlockState(pos.above()).blocksMotion()) return false;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (!level.getBlockState(pos.offset(x, 0, z)).is(EBBlocks.TRANSPORTATION_STONE.get())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Nullable
    private static BlockPos findMostLikelyCircle(Level level, BlockPos pos) {
        int bestSoFar = 0;
        BlockPos result = null;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos pos1 = pos.offset(x, 0, z);
                int n = getCircleCompleteness(level, pos1);
                if (n > bestSoFar) {
                    bestSoFar = n;
                    result = pos1;
                }
            }
        }

        return result;
    }

    private static int getCircleCompleteness(Level level, BlockPos pos) {
        int n = 0;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (level.getBlockState(pos.offset(x, 0, z)).is(EBBlocks.TRANSPORTATION_STONE.get())) n++;
            }
        }

        return n;
    }
}
