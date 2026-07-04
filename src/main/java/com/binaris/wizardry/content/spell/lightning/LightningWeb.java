package com.binaris.wizardry.content.spell.lightning;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.core.AllyDesignation;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LightningWeb extends RaySpell {

    public static final SpellProperty<Float> PRIMARY_DAMAGE = SpellProperty.floatProperty("primary_damage");
    public static final SpellProperty<Float> SECONDARY_DAMAGE = SpellProperty.floatProperty("secondary_damage");
    public static final SpellProperty<Float> TERTIARY_DAMAGE = SpellProperty.floatProperty("tertiary_damage");

    public static final SpellProperty<Float> SECONDARY_RANGE = SpellProperty.floatProperty("secondary_range");
    public static final SpellProperty<Float> TERTIARY_RANGE = SpellProperty.floatProperty("tertiary_range");

    public static final SpellProperty<Integer> SECONDARY_MAX_TARGETS = SpellProperty.intProperty("secondary_max_targets");
    /** This is per secondary target. */
    public static final SpellProperty<Integer> TERTIARY_MAX_TARGETS = SpellProperty.intProperty("tertiary_max_targets");

    public LightningWeb() {
        this.aimAssist(0.6f);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (entityHit.getEntity() instanceof LivingEntity target && ctx.castingTicks() % 10 == 0) {

            electrocute(ctx, origin, target, property(PRIMARY_DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));

            List<LivingEntity> secondaryTargets = EntityUtil.getLivingWithinRadius(
                    property(SECONDARY_RANGE), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), ctx.world());

            secondaryTargets.stream()
                    .filter(entity -> !entity.equals(target))
                    .filter(EntityUtil::isLiving)
                    .filter(e -> AllyDesignation.isValidTarget(ctx.caster(), e))
                    .limit(property(SECONDARY_MAX_TARGETS))
                    .forEach(secondaryTarget -> {
                        electrocute(ctx, target.position().add(0, target.getBbHeight() / 2, 0), secondaryTarget,
                                property(SECONDARY_DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));

                        List<LivingEntity> tertiaryTargets = EntityUtil.getLivingWithinRadius(
                                property(TERTIARY_RANGE), secondaryTarget.getX(),
                                secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2, secondaryTarget.getZ(), ctx.world());

                        tertiaryTargets.stream()
                                .filter(entity -> !secondaryTargets.contains(entity))
                                .filter(entity -> !entity.equals(target))
                                .filter(EntityUtil::isLiving)
                                .filter(e -> AllyDesignation.isValidTarget(ctx.caster(), e))
                                .limit(property(TERTIARY_MAX_TARGETS))
                                .forEach(tertiaryTarget -> electrocute(ctx,
                                        secondaryTarget.position().add(0, secondaryTarget.getBbHeight() / 2, 0),
                                        tertiaryTarget,
                                        property(TERTIARY_DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY)));
                    });
        }
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        if (ctx.world().isClientSide) {
            // The arc does not reach full range when it has a free end
            double freeRange = 0.8 * property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE);

            ParticleBuilder beam = ParticleBuilder.create(EBParticles.BEAM).color(0.2f, 0.6f, 1f);
            if (ctx.caster() != null) beam.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position())).length(freeRange);
            else beam.pos(origin).target(origin.add(direction.scale(freeRange)));
            beam.spawn(ctx.world());

            if (ctx.castingTicks() % 4 == 0) {
                ParticleBuilder arc = ParticleBuilder.create(EBParticles.LIGHTNING);
                if (ctx.caster() != null) arc.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position())).length(freeRange);
                else arc.pos(origin).target(origin.add(direction.scale(freeRange)));
                arc.spawn(ctx.world());
            }
        }
        return true;
    }

    private void electrocute(CastContext ctx, Vec3 origin, Entity target, float damage) {
        if (MagicDamageSource.isEntityImmune(EBDamageSources.SHOCK, target)) {
            if (!ctx.world().isClientSide && ctx.castingTicks() == 1 && ctx instanceof PlayerCastContext playerCtx) {
                playerCtx.caster().displayClientMessage(
                        Component.translatable("spell.resist", target.getName(), this.getDescriptionFormatted()), true);
            }
        } else {
            DamageSource source = ctx.caster() != null ? MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.SHOCK)
                    : target.damageSources().magic();
            EntityUtil.attackEntityWithoutKnockback(target, source, damage);
        }

        if (ctx.world().isClientSide) {
            ParticleBuilder beam = ParticleBuilder.create(EBParticles.BEAM).color(0.2f, 0.6f, 1f);
            if (ctx.caster() != null) beam.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
            else beam.pos(origin);
            beam.target(target).spawn(ctx.world());

            if (ctx.castingTicks() % 3 == 0) {
                ParticleBuilder arc = ParticleBuilder.create(EBParticles.LIGHTNING);
                if (ctx.caster() != null) arc.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
                else arc.pos(origin);
                arc.target(target).spawn(ctx.world());
            }

            for (int i = 0; i < 5; i++) {
                ParticleBuilder.create(EBParticles.SPARK, target).spawn(ctx.world());
            }
        }
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
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.MASTER, Elements.LIGHTNING, SpellType.ATTACK, SpellAction.POINT, 15, 15, 70)
                .add(DefaultProperties.RANGE, 10f)
                .add(PRIMARY_DAMAGE, 5f)
                .add(SECONDARY_DAMAGE, 4f)
                .add(TERTIARY_DAMAGE, 3f)
                .add(SECONDARY_RANGE, 5f)
                .add(TERTIARY_RANGE, 5f)
                .add(SECONDARY_MAX_TARGETS, 5)
                .add(TERTIARY_MAX_TARGETS, 2)
                .build();
    }
}
