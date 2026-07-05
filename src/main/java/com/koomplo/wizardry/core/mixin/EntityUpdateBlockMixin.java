package com.koomplo.wizardry.core.mixin;

import com.koomplo.wizardry.core.UpdateBlockable;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityUpdateBlockMixin implements UpdateBlockable {

    @Unique
    private long ebwizardry$blockedUntil = Long.MIN_VALUE;

    @Override
    public void ebwizardry$blockUpdatesUntil(long gameTime) {
        this.ebwizardry$blockedUntil = gameTime;
    }

    @Override
    public long ebwizardry$getBlockedUntil() {
        return this.ebwizardry$blockedUntil;
    }
}
