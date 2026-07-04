package com.binaris.wizardry.content.spell.earth;

import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.EntityCastContext;
import com.binaris.wizardry.api.content.spell.internal.LocationCastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.content.block.ThornsBlock;
import com.binaris.wizardry.content.blockentity.ThornsBlockEntity;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.EBBlocks;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ForestOfThorns extends Spell {

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    public boolean canCastByEntity() {
        return true;
    }

    @Override
    public boolean canCastByLocation() {
        return true;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        if (!summonThorns(ctx.world(), ctx.caster(), ctx.caster().blockPosition(), ctx.modifiers())) return false;
        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean cast(EntityCastContext ctx) {
        if (!summonThorns(ctx.world(), ctx.caster(), ctx.caster().blockPosition(), ctx.modifiers())) return false;
        this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
        return true;
    }

    @Override
    public boolean cast(LocationCastContext ctx) {
        BlockPos origin = BlockPos.containing(ctx.vec3()).relative(ctx.direction());
        if (!summonThorns(ctx.world(), null, origin, ctx.modifiers())) return false;
        this.playSound(ctx.world(), ctx.vec3(), ctx.castingTicks(), -1);
        return true;
    }

    private boolean summonThorns(Level world, @Nullable LivingEntity caster, BlockPos origin, SpellModifiers modifiers) {
        if (world.isClientSide) return true;

        double radius = property(DefaultProperties.EFFECT_RADIUS) * modifiers.get(SpellModifiers.BLAST);

        List<BlockPos> ring = new ArrayList<>((int) (7 * radius));

        for (int x = -(int) radius; x <= radius; x++) {
            for (int z = -(int) radius; z <= radius; z++) {
                double distance = Mth.sqrt(x * x + z * z);
                if (distance > radius || distance < radius - 1.5) continue;

                Integer y = BlockUtil.getNearestSurface(world, origin.offset(x, 0, z), Direction.UP, (int) radius, true,
                        BlockUtil.SurfaceCriteria.BUILDABLE);
                if (y != null) ring.add(new BlockPos(origin.getX() + x, y, origin.getZ() + z));
            }
        }

        if (ring.isEmpty()) return false;

        for (BlockPos pos : ring) {
            if (BlockUtil.canBlockBeReplaced(world, pos) && BlockUtil.canBlockBeReplaced(world, pos.above())) {
                ((ThornsBlock) EBBlocks.THORNS.get()).placeAt(world, pos, 3);

                if (world.getBlockEntity(pos) instanceof ThornsBlockEntity thorns) {
                    thorns.setLifetime((int) (property(DefaultProperties.DURATION) * modifiers.get(SpellModifiers.DURATION)));
                    if (caster != null) thorns.setCaster(caster);
                    thorns.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
                }
            }
        }
        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.EARTH, SpellType.CONSTRUCT, SpellAction.SUMMON, 100, 20, 250)
                .add(DefaultProperties.EFFECT_RADIUS, 3)
                .add(DefaultProperties.DURATION, 600)
                .add(DefaultProperties.DAMAGE, 1f)
                .build();
    }
}
