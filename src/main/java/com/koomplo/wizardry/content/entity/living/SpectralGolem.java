package com.koomplo.wizardry.content.entity.living;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

public class SpectralGolem extends IronGolem {

    public SpectralGolem(EntityType<? extends IronGolem> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public SpectralGolem(Level level) {
        this(EBEntities.SPECTRAL_GOLEM.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        // Ambient dust particles (1.12.2 EntitySpectralGolem)
        if (level().isClientSide) {
            double x = this.getX() - this.getBbWidth() / 2 + this.random.nextFloat() * this.getBbWidth();
            double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
            double z = this.getZ() - this.getBbWidth() / 2 + this.random.nextFloat() * this.getBbWidth();
            ParticleBuilder.create(EBParticles.DUST).pos(x, y, z).color(0.7f, 0.9f, 1f).shaded(true).spawn(level());
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }
}
