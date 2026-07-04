package com.binaris.wizardry.core;

/**
 * Duck interface injected into {@link net.minecraft.world.entity.Entity} by
 * {@code EntityUpdateBlockMixin}. Replaces the Forge 1.12.2 {@code Entity#updateBlocked} patch used by slow time.
 * <p>
 * The block is a game-time deadline rather than a plain flag: whoever slows the entity must keep refreshing the
 * deadline every tick, so entities can never stay frozen when the effect holder dies, logs out or leaves range
 * (the 1.12.2 implementation had a FIXME for exactly that).
 */
public interface UpdateBlockable {

    /** Blocks this entity's updates until the given game time (exclusive). */
    void ebwizardry$blockUpdatesUntil(long gameTime);

    long ebwizardry$getBlockedUntil();
}
