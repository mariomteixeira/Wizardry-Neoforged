package com.binaris.wizardry.content.item;

import com.binaris.wizardry.api.client.ParticleBuilder;
import com.binaris.wizardry.setup.registries.EBSounds;
import com.binaris.wizardry.setup.registries.client.EBParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PurifyingElixirItem extends Item {
    private static final int USE_DURATION = 32;

    public PurifyingElixirItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (level.isClientSide) {
            ParticleBuilder.spawnHealParticles(level, livingEntity);

            double x = livingEntity.xo + level.random.nextDouble() * 2 - 1;
            double y = livingEntity.yo + livingEntity.getEyeHeight() - 0.5 + level.random.nextDouble();
            double z = livingEntity.zo + level.random.nextDouble() * 2 - 1;
            ParticleBuilder.create(EBParticles.SPARKLE).pos(x, y, z).velocity(0, 0.14, 0)
                    .color(0x0f001b).time(20 + level.random.nextInt(12)).spawn(level);
            ParticleBuilder.create(EBParticles.DARK_MAGIC).pos(x, y, z).color(0x0f001b).spawn(level);
        } else {
            livingEntity.removeAllEffects();
        }

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                EBSounds.ITEM_PURIFYING_ELIXIR_DRINK.get(), SoundSource.PLAYERS, 0.5f, 1f);

        if (livingEntity instanceof ServerPlayer serverPlayer)
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);

        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        String desc = flag.isAdvanced() ? ".desc_extended" : ".desc";
        tooltip.add(Component.translatable(getOrCreateDescriptionId() + desc).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }
}
