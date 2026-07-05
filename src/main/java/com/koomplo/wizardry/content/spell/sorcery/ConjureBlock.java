package com.koomplo.wizardry.content.spell.sorcery;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ConjureBlock extends RaySpell {

    public static final SpellProperty<Integer> BLOCK_LIFETIME = SpellProperty.intProperty("block_lifetime");

    public ConjureBlock() {
        this.ignoreLivingEntities(true);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        BlockPos pos = blockHit.getBlockPos();

        // Dispelling: sneak-cast at an existing spectral block removes it
        if (ctx.caster() != null && ctx.caster().isShiftKeyDown()
                && ctx.world().getBlockState(pos).is(EBBlocks.SPECTRAL_BLOCK.get())) {
            if (!ctx.world().isClientSide) {
                ctx.world().removeBlock(pos, false);
            } else {
                ParticleBuilder.create(EBParticles.FLASH).pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                        .scale(3).color(0.75f, 1f, 0.85f).spawn(ctx.world());
            }
            return true;
        }

        pos = pos.relative(blockHit.getDirection());

        if (ctx.world().isClientSide) {
            ParticleBuilder.create(EBParticles.FLASH).pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                    .scale(3).color(0.75f, 1f, 0.85f).spawn(ctx.world());
        }

        if (BlockUtil.canBlockBeReplaced(ctx.world(), pos)) {
            if (!ctx.world().isClientSide) {
                ctx.world().setBlockAndUpdate(pos, EBBlocks.SPECTRAL_BLOCK.get().defaultBlockState());
                int lifetime = (int) (property(BLOCK_LIFETIME) * ctx.modifiers().get(SpellModifiers.DURATION));
                ctx.world().scheduleTick(pos.immutable(), EBBlocks.SPECTRAL_BLOCK.get(), lifetime);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.NOVICE, Elements.SORCERY, SpellType.UTILITY, SpellAction.POINT, 10, 0, 10)
                .add(DefaultProperties.RANGE, 10f)
                .add(BLOCK_LIFETIME, 900)
                .build();
    }
}
