package com.koomplo.wizardry.content.spell.sorcery;

import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SpectralPathway extends Spell {

    /** The base length of the conjured bridge, in blocks. */
    public static final SpellProperty<Integer> LENGTH = SpellProperty.intProperty("length");

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        // Won't work if caster is airborne or already on a bridge (prevents infinite bridges)
        var standingOn = ctx.world().getBlockState(ctx.caster().getOnPos());
        if (standingOn.isAir() || standingOn.is(EBBlocks.SPECTRAL_BLOCK.get())) return false;

        Direction direction = ctx.caster().getDirection();

        boolean flag = false;

        if (!ctx.world().isClientSide) {
            // Nearest block intersection to the player's feet (a block takes its northwestern corner coords)
            BlockPos origin = new BlockPos((int) Math.round(ctx.caster().getX()), (int) ctx.caster().getY() - 1,
                    (int) Math.round(ctx.caster().getZ()));

            int startPoint = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? -1 : 0;
            int length = (int) (property(LENGTH) * ctx.modifiers().get(SpellModifiers.RANGE));
            int lifetime = (int) (property(DefaultProperties.DURATION) * ctx.modifiers().get(SpellModifiers.DURATION));

            Direction perpendicular = Direction.fromAxisAndDirection(direction.getClockWise().getAxis(), Direction.AxisDirection.NEGATIVE);

            for (int i = 0; i < length; i++) {
                flag = placePathwayBlockIfPossible(ctx.world(), origin.relative(direction, startPoint + i), lifetime) || flag;
                flag = placePathwayBlockIfPossible(ctx.world(), origin.relative(direction, startPoint + i).relative(perpendicular), lifetime) || flag;
            }
        }

        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return flag;
    }

    private boolean placePathwayBlockIfPossible(Level world, BlockPos pos, int lifetime) {
        if (BlockUtil.canBlockBeReplaced(world, pos, true)) {
            world.setBlockAndUpdate(pos, EBBlocks.SPECTRAL_BLOCK.get().defaultBlockState());
            world.scheduleTick(pos.immutable(), EBBlocks.SPECTRAL_BLOCK.get(), lifetime);
            return true;
        }
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.SORCERY, SpellType.UTILITY, SpellAction.POINT, 40, 15, 300)
                .add(LENGTH, 25)
                .add(DefaultProperties.DURATION, 1200)
                .build();
    }
}
