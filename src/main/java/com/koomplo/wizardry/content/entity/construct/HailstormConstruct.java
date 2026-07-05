package com.koomplo.wizardry.content.entity.construct;

import com.koomplo.wizardry.api.content.entity.construct.ScaledConstructEntity;
import com.koomplo.wizardry.content.entity.projectile.IceShardEntity;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class HailstormConstruct extends ScaledConstructEntity {

    public HailstormConstruct(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setBaseSize(4, 5);
    }

    public HailstormConstruct(Level level) {
        super(EBEntities.HAILSTORM.get(), level);
        this.lifetime = Spells.HAILSTORM.property(DefaultProperties.DURATION);
        this.setBaseSize(4, 5);
    }

    @Override
    public void tick() {

        super.tick();

        if (!this.level().isClientSide) {

            double x = getX() + (level().random.nextDouble() - 0.5D) * (double) getBbWidth();
            double y = getY() + level().random.nextDouble() * (double) getBbHeight();
            double z = getZ() + (level().random.nextDouble() - 0.5D) * (double) getBbWidth();

            IceShardEntity iceshard = new IceShardEntity(level());
            iceshard.setPos(x, y, z);

            iceshard.setDeltaMovement(Mth.cos((float) Math.toRadians(this.getYRot() + 90)), -0.6, Mth.sin((float) Math.toRadians(this.getYRot() + 90)));
            iceshard.setOwner(this.getCaster());
            iceshard.damageMultiplier = this.damageMultiplier;

            this.level().addFreshEntity(iceshard);
        }
    }

    @Override
    protected boolean shouldScaleHeight() {
        return false;
    }
}
