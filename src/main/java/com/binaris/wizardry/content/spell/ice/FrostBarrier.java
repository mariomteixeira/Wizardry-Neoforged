package com.binaris.wizardry.content.spell.ice;

import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.api.content.spell.internal.EntityCastContext;
import com.binaris.wizardry.api.content.spell.internal.LocationCastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.api.content.util.GeometryUtil;
import com.binaris.wizardry.content.entity.construct.IceBarrierConstruct;
import com.binaris.wizardry.setup.registries.EBEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FrostBarrier extends Spell {
    private static final double BARRIER_DISTANCE = 2;
    private static final double BARRIER_ARC_RADIUS = 10;
    private static final double BARRIER_SPACING = 1.4;

    @Override
    public boolean cast(PlayerCastContext ctx) {
        if (ctx.caster().onGround()) {
            if (!createBarriers(ctx.world(), ctx.caster().position(), ctx.caster().getLookAngle(), ctx.caster(), ctx.modifiers()))
                return false;
            this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
            return true;
        }

        return false;
    }

    @Override
    public boolean cast(EntityCastContext ctx) {
        if (ctx.target() == null) return false;
        if (ctx.caster().onGround()) {
            if (!createBarriers(ctx.world(), ctx.caster().position(), ctx.target().position().subtract(ctx.caster().position()), ctx.caster(), ctx.modifiers()))
                return false;
            this.playSound(ctx.world(), ctx.caster(), ctx.castingTicks(), -1);
            return true;
        }

        return false;
    }

    @Override
    public boolean cast(LocationCastContext ctx) {
        if (!createBarriers(ctx.world(), ctx.vec3(), new Vec3(ctx.direction().step()), null, ctx.modifiers())) return false;

        this.playSound(ctx.world(), ctx.x() - ctx.direction().getStepX(),
                ctx.y() - ctx.direction().getStepY(), ctx.z() - ctx.direction().getStepZ(),
                ctx.castingTicks(), ctx.duration());
        return true;
    }

    private boolean createBarriers(Level world, Vec3 origin, Vec3 direction, @Nullable LivingEntity caster, com.binaris.wizardry.api.content.spell.internal.SpellModifiers modifiers) {
        if (!world.isClientSide) {
            direction = GeometryUtil.horizontalise(direction);
            Vec3 centre = origin.add(direction.scale(BARRIER_DISTANCE - BARRIER_ARC_RADIUS));

            List<IceBarrierConstruct> barriers = new ArrayList<>();

            // More barriers with higher potency (1.12.2)
            int barrierCount = 1 + Math.max(1, (int) ((modifiers.get(com.binaris.wizardry.api.content.spell.internal.SpellModifiers.POTENCY) - 1)
                    / com.binaris.wizardry.core.config.EBServerConfig.POTENCY_INCREASE_PER_TIER.get() + 0.5f));

            for (int i = 0; i < barrierCount; i++) {
                IceBarrierConstruct barrier = createBarrier(world, centre, direction.yRot((float) (BARRIER_SPACING / BARRIER_ARC_RADIUS) * i), caster, modifiers, barrierCount, i);
                if (barrier != null) barriers.add(barrier);

                if (i == 0) continue;
                barrier = createBarrier(world, centre, direction.yRot(-(float) (BARRIER_SPACING / BARRIER_ARC_RADIUS) * i), caster, modifiers, barrierCount, i);
                if (barrier != null) barriers.add(barrier);
            }

            if (barriers.isEmpty()) return false;

            barriers.forEach(world::addFreshEntity);
        }

        return true;
    }


    private IceBarrierConstruct createBarrier(Level world, Vec3 centre, Vec3 direction, @Nullable LivingEntity caster, com.binaris.wizardry.api.content.spell.internal.SpellModifiers modifiers, int barrierCount, int index) {
        Vec3 position = centre.add(direction.scale(BARRIER_ARC_RADIUS));
        Integer floor = BlockUtil.getNearestFloor(world, BlockPos.containing(position), 3);
        if (floor == null) return null;
        position = GeometryUtil.replaceComponent(position, Axis.Y, floor);

        float scale = 1.5f - (float) index / barrierCount * 0.5f;
        double yOffset = 1.5 * scale;

        IceBarrierConstruct barrier = new IceBarrierConstruct(EBEntities.ICE_BARRIER.get(), world);
        barrier.setPos(position.x, position.y - yOffset, position.z);
        barrier.setCaster(caster);
        barrier.lifetime = (int) (property(com.binaris.wizardry.content.spell.DefaultProperties.DURATION)
                * modifiers.get(com.binaris.wizardry.api.content.spell.internal.SpellModifiers.DURATION));
        barrier.damageMultiplier = modifiers.get(com.binaris.wizardry.api.content.spell.internal.SpellModifiers.POTENCY);
        barrier.setRot((float) Math.toDegrees(Mth.atan2(-direction.x, direction.z)), barrier.getXRot());
        barrier.setSizeMultiplier(scale);
        barrier.setDelay(1 + 3 * index);

        if (!world.getEntitiesOfClass(barrier.getClass(), barrier.getBoundingBox().move(0, yOffset, 0)).isEmpty())
            return null;

        return barrier;
    }

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
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.ICE, SpellType.DEFENCE,
                        com.binaris.wizardry.api.content.spell.SpellAction.SUMMON, 20, 0, 50)
                .add(com.binaris.wizardry.content.spell.DefaultProperties.DURATION, 400)
                .build();
    }
}
