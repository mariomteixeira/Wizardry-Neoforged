package com.koomplo.wizardry.core.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes {@code LivingEntity#dead} so resurrection can bring players back (1.12.2 set isDead directly). */
@Mixin(LivingEntity.class)
public interface LivingEntityDeadAccessor {

    @Accessor("dead")
    void EBWIZARDRY$setDead(boolean dead);
}
