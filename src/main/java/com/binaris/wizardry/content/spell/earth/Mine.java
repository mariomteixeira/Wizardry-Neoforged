package com.binaris.wizardry.content.spell.earth;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.item.ICastItem;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.core.config.EBServerConfig;
import com.binaris.wizardry.core.integrations.ArtifactChannel;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Mine extends RaySpell {

    public Mine() {
        this.ignoreLivingEntities(true);
        this.particleSpacing(0.5);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        BlockPos pos = blockHit.getBlockPos();

        // Needs to be outside because it gets run on the client side
        if (ctx.caster() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof ICastItem) player.swing(InteractionHand.MAIN_HAND);
            else if (player.getOffhandItem().getItem() instanceof ICastItem) player.swing(InteractionHand.OFF_HAND);
        }

        if (ctx.world().isClientSide) return true;

        if (BlockUtil.isBlockUnbreakable(ctx.world(), pos)) return false;
        if (!EntityUtil.canDamageBlocks(ctx.caster(), ctx.world())) return false;

        // The maximum harvest level as determined by the potency multiplier (1.12.2 formula)
        int harvestLevel = (int) ((ctx.modifiers().get(SpellModifiers.POTENCY) - 1) / EBServerConfig.POTENCY_INCREASE_PER_TIER.get() + 0.5f);
        if (harvestLevel > 0) harvestLevel--; // Novice wands already give some potency

        if (getHarvestLevel(ctx.world().getBlockState(pos)) > harvestLevel && harvestLevel < 3) return false;

        boolean flag = false;

        int blastUpgradeCount = (int) ((ctx.modifiers().get(SpellModifiers.BLAST) - 1) / EBServerConfig.BLAST_RADIUS_INCREASE_PER_LEVEL.get() + 0.5f);
        // 0 upgrades: single block; 1: 3x3 sem cantos; 2: 3x3; 3: 5x5 sem cantos (1.12.2)
        float radius = 0.5f + 0.73f * blastUpgradeCount;

        List<BlockPos> sphere = BlockUtil.getBlockSphere(pos, radius);

        for (BlockPos pos1 : sphere) {
            if (BlockUtil.isBlockUnbreakable(ctx.world(), pos1)) continue;

            BlockState state1 = ctx.world().getBlockState(pos1);
            if (state1.isAir()) continue;
            if (getHarvestLevel(state1) > harvestLevel && harvestLevel < 3) continue;

            if (ctx.caster() instanceof ServerPlayer player) {
                if (!BlockUtil.canBreak(player, ctx.world(), pos1, false)) continue;

                boolean silkTouch = ArtifactChannel.isEquipped(player, EBItems.CHARM_SILK_TOUCH.get());

                if (silkTouch && ctx.world() instanceof ServerLevel serverLevel) {
                    ItemStack silkTool = new ItemStack(Items.NETHERITE_PICKAXE);
                    silkTool.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
                    List<ItemStack> drops = Block.getDrops(state1, serverLevel, pos1, serverLevel.getBlockEntity(pos1), player, silkTool);
                    if (ctx.world().destroyBlock(pos1, false)) {
                        flag = true;
                        for (ItemStack drop : drops) {
                            ctx.world().addFreshEntity(new ItemEntity(ctx.world(), pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5, drop));
                        }
                    }
                } else {
                    int xp = ctx.world() instanceof ServerLevel serverLevel
                            ? state1.getExpDrop(serverLevel, pos1, serverLevel.getBlockEntity(pos1), player, ItemStack.EMPTY) : 0;
                    if (ctx.world().destroyBlock(pos1, true, player)) {
                        flag = true;
                        if (xp > 0 && ctx.world() instanceof ServerLevel serverLevel) {
                            state1.getBlock().popExperience(serverLevel, pos1, xp);
                        }
                    }
                }
            } else if (ctx.caster() != null && BlockUtil.canPlaceBlock(ctx.caster(), ctx.world(), pos1)) {
                // NPCs can dig the block under the target's feet
                flag = ctx.world().destroyBlock(pos1, true) || flag;
            }
        }
        return flag;
    }

    /** Maps the 1.21 tool-tier block tags back onto the 1.12.2 harvest-level scale. */
    private static int getHarvestLevel(BlockState state) {
        if (!state.requiresCorrectToolForDrops()) return 0;
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return 3;
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) return 2;
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) return 1;
        return 0;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    @Override
    protected void spawnParticle(CastContext ctx, double x, double y, double z, double vx, double vy, double vz) {
        ParticleBuilder.create(EBParticles.DUST).pos(x, y, z).time(20 + ctx.world().random.nextInt(5))
                .color(0.9f, 0.95f, 1f).shaded(false).spawn(ctx.world());
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.NOVICE, Elements.EARTH, SpellType.UTILITY, SpellAction.POINT, 5, 0, 5)
                .add(DefaultProperties.RANGE, 8f)
                .build();
    }
}
