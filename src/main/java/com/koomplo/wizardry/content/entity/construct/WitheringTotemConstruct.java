package com.koomplo.wizardry.content.entity.construct;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.entity.construct.ScaledConstructEntity;
import com.koomplo.wizardry.api.content.util.BlockUtil;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.api.content.util.GeometryUtil;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * Floating totem that drains life from nearby enemies and explodes with the accumulated damage when it
 * expires (1.12.2 EntityWitheringTotem).
 */
public class WitheringTotemConstruct extends ScaledConstructEntity {

    private static final int PERIMETER_PARTICLE_DENSITY = 6;

    private static final EntityDataAccessor<Float> HEALTH_DRAINED =
            SynchedEntityData.defineId(WitheringTotemConstruct.class, EntityDataSerializers.FLOAT);

    public WitheringTotemConstruct(EntityType<?> type, Level level) {
        super(type, level);
        this.setBaseSize(1, 1); // The area of effect is kind of 'outside' the entity
    }

    public WitheringTotemConstruct(Level level) {
        this(EBEntities.WITHERING_TOTEM.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEALTH_DRAINED, 0f);
    }

    public float getHealthDrained() {
        return entityData.get(HEALTH_DRAINED);
    }

    public void addHealthDrained(float health) {
        entityData.set(HEALTH_DRAINED, getHealthDrained() + health);
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
            ClientSpellSoundManager.playMovingSound(this, EBSounds.ENTITY_WITHERING_TOTEM_AMBIENT.get(),
                    SoundSource.PLAYERS, 1, 1, true);
        }

        // Lifetime end goes through despawn() so the explosion happens (the base class discards silently);
        // runs on both sides — the explosion damage is server-side, the sphere particle is client-side
        if (this.tickCount > lifetime && lifetime != -1) {
            this.despawn();
            return;
        }

        super.tick();

        double radius = Spells.WITHERING_TOTEM.property(DefaultProperties.EFFECT_RADIUS) * getSizeMultiplier();

        if (level().isClientSide) {

            ParticleBuilder.create(EBParticles.DUST, random, getX(), getY() + 0.2, getZ(), 0.3, false)
                    .velocity(0, -0.02 - level().random.nextFloat() * 0.01, 0).color(0xf575f5).fade(0x382366).spawn(level());

            for (int i = 0; i < PERIMETER_PARTICLE_DENSITY; i++) {

                float angle = ((float) Math.PI * 2) / PERIMETER_PARTICLE_DENSITY * (i + random.nextFloat());

                double x = getX() + radius * Mth.sin(angle);
                double z = getZ() + radius * Mth.cos(angle);

                Integer y = BlockUtil.getNearestSurface(level(), BlockPos.containing(x, getY(), z), Direction.UP, 5, true,
                        BlockUtil.SurfaceCriteria.COLLIDABLE);

                if (y != null) {
                    ParticleBuilder.create(EBParticles.DUST).pos(x, y, z).velocity(0, 0.01, 0)
                            .color(0xf575f5).fade(0x382366).spawn(level());
                }
            }
        }

        List<LivingEntity> nearby = EntityUtil.getLivingWithinRadius(radius, getX(), getY(), getZ(), level());
        nearby.removeIf(e -> !isValidTarget(e));
        nearby.sort(Comparator.comparingDouble(this::distanceToSqr));

        int targetsRemaining = Spells.WITHERING_TOTEM.property(DefaultProperties.MAX_TARGETS)
                + (int) ((damageMultiplier - 1) / EBServerConfig.POTENCY_INCREASE_PER_TIER.get());

        while (!nearby.isEmpty() && targetsRemaining > 0) {

            LivingEntity target = nearby.remove(0);

            if (EntityUtil.isLiving(target)) {

                if (target.tickCount % target.invulnerableDuration == 1) {
                    float damage = Spells.WITHERING_TOTEM.property(DefaultProperties.DAMAGE);

                    if (EntityUtil.attackEntityWithoutKnockback(target,
                            MagicDamageSource.causeIndirectMagicDamage(this, getCaster(), EBDamageSources.WITHER), damage)) {
                        if (!level().isClientSide) addHealthDrained(damage);
                    }
                }

                targetsRemaining--;

                if (level().isClientSide) {

                    Vec3 centre = GeometryUtil.getCentre(this);
                    Vec3 pos = GeometryUtil.getCentre(target);

                    ParticleBuilder.create(EBParticles.BEAM).pos(centre).target(target)
                            .color(0.1f + 0.2f * level().random.nextFloat(), 0f, 0.3f).spawn(level());

                    for (int i = 0; i < 3; i++) {
                        ParticleBuilder.create(EBParticles.DUST, random, pos.x, pos.y, pos.z, 0.3, false)
                                .velocity(pos.subtract(centre).normalize().scale(-0.1)).color(0x0c0024).fade(0x610017).spawn(level());
                    }
                }
            }
        }
    }

    @Override
    public void despawn() {

        double radius = Spells.WITHERING_TOTEM.property(DefaultProperties.EFFECT_RADIUS) * getSizeMultiplier();

        List<LivingEntity> nearby = EntityUtil.getLivingWithinRadius(radius, getX(), getY(), getZ(), level());
        nearby.removeIf(e -> !isValidTarget(e));

        float damage = Math.min(getHealthDrained() * 0.2f, Spells.WITHERING_TOTEM.property(DefaultProperties.MAX_EXPLOSION_DAMAGE));

        for (LivingEntity target : nearby) {

            if (!level().isClientSide && EntityUtil.attackEntityWithoutKnockback(target,
                    MagicDamageSource.causeIndirectMagicDamage(this, getCaster(), EBDamageSources.MAGIC), damage)) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                        Spells.WITHERING_TOTEM.property(DefaultProperties.EFFECT_DURATION),
                        Spells.WITHERING_TOTEM.property(DefaultProperties.EFFECT_STRENGTH)));
            }
        }

        if (level().isClientSide) {
            ParticleBuilder.create(EBParticles.SPHERE).pos(GeometryUtil.getCentre(this)).scale((float) radius)
                    .color(0xbe1a53).fade(0x210f4a).spawn(level());
        }

        this.playSound(EBSounds.ENTITY_WITHERING_TOTEM_EXPLODE.get(), 1, 1);
        super.despawn();
    }
}
