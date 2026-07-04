package com.binaris.wizardry.core.mixin;

import com.binaris.wizardry.core.UpdateBlockable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin {

    /**
     * Slow time: skips the whole entity update while the entity's block deadline (set every tick by
     * {@code SlowTimeMobEffect}) is still in the future. Equivalent to the Forge 1.12.2 {@code updateBlocked} hook
     * in {@code World#updateEntityWithOptionalForce}, but self-expiring.
     */
    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void ebwizardry$blockTimeSlowedEntities(java.util.function.Consumer<T> consumer, T entity, CallbackInfo ci) {
        if (entity instanceof UpdateBlockable blockable
                && ((Level) (Object) this).getGameTime() < blockable.ebwizardry$getBlockedUntil()) {
            ci.cancel();
        }
    }
}
