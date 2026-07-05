package com.koomplo.wizardry.content.blockentity;

import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBBlockEntities;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class VanishingCobwebBlockEntity extends BlockEntityTimer {
    public VanishingCobwebBlockEntity(BlockPos pos, BlockState state) {
        super(EBBlockEntities.VANISHING_COBWEB.get(), pos, state, Spells.COBWEBS.property(DefaultProperties.DURATION));
    }
}
