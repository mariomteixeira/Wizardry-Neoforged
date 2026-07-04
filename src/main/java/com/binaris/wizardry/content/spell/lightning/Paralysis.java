package com.binaris.wizardry.content.spell.lightning;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.event.EBLivingHurtEvent;
import com.binaris.wizardry.api.content.event.EBLivingTick;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.api.content.util.GeometryUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.EBMobEffects;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.Spells;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Paralysis extends RaySpell {

    /** Creatures at or below this health snap out of paralysis when they take damage. */
    public static final SpellProperty<Float> CRITICAL_HEALTH = SpellProperty.floatProperty("critical_health");
    /** Duration multiplier when the target is a player. (1.12.2 declared this but read it through the modifier
     * map by mistake, making it a no-op; here it applies as intended.) */
    public static final SpellProperty<Float> PLAYER_EFFECT_DURATION_MULTIPLIER = SpellProperty.floatProperty("player_effect_duration_multiplier");

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return false;

        if (ctx.world().isClientSide) {
            ParticleBuilder beam = ParticleBuilder.create(EBParticles.BEAM).color(0.2f, 0.6f, 1f);
            ParticleBuilder arc = ParticleBuilder.create(EBParticles.LIGHTNING);
            if (ctx.caster() != null) {
                beam.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
                arc.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
            } else {
                beam.pos(origin);
                arc.pos(origin);
            }
            beam.target(target).spawn(ctx.world());
            arc.target(target).spawn(ctx.world());
        }

        if (MagicDamageSource.isEntityImmune(EBDamageSources.SHOCK, target)) {
            if (!ctx.world().isClientSide && ctx instanceof PlayerCastContext playerCtx) {
                playerCtx.caster().displayClientMessage(
                        Component.translatable("spell.resist", target.getName(), this.getDescriptionFormatted()), true);
            }
        } else {
            DamageSource source = ctx.caster() != null ? MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.SHOCK)
                    : target.damageSources().magic();
            target.hurt(source, property(DefaultProperties.DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));
        }

        if (!ctx.world().isClientSide) {
            float durationMultiplier = target instanceof Player ? property(PLAYER_EFFECT_DURATION_MULTIPLIER) : 1.0f;
            target.addEffect(new MobEffectInstance(EBMobEffects.holder(EBMobEffects.PARALYSIS),
                    (int) (property(DefaultProperties.EFFECT_DURATION) * durationMultiplier * ctx.modifiers().get(SpellModifiers.DURATION)), 0));
        }
        return false;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        if (ctx.world().isClientSide && ctx.world().getBlockState(blockHit.getBlockPos()).isSolid()) {
            Vec3 vec = blockHit.getLocation().add(Vec3.atLowerCornerOf(blockHit.getDirection().getNormal()).scale(GeometryUtil.ANTI_Z_FIGHTING_OFFSET));
            ParticleBuilder.create(EBParticles.SCORCH).pos(vec).face(blockHit.getDirection()).color(0.4f, 0.8f, 1f).spawn(ctx.world());
        }
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        Vec3 endpoint = origin.add(direction.scale(property(DefaultProperties.RANGE) * ctx.modifiers().get(SpellModifiers.RANGE)));

        if (ctx.world().isClientSide) {
            ParticleBuilder.create(EBParticles.LIGHTNING).time(4).pos(origin).target(endpoint).scale(0.5f).spawn(ctx.world());
            ParticleBuilder.create(EBParticles.BEAM).color(0.2f, 0.6f, 1f).time(4).pos(origin).target(endpoint).spawn(ctx.world());
        }
        return true;
    }

    // Player movement lock lives in LocalPlayerMixin; mobs get their AI switched off while paralysed
    public static void onLivingTick(EBLivingTick event) {
        if (event.getEntity() instanceof Mob mob) {
            MobEffectInstance paralysis = mob.getEffect(EBMobEffects.holder(EBMobEffects.PARALYSIS));
            if (paralysis != null) {
                mob.setNoAi(paralysis.getDuration() > 1);
            }
        }
    }

    // Paralysed creatures snap out of paralysis when they take critical damage
    public static void onLivingHurt(EBLivingHurtEvent event) {
        if (event.isCanceled()) return;
        LivingEntity entity = event.getDamagedEntity();
        if (entity.hasEffect(EBMobEffects.holder(EBMobEffects.PARALYSIS))
                && entity.getHealth() - event.getAmount() <= Spells.PARALYSIS.property(CRITICAL_HEALTH)
                && !entity.level().isClientSide) {
            entity.removeEffect(EBMobEffects.holder(EBMobEffects.PARALYSIS));
            if (entity instanceof Mob mob) mob.setNoAi(false);
        }
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.LIGHTNING, SpellType.ALTERATION, SpellAction.POINT, 20, 10, 60)
                .add(DefaultProperties.RANGE, 10f)
                .add(DefaultProperties.DAMAGE, 4f)
                .add(DefaultProperties.EFFECT_DURATION, 100)
                .add(CRITICAL_HEALTH, 4f)
                .add(PLAYER_EFFECT_DURATION_MULTIPLIER, 0.5f)
                .build();
    }
}
