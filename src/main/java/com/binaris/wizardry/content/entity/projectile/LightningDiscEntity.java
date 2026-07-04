package com.binaris.wizardry.content.entity.projectile;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.api.content.entity.projectile.MagicProjectileEntity;
import com.binaris.wizardry.api.content.util.MagicDamageSource;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.EBDamageSources;
import com.binaris.wizardry.setup.registries.EBEntities;
import com.binaris.wizardry.setup.registries.EBSounds;
import com.binaris.wizardry.setup.registries.Spells;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class LightningDiscEntity extends MagicProjectileEntity {

    public LightningDiscEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public LightningDiscEntity(Level world) {
        super(EBEntities.LIGHTNING_DISC.get(), world);
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (result instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            float damage = Spells.LIGHTNING_DISC.property(DefaultProperties.DAMAGE) * damageMultiplier;
            entity.hurt(MagicDamageSource.causeIndirectMagicDamage(this, this.getOwner(), EBDamageSources.SHOCK), damage);
        }

        this.playSound(EBSounds.ENTITY_LIGHTNING_DISC_HIT.get(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

        if (result.getType() == HitResult.Type.BLOCK) this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            for (int i = 0; i < 8; i++) {
                ParticleBuilder.create(EBParticles.SPARK).pos(this.getX() + random.nextFloat() * 2 - 1,
                        this.getY(), this.getZ() + random.nextFloat() * 2 - 1).spawn(level());
            }
        }

        // Cancels out the throwable slowdown so the disc glides
        this.setDeltaMovement(this.getDeltaMovement().scale(1 / 0.99));
    }

    @Override
    public float getSeekingStrength() {
        return Spells.LIGHTNING_DISC.property(DefaultProperties.SEEKING_STRENGTH);
    }

    @Override
    public int getLifeTime() {
        return 30;
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.AIR; // rendered by LightningDiscRenderer
    }
}
