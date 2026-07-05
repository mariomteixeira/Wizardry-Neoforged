package com.koomplo.wizardry.content.spell.lightning;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.PlayerCastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.core.AllyDesignation;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.RaySpell;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChainLightning extends RaySpell {

    public static final SpellProperty<Float> PRIMARY_DAMAGE = SpellProperty.floatProperty("primary_damage");
    public static final SpellProperty<Float> SECONDARY_DAMAGE = SpellProperty.floatProperty("secondary_damage");
    public static final SpellProperty<Float> TERTIARY_DAMAGE = SpellProperty.floatProperty("tertiary_damage");

    public static final SpellProperty<Float> SECONDARY_RANGE = SpellProperty.floatProperty("secondary_range");
    public static final SpellProperty<Float> TERTIARY_RANGE = SpellProperty.floatProperty("tertiary_range");

    public static final SpellProperty<Integer> SECONDARY_MAX_TARGETS = SpellProperty.intProperty("secondary_max_targets");
    /** This is per secondary target. */
    public static final SpellProperty<Integer> TERTIARY_MAX_TARGETS = SpellProperty.intProperty("tertiary_max_targets");

    public ChainLightning() {
        this.aimAssist(0.6f);
        this.soundValues(1, 1.7f, 0.2f);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        // Anything can be attacked with the initial arc, because the player has control over where it goes.
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return false;

        electrocute(ctx, origin, target, property(PRIMARY_DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));

        // Secondary chaining effect
        List<LivingEntity> secondaryTargets = EntityUtil.getLivingWithinRadius(
                property(SECONDARY_RANGE), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), ctx.world());

        secondaryTargets.remove(target);
        secondaryTargets.removeIf(e -> !EntityUtil.isLiving(e));
        secondaryTargets.removeIf(e -> !AllyDesignation.isValidTarget(ctx.caster(), e));
        if (secondaryTargets.size() > property(SECONDARY_MAX_TARGETS))
            secondaryTargets = secondaryTargets.subList(0, property(SECONDARY_MAX_TARGETS));

        for (LivingEntity secondaryTarget : secondaryTargets) {

            electrocute(ctx, target.position().add(0, target.getBbHeight() / 2, 0), secondaryTarget,
                    property(SECONDARY_DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));

            // Tertiary chaining effect
            List<LivingEntity> tertiaryTargets = EntityUtil.getLivingWithinRadius(
                    property(TERTIARY_RANGE), secondaryTarget.getX(),
                    secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2, secondaryTarget.getZ(), ctx.world());

            tertiaryTargets.remove(target);
            tertiaryTargets.removeAll(secondaryTargets);
            tertiaryTargets.removeIf(e -> !EntityUtil.isLiving(e));
            tertiaryTargets.removeIf(e -> !AllyDesignation.isValidTarget(ctx.caster(), e));
            if (tertiaryTargets.size() > property(TERTIARY_MAX_TARGETS))
                tertiaryTargets = tertiaryTargets.subList(0, property(TERTIARY_MAX_TARGETS));

            for (LivingEntity tertiaryTarget : tertiaryTargets) {
                electrocute(ctx, secondaryTarget.position().add(0, secondaryTarget.getBbHeight() / 2, 0),
                        tertiaryTarget, property(TERTIARY_DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));
            }
        }
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false;
    }

    private void electrocute(CastContext ctx, Vec3 origin, Entity target, float damage) {
        if (MagicDamageSource.isEntityImmune(EBDamageSources.SHOCK, target)) {
            if (!ctx.world().isClientSide && ctx instanceof PlayerCastContext playerCtx) {
                playerCtx.caster().displayClientMessage(
                        Component.translatable("spell.resist", target.getName(), this.getDescriptionFormatted()), true);
            }
        } else {
            DamageSource source = ctx.caster() != null ? MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.SHOCK)
                    : target.damageSources().magic();
            target.hurt(source, damage);
        }

        if (ctx.world().isClientSide) {
            ParticleBuilder arc = ParticleBuilder.create(EBParticles.LIGHTNING);
            if (ctx.caster() != null) arc.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
            else arc.pos(origin);
            arc.target(target).spawn(ctx.world());

            ParticleBuilder.spawnShockParticles(ctx.world(), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.LIGHTNING, SpellType.ATTACK, SpellAction.POINT, 25, 5, 50)
                .add(DefaultProperties.RANGE, 10f)
                .add(PRIMARY_DAMAGE, 10f)
                .add(SECONDARY_DAMAGE, 8f)
                .add(TERTIARY_DAMAGE, 6f)
                .add(SECONDARY_RANGE, 5f)
                .add(TERTIARY_RANGE, 5f)
                .add(SECONDARY_MAX_TARGETS, 5)
                .add(TERTIARY_MAX_TARGETS, 2)
                .build();
    }
}
