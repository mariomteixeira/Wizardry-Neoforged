package com.binaris.wizardry.content.block;

import com.binaris.wizardry.api.content.item.ICastItem;
import com.binaris.wizardry.content.blockentity.MagicLightBlockEntity;
import com.binaris.wizardry.core.integrations.ArtifactChannel;
import com.binaris.wizardry.setup.registries.EBBlockEntities;
import com.binaris.wizardry.setup.registries.EBItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

public class MagicLightBlock extends BaseEntityBlock {

    public static final MapCodec<MagicLightBlock> CODEC = simpleCodec(properties -> new MagicLightBlock());

    public MagicLightBlock() {
        super(Properties.of().lightLevel(state -> 15).strength(-1.0F, 3600000.0F).noLootTable().noOcclusion());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE; // The block entity renderer draws the flare
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldStack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        // Players with the lantern charm can dispel any light with a wand (1.12.2 parity)
        if (heldStack.getItem() instanceof ICastItem && ArtifactChannel.isEquipped(player, EBItems.CHARM_LIGHT.get())) {
            if (!level.isClientSide) level.removeBlock(pos, false);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(heldStack, state, level, pos, player, hand, hit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new MagicLightBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, EBBlockEntities.MAGIC_LIGHT.get(), (lvl, pos, st, be) -> MagicLightBlockEntity.tick(lvl, pos, st, be));
    }
}
