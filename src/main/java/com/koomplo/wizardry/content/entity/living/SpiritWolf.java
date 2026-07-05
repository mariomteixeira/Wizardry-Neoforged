package com.koomplo.wizardry.content.entity.living;

import com.koomplo.wizardry.api.client.ParticleBuilder;
import com.koomplo.wizardry.api.content.item.ICastItem;
import com.koomplo.wizardry.setup.registries.EBEntities;
import com.koomplo.wizardry.setup.registries.EBSounds;
import com.koomplo.wizardry.setup.registries.client.EBParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Permanent summon: uses the vanilla wolf owner system instead of the minion data (1.12.2 EntitySpiritWolf). */
public class SpiritWolf extends Wolf {

    private static final int DISPEL_TIME = 10;

    private int dispelTimer = 0;

    public SpiritWolf(EntityType<? extends Wolf> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public SpiritWolf(Level level) {
        this(EBEntities.SPIRIT_WOLF.get(), level);
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

        // Allows the owner (but not other players) to dispel the spirit wolf by sneak-clicking with a wand
        if (this.isTame() && stack.getItem() instanceof ICastItem && this.getOwner() == player && player.isShiftKeyDown()) {
            // Prevents accidental double clicking
            if (this.tickCount > 20 && dispelTimer == 0) {
                this.dispelTimer++;
                this.playSound(EBSounds.ENTITY_SPIRIT_WOLF_VANISH.get(), 0.7F, random.nextFloat() * 0.4F + 1.0F);
                // This is necessary to prevent the wand's spell being cast when performing this action
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public Wolf getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        return null;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
}
