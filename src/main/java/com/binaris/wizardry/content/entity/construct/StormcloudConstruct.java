package com.binaris.wizardry.content.entity.construct;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.entity.construct.ScaledConstructEntity;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.EBSounds;
import com.binaris.wizardry.setup.registries.Spells;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Floating storm cloud that zaps whatever passes underneath (1.12.2 EntityStormcloud). */
public class StormcloudConstruct extends ScaledConstructEntity {

    public StormcloudConstruct(EntityType<?> type, Level level) {
        super(type, level);
    }

    public StormcloudConstruct(Level level) {
        this(EBEntities.STORMCLOUD.get(), level);
    }

    @Override
    protected boolean shouldScaleHeight() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        this.move(MoverType.SELF, new Vec3(getDeltaMovement().x, 0, getDeltaMovement().z));

        if (level().isClientSide) {
            // Ensures cloud density stays the same for different sizes
            float areaFactor = (getBbWidth() * getBbWidth()) / 36;

            for (int i = 0; i < 2 * areaFactor; i++) {
                ParticleBuilder.create(EBParticles.CLOUD, this)
                        .color(0.3f, 0.3f, 0.3f).shaded(true).spawn(level());
            }
        }

        // TODO ring_stormcloud (marco 6/7): nuvem persegue o alvo mais próximo e cada raio estende o lifetime

        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().expandTowards(0, -10, 0));

        targets.removeIf(t -> !this.isValidTarget(t));

        float damage = Spells.STORMCLOUD.property(DefaultProperties.DAMAGE) * this.damageMultiplier;

        for (LivingEntity target : targets) {

            // Use target's lifetime so they don't all get hit at once, looks better
            if (target.tickCount % 150 == 0) {

                if (!level().isClientSide) {
                    EntityUtil.attackEntityWithoutKnockback(target,
                            MagicDamageSource.causeIndirectMagicDamage(this, this.getCaster(), EBDamageSources.SHOCK), damage);
                } else {
                    ParticleBuilder.create(EBParticles.LIGHTNING).pos(target.getX(), getY() + getBbHeight() / 2, target.getZ())
                            .target(target).scale(2).spawn(level());
                    ParticleBuilder.spawnShockParticles(level(), target.getX(), target.getY() + target.getBbHeight(), target.getZ());
                }

                target.playSound(EBSounds.ENTITY_STORMCLOUD_THUNDER.get(), 1, 1.6f);
                target.playSound(EBSounds.ENTITY_STORMCLOUD_ATTACK.get(), 1, 1);
            }
        }
    }
}
