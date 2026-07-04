package com.binaris.wizardry.content.spell.sorcery;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.CastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.spell.properties.SpellProperty;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.abstr.RaySpell;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GuardianBeam extends RaySpell {

    public static final SpellProperty<Integer> AIR_DEPLETION = SpellProperty.intProperty("air_depletion");

    @Override
    protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration) {
        if (ticksInUse % 50 == 1) super.playSound(world, x, y, z, ticksInUse, duration);
    }

    @Override
    protected void playSound(Level world, LivingEntity entity, int castTicks, int duration) {
        if (castTicks % 50 == 1) super.playSound(world, entity, castTicks, duration);
    }

    @Override
    protected boolean onEntityHit(CastContext ctx, EntityHitResult entityHit, Vec3 origin) {
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return true;

        if (ctx.castingTicks() % 50 == 1) {
            DamageSource source = ctx.caster() != null ? MagicDamageSource.causeDirectMagicDamage(ctx.caster(), EBDamageSources.MAGIC)
                    : target.damageSources().magic();
            EntityUtil.attackEntityWithoutKnockback(target, source,
                    property(DefaultProperties.DAMAGE) * ctx.modifiers().get(SpellModifiers.POTENCY));

            if (!target.canBreatheUnderwater() && !target.hasEffect(MobEffects.WATER_BREATHING)) {
                target.setAirSupply(Math.max(-20, target.getAirSupply() - property(AIR_DEPLETION)));
            }
        }

        if (ctx.world().isClientSide) {
            float t = (ctx.castingTicks() % 50) / 50f;
            float yellowness = t * t;
            int r = 64 + (int) (yellowness * 191.0F);
            int g = 32 + (int) (yellowness * 191.0F);
            int b = 128 - (int) (yellowness * 64.0F);

            if (ctx.castingTicks() % 3 == 0) {
                ParticleBuilder beam = ParticleBuilder.create(EBParticles.GUARDIAN_BEAM);
                if (ctx.caster() != null) beam.entity(ctx.caster()).pos(origin.subtract(ctx.caster().position()));
                else beam.pos(origin);
                beam.target(target).color(r, g, b).spawn(ctx.world());
            }

            Vec3 direction = target.position().add(0, target.getBbHeight() / 2, 0).subtract(origin);
            Vec3 pos = origin.add(direction.scale(ctx.world().random.nextFloat()));
            ParticleBuilder.create(EBParticles.MAGIC_BUBBLE, ctx.world().random, pos.x, pos.y, pos.z, 0.15, false).spawn(ctx.world());
        }
        return true;
    }

    @Override
    protected boolean onBlockHit(CastContext ctx, BlockHitResult blockHit, Vec3 origin) {
        return false;
    }

    @Override
    protected boolean onMiss(CastContext ctx, Vec3 origin, Vec3 direction) {
        return false; // Only works on hit
    }

    @Override
    public boolean isInstantCast() {
        return false;
    }

    @Override
    protected void spawnParticle(CastContext ctx, double x, double y, double z, double vx, double vy, double vz) {
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.SORCERY, SpellType.ATTACK, SpellAction.POINT, 10, 0, 50)
                .add(DefaultProperties.RANGE, 12f)
                .add(DefaultProperties.DAMAGE, 5f)
                .add(AIR_DEPLETION, 70)
                .build();
    }
}
