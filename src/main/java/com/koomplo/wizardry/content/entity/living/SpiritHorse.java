package com.koomplo.wizardry.content.entity.living;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.item.ICastItem;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.EBSounds;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Permanent summon: uses the vanilla horse owner system instead of the minion data (1.12.2 EntitySpiritHorse). */
public class SpiritHorse extends Horse {

    private static final int DISPEL_TIME = 10;

    private int idleTimer = 0;
    private int dispelTimer = 0;

    public SpiritHorse(EntityType<? extends Horse> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0D);
        this.setHealth(24.0F);
    }

    public SpiritHorse(Level level) {
        this(EBEntities.SPIRIT_HORSE.get(), level);
    }

    @Override
    protected void randomizeAttributes(@NotNull RandomSource random) {
        // Fixed stats (1.12.2 spirit horse always had 24 health)
    }

    @Override
    public void openCustomInventoryScreen(@NotNull Player player) {
        // No inventory GUI (1.12.2 openGUI no-op) — no armor or chest for spirit horses
    }

    @Override
    public void tick() {
        super.tick();

        if (dispelTimer > 0) {
            if (dispelTimer++ > DISPEL_TIME) this.discard();
        }

        if (level().isClientSide) {
            if (this.tickCount == 1) spawnAppearParticles();

            // Ambient dust particle effect
            double x = this.getX() - this.getBbWidth() / 2 + this.random.nextFloat() * this.getBbWidth();
            double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
            double z = this.getZ() - this.getBbWidth() / 2 + this.random.nextFloat() * this.getBbWidth();
            ParticleBuilder.create(EBParticles.DUST).pos(x, y, z).color(0.8f, 0.8f, 1f).shaded(true).spawn(level());
        }

        // Spirit horse disappears a short time after being dismounted
        if (!this.isVehicle()) {
            this.idleTimer++;
        } else if (this.idleTimer > 0) {
            this.idleTimer = 0;
        }

        if (this.idleTimer > 200 && dispelTimer == 0) {
            this.playSound(EBSounds.ENTITY_SPIRIT_HORSE_VANISH.get(), 0.7F, random.nextFloat() * 0.4F + 1.0F);
            this.dispelTimer++;
        }
    }

    private void spawnAppearParticles() {
        for (int i = 0; i < 15; i++) {
            double x = this.getX() - this.getBbWidth() / 2 + this.random.nextFloat() * this.getBbWidth();
            double y = this.getY() + this.getBbHeight() * this.random.nextFloat() + 0.2f;
            double z = this.getZ() - this.getBbWidth() / 2 + this.random.nextFloat() * this.getBbWidth();
            ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).color(0.8f, 0.8f, 1f).spawn(level());
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Allows the owner (but not other players) to dispel the spirit horse by sneak-clicking with a wand
        // (sneak-clicking, because clicking mounts the horse in this case)
        if (stack.getItem() instanceof ICastItem && this.getOwnerUUID() != null
                && this.getOwnerUUID().equals(player.getUUID()) && player.isShiftKeyDown()) {
            // Prevents accidental double clicking
            if (this.tickCount > 20 && dispelTimer == 0) {
                this.dispelTimer++;
                this.playSound(EBSounds.ENTITY_SPIRIT_HORSE_VANISH.get(), 0.7F, random.nextFloat() * 0.4F + 1.0F);
                // This is necessary to prevent the wand's spell being cast when performing this action
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            return InteractionResult.FAIL;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canMate(@NotNull Animal otherAnimal) {
        return false;
    }

    @Override
    public Horse getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        return null;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
}
