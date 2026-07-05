package com.koomplo.wizardry.content.block;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Conjured translucent block (conjure_block / spectral_pathway). The 1.12.2 TileEntityTimer is replaced by a
 * scheduled tick carrying the lifetime, which the conjuring spell sets explicitly; pending ticks persist in saves.
 */
public class SpectralBlock extends HalfTransparentBlock {

    public SpectralBlock() {
        super(Properties.of().sound(SoundType.GLASS).noOcclusion().noLootTable().instabreak());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            ParticleBuilder.create(EBParticles.DUST)
                    .pos(pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble())
                    .time((int) (16.0D / (Math.random() * 0.8D + 0.2D)))
                    .color(0.4f + random.nextFloat() * 0.2f, 0.6f + random.nextFloat() * 0.4f, 0.6f + random.nextFloat() * 0.4f)
                    .shaded(true).spawn(world);
        }
    }

    // Spectral blocks cannot be built on (1.12.2 parity)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedAgainst().is(EBBlocks.SPECTRAL_BLOCK.get())) {
            event.setCanceled(true);
        }
    }
}
