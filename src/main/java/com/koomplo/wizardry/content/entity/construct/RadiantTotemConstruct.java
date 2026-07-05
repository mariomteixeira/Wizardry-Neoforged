package com.koomplo.wizardry.content.entity.construct;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.entity.construct.ScaledConstructEntity;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.core.AllyDesignation;
import com.koomplo.wizardry.core.ClientSpellSoundManager;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.EBSounds;
import com.koomplo.wizardry.setup.registries.Spells;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Floating totem that heals nearby allies and burns nearby enemies with radiant beams (1.12.2 EntityRadiantTotem). */
public class RadiantTotemConstruct extends ScaledConstructEntity {

    private static final int PERIMETER_PARTICLE_DENSITY = 6;

    public RadiantTotemConstruct(EntityType<?> type, Level level) {
        super(type, level);
        this.setBaseSize(1, 1); // The area of effect is kind of 'outside' the entity
    }

    public RadiantTotemConstruct(Level level) {
        this(EBEntities.RADIANT_TOTEM.get(), level);
    }

    @Override
    protected boolean shouldScaleWidth() {
        return false;
    }

    @Override
    protected boolean shouldScaleHeight() {
        return false;
    }

    @Override
    public void tick() {
        if (level().isClientSide && this.tickCount == 1) {
            ClientSpellSoundManager.playMovingSound(this, EBSounds.ENTITY_RADIANT_TOTEM_AMBIENT.get(),
                    SoundSource.PLAYERS, 1, 1, true);
        }

        // Lifetime end goes through despawn() so the vanish sound plays (the base class discards silently)
        if (this.tickCount > lifetime && lifetime != -1) {
            this.despawn();
            return;
        }

        super.tick();

        double radius = Spells.RADIANT_TOTEM.property(DefaultProperties.EFFECT_RADIUS) * getSizeMultiplier();

        if (level().isClientSide) {

            ParticleBuilder.create(EBParticles.DUST, random, getX(), getY() + 0.2, getZ(), 0.3, false)
                    .velocity(0, -0.02 - level().random.nextFloat() * 0.01, 0).color(0xffffff).fade(0xffec90).spawn(level());

            for (int i = 0; i < PERIMETER_PARTICLE_DENSITY; i++) {

                float angle = ((float) Math.PI * 2) / PERIMETER_PARTICLE_DENSITY * (i + random.nextFloat());

                double x = getX() + radius * Mth.sin(angle);
                double z = getZ() + radius * Mth.cos(angle);

                Integer y = BlockUtil.getNearestSurface(level(), BlockPos.containing(x, getY(), z), Direction.UP, 5, true,
                        BlockUtil.SurfaceCriteria.COLLIDABLE);

                if (y != null) {
                    ParticleBuilder.create(EBParticles.DUST).pos(x, y, z).velocity(0, 0.01, 0)
                            .color(0xffffff).fade(0xffec90).spawn(level());
                }
            }
        }

        List<LivingEntity> nearby = EntityUtil.getLivingWithinRadius(radius, getX(), getY(), getZ(), level());
        nearby.sort(Comparator.comparingDouble(this::distanceToSqr));

        List<LivingEntity> nearbyAllies = nearby.stream().filter(e -> e == getCaster()
                || AllyDesignation.isAllied(getCaster(), e)).collect(Collectors.toList());
        nearby.removeAll(nearbyAllies);

        int targetsRemaining = Spells.RADIANT_TOTEM.property(DefaultProperties.MAX_TARGETS)
                + (int) ((damageMultiplier - 1) / EBServerConfig.POTENCY_INCREASE_PER_TIER.get());

        while (!nearbyAllies.isEmpty() && targetsRemaining > 0) {

            LivingEntity ally = nearbyAllies.remove(0);

            if (ally.getHealth() < ally.getMaxHealth()) {
                // Slightly slower than healing aura, and it only does 1 at a time (without potency modifiers)
                if (ally.tickCount % 8 == 0) ally.heal(Spells.RADIANT_TOTEM.property(DefaultProperties.HEALTH));
                targetsRemaining--;

                if (level().isClientSide) {
                    ParticleBuilder.create(EBParticles.BEAM).pos(this.position().add(0, getBbHeight() / 2, 0))
                            .target(ally).color(1f, 0.6f + 0.3f * level().random.nextFloat(), 0.2f).spawn(level());
                }
            }
        }

        while (!nearby.isEmpty() && targetsRemaining > 0) {

            LivingEntity target = nearby.remove(0);

            if (EntityUtil.isLiving(target) && isValidTarget(target)) {

                if (target.tickCount % target.invulnerableDuration == 1) {
                    float damage = Spells.RADIANT_TOTEM.property(DefaultProperties.DAMAGE);
                    EntityUtil.attackEntityWithoutKnockback(target,
                            MagicDamageSource.causeIndirectMagicDamage(this, getCaster(), EBDamageSources.RADIANT), damage);
                }

                targetsRemaining--;

                if (level().isClientSide) {
                    ParticleBuilder.create(EBParticles.BEAM).pos(this.position().add(0, getBbHeight() / 2, 0))
                            .target(target).color(1f, 0.6f + 0.3f * level().random.nextFloat(), 0.2f).spawn(level());
                }
            }
        }
    }

    @Override
    public void despawn() {
        this.playSound(EBSounds.ENTITY_RADIANT_TOTEM_VANISH.get(), 1, 1);
        super.despawn();
    }
}
