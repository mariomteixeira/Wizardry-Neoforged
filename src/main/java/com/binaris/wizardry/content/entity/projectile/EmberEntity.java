package com.binaris.wizardry.content.entity.projectile;

import com.binaris.wizardry.api.content.entity.projectile.MagicProjectileEntity;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.content.spell.fire.Disintegration;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/** Burning ember left behind by disintegrated creatures (1.12.2 EntityEmber): lands, lingers and ignites on contact. */
public class EmberEntity extends MagicProjectileEntity {

    private int extraLifetime;
    private boolean grounded;

    public EmberEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public EmberEntity(Level world, LivingEntity caster) {
        super(EBEntities.EMBER.get(), caster, world);
        this.extraLifetime = this.random.nextInt(30);
    }

    @Override
    public int getLifeTime() {
        return Spells.DISINTEGRATION.property(Disintegration.EMBER_LIFETIME) + extraLifetime;
    }

    @Override
    protected @NotNull net.minecraft.world.item.Item getDefaultItem() {
        return net.minecraft.world.item.Items.BLAZE_POWDER; // rendering uses the custom ember texture
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        // Embers stick to the ground instead of vanishing
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            Vec3 motion = this.getDeltaMovement();
            switch (blockHit.getDirection().getAxis()) {
                case X -> this.setDeltaMovement(0, motion.y, motion.z);
                case Y -> {
                    this.setDeltaMovement(motion.x, 0, motion.z);
                    this.grounded = true;
                }
                case Z -> this.setDeltaMovement(motion.x, motion.y, 0);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (grounded) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.5, motion.y, motion.z * 0.5);
        }

        if (!level().isClientSide) {
            level().getEntities(this, this.getBoundingBox(), e -> e instanceof LivingEntity living && living.getHealth() > 0 && e != getOwner())
                    .forEach(e -> e.igniteForSeconds(Spells.DISINTEGRATION.property(DefaultProperties.EFFECT_DURATION)));
        }

        if (this.random.nextFloat() > (float) this.tickCount / this.getLifeTime()) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z);
        }
    }
}
