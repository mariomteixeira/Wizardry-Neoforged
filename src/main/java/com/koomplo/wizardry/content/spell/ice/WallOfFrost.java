package com.koomplo.wizardry.content.spell.ice;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.content.block.StatueBlock;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.setup.registries.EBBlocks;
import com.koomplo.wizardry.setup.registries.EBSounds;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WallOfFrost extends RaySpell {

    private static final int MINIMUM_PLACEMENT_RANGE = 2;

    public WallOfFrost() {
        this.particleVelocity(1);
        this.particleSpacing(0.5);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        // Wall of frost freezes entities solid too!
        if (entityHit.getEntity() instanceof Mob target && !ctx.world().isClientSide) {
            if (((StatueBlock) EBBlocks.ICE_STATUE.get()).convertToStatue(target, ctx.caster(),
                    (int) (property(DefaultProperties.DURATION) * ctx.modifiers().get(SpellModifiers.DURATION)))) {
                target.playSound(EBSounds.MISC_FREEZE.get(), 1.0F, ctx.world().random.nextFloat() * 0.4F + 0.8F);
            }
        }
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        Level world = ctx.world();
        LivingEntity caster = ctx.caster();
        BlockPos pos = blockHit.getBlockPos();
        Direction side = blockHit.getDirection();

        if (!world.isClientSide && EntityUtil.canDamageBlocks(caster, world)) {

            // Stops the ice being placed floating above snow and grass
            if (BlockUtil.canBlockBeReplaced(world, pos)) {
                pos = pos.relative(side.getOpposite());
            }

            if (origin.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > MINIMUM_PLACEMENT_RANGE * MINIMUM_PLACEMENT_RANGE
                    && !world.getBlockState(pos).is(EBBlocks.ICE_STATUE.get())
                    && !world.getBlockState(pos).is(EBBlocks.DRY_FROSTED_ICE.get())) {

                pos = pos.relative(side);

                int duration = (int) (property(DefaultProperties.DURATION) * ctx.modifiers().get(SpellModifiers.DURATION));

                if (BlockUtil.canBlockBeReplaced(world, pos) && BlockUtil.canPlaceBlock(caster, world, pos)) {
                    world.setBlockAndUpdate(pos, EBBlocks.DRY_FROSTED_ICE.get().defaultBlockState());
                    world.scheduleTick(pos.immutable(), EBBlocks.DRY_FROSTED_ICE.get(), duration);
                }

                // Builds a 2 block high wall if it hits the ground
                if (side == Direction.UP) {
                    pos = pos.relative(side);

                    if (BlockUtil.canBlockBeReplaced(world, pos) && BlockUtil.canPlaceBlock(caster, world, pos)) {
                        world.setBlockAndUpdate(pos, EBBlocks.DRY_FROSTED_ICE.get().defaultBlockState());
                        world.scheduleTick(pos.immutable(), EBBlocks.DRY_FROSTED_ICE.get(), duration);
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return true;
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    @Override
    protected void playSound(Level world, LivingEntity entity, int castTicks, int duration) {
        this.playSoundLoop(world, entity, castTicks);
    }

    @Override
    protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration) {
        this.playSoundLoop(world, x, y, z, ticksInUse, duration);
    }

    @Override
    protected void spawnParticle(CastContext ctx, double x, double y, double z, double vx, double vy, double vz) {
        ParticleBuilder.create(EBParticles.ICE).pos(x, y, z).velocity(vx, vy, vz).collide(true).spawn(ctx.world());
        ParticleBuilder.create(EBParticles.SNOW).pos(x, y, z).velocity(vx, vy, vz).collide(true).spawn(ctx.world());
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.ICE, SpellType.UTILITY, SpellAction.POINT, 15, 15, 70)
                .add(DefaultProperties.RANGE, 10f)
                .add(DefaultProperties.DURATION, 600)
                .build();
    }
}
