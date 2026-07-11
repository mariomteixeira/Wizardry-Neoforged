package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.effect.MagicMobEffect;
import com.koomplo.wizardry.api.content.event.EBLivingTick;
import com.koomplo.wizardry.api.content.util.EntityUtil;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.core.UpdateBlockable;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.EBMobEffects;
import com.koomplo.wizardry.setup.registries.Spells;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.koomplo.wizardry.core.config.EBServerConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SlowTimeMobEffect extends MagicMobEffect {

    public SlowTimeMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x5be3bb);
    }

    private static double getEffectRadius() {
        return Spells.SLOW_TIME.property(DefaultProperties.EFFECT_RADIUS);
    }

    public static void onLivingTick(EBLivingTick event) {
        LivingEntity host = event.getEntity();
        MobEffectInstance effect = host.getEffect(EBMobEffects.holder(EBMobEffects.SLOW_TIME));
        if (effect != null) performEffectConsistent(host, effect.getAmplifier());
    }

    // Port of 1.12.2 PotionSlowTime#performEffectConsistent. Blocking uses a per-entity game-time deadline
    // (see UpdateBlockable) refreshed here every host tick, so it self-expires when the host stops refreshing.
    private static void performEffectConsistent(LivingEntity host, int strength) {
        Level world = host.level();

        boolean stopTime = host instanceof Player player && ArtifactChannel.isEquipped(player, EBItems.CHARM_STOP_TIME.get());

        int interval = strength * 4 + 6;

        List<Entity> targetsInRange = EntityUtil.getEntitiesWithinRadius(getEffectRadius(), host.getX(), host.getY(), host.getZ(), world, Entity.class);
        targetsInRange.remove(host);
        // Other entities with the slow time effect are unaffected
        targetsInRange.removeIf(t -> t instanceof LivingEntity living && living.hasEffect(EBMobEffects.holder(EBMobEffects.SLOW_TIME)));
        if (!EBServerConfig.SLOW_TIME_AFFECTS_PLAYERS.get()) targetsInRange.removeIf(t -> t instanceof Player);
        targetsInRange.removeIf(t -> t instanceof AbstractArrow && t.isInWall());

        for (Entity entity : targetsInRange) {

            // If time is stopped, block every update; otherwise block all updates except every [interval] ticks
            boolean blocked = stopTime || host.tickCount % interval != 0;
            ((UpdateBlockable) entity).ebwizardry$blockUpdatesUntil(blocked ? world.getGameTime() + 2 : world.getGameTime());

            if (!stopTime && world.isClientSide) {
                // Client-side movement interpolation (smoothing), straight from 1.12.2
                Vec3 motion = entity.getDeltaMovement();
                if (entity.onGround()) {
                    entity.setDeltaMovement(motion.x, 0, motion.z); // Don't ask. It just works.
                    motion = entity.getDeltaMovement();
                }

                if (blocked) {
                    double x = entity.getX() + motion.x / interval;
                    double y = entity.getY() + motion.y / interval;
                    double z = entity.getZ() + motion.z / interval;

                    entity.xo = entity.getX();
                    entity.yo = entity.getY();
                    entity.zo = entity.getZ();

                    entity.setPos(x, y, z);
                } else {
                    // The entity already covered most of this distance while blocked
                    entity.setPos(entity.getX() + motion.x / interval,
                            entity.getY() + motion.y / interval,
                            entity.getZ() + motion.z / interval);

                    entity.xo = entity.getX() - motion.x / interval;
                    entity.yo = entity.getY() - motion.y / interval;
                    entity.zo = entity.getZ() - motion.z / interval;
                }
            }

            if (world.isClientSide && host.tickCount % 2 == 0) {
                int lifetime = 15;
                double dx = (world.random.nextDouble() - 0.5D) * 2 * entity.getBbWidth();
                double dy = (world.random.nextDouble() - 0.5D) * 2 * entity.getBbWidth();
                double dz = (world.random.nextDouble() - 0.5D) * 2 * entity.getBbWidth();
                double x = entity.getX() + dx;
                double y = (entity instanceof Projectile ? entity.getY() : entity.getY() + entity.getBbHeight() / 2) + dy;
                double z = entity.getZ() + dz;
                ParticleBuilder.create(EBParticles.DUST)
                        .pos(x, y, z)
                        .velocity(-dx / lifetime, -dy / lifetime, -dz / lifetime)
                        .color(0x5b, 0xe3, 0xbb).time(lifetime).spawn(world);
            }
        }
    }

    @Override
    public void spawnCustomParticle(Level world, double x, double y, double z) {
    }
}
