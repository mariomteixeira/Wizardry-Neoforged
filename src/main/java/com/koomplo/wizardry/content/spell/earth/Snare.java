package com.koomplo.wizardry.content.spell.earth;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.content.blockentity.SnareBlockEntity;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Snare extends RaySpell {

    public Snare() {
        this.soundValues(1, 1.4f, 0.4f);
        this.ignoreLivingEntities(true);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        BlockPos pos = blockHit.getBlockPos();

        if (blockHit.getDirection() == Direction.UP
                && ctx.world().getBlockState(pos).isFaceSturdy(ctx.world(), pos, Direction.UP)
                && BlockUtil.canBlockBeReplaced(ctx.world(), pos.above())) {
            if (!ctx.world().isClientSide) {
                ctx.world().setBlockAndUpdate(pos.above(), EBBlocks.SNARE.get().defaultBlockState());
                if (ctx.world().getBlockEntity(pos.above()) instanceof SnareBlockEntity snare) {
                    snare.setCaster(ctx.caster());
                }
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
    protected void spawnParticle(CastContext ctx, double x, double y, double z, double vx, double vy, double vz) {
        float brightness = ctx.world().random.nextFloat() * 0.25f;
        ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).time(20 + ctx.world().random.nextInt(8))
                .color(brightness, brightness + 0.1f, 0f).spawn(ctx.world());
        ParticleBuilder.create(EBParticles.LEAF).pos(x, y, z).velocity(0, -0.01, 0)
                .time(40 + ctx.world().random.nextInt(10)).spawn(ctx.world());
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.NOVICE, Elements.EARTH, SpellType.ATTACK, SpellAction.POINT, 10, 0, 10)
                .add(DefaultProperties.RANGE, 10f)
                .add(DefaultProperties.DAMAGE, 6f)
                .add(DefaultProperties.EFFECT_DURATION, 100)
                .add(DefaultProperties.EFFECT_STRENGTH, 2)
                .build();
    }
}
