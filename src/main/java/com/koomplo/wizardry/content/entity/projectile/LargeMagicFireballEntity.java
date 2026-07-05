package com.koomplo.wizardry.content.entity.projectile;

import com.koomplo.wizardry.api.content.spell.properties.SpellProperty;
import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

/** It's like {@link MagicFireballEntity}, but bigger — the wizardry version of vanilla's large fireball. */
public class LargeMagicFireballEntity extends MagicFireballEntity {

    public static final SpellProperty<Float> EXPLOSION_POWER = SpellProperty.floatProperty("explosion_power");

    public LargeMagicFireballEntity(EntityType<MagicFireballEntity> entityType, Level world) {
        super(entityType, world);
    }

    public LargeMagicFireballEntity(Level world) {
        super(EBEntities.LARGE_MAGIC_FIREBALL.get(), world);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (level().isClientSide) return;

        Entity entity = result.getEntity();
        MagicDamageSource.causeMagicDamage(this, entity, Spells.GREATER_FIREBALL.property(DefaultProperties.DAMAGE) * damageMultiplier, EBDamageSources.FIRE);
        if (!MagicDamageSource.isEntityImmune(EBDamageSources.FIRE, entity))
            entity.igniteForSeconds(Spells.GREATER_FIREBALL.property(DefaultProperties.DAMAGE).intValue());
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        // The explosion in onHit covers block effects
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(),
                    Spells.GREATER_FIREBALL.property(EXPLOSION_POWER), true, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }
}
