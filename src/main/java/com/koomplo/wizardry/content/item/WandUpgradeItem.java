package com.koomplo.wizardry.content.item;

import com.koomplo.wizardry.api.content.event.EBLivingDeathEvent;
import com.koomplo.wizardry.api.content.item.IManaItem;
import com.koomplo.wizardry.api.content.util.InventoryUtil;
import com.koomplo.wizardry.api.content.util.CastItemDataHelper;
import com.koomplo.wizardry.core.config.EBServerConfig;
import com.koomplo.wizardry.core.integrations.ArtifactChannel;
import com.koomplo.wizardry.setup.registries.EBItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WandUpgradeItem extends Item {
    public static final float RING_SIPHONING_BONUS = 1.3f;

    public WandUpgradeItem(Properties properties) {
        super(properties);
    }

    public static void onPlayerKillMob(EBLivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        // Need to be a ManaStoringItem and without full mana
        // only can recharge 1 item for death
        for (ItemStack itemStack : InventoryUtil.getHotBarAndOffhand(player)) {
            if (itemStack.getItem() instanceof IManaItem manaItem && !manaItem.isManaFull(itemStack)) {
                if (CastItemDataHelper.getUpgradeLevel(itemStack, EBItems.SIPHON_UPGRADE.get()) <= 0) continue;

                float mana = EBServerConfig.SIPHON_MANA_PER_LEVEL.get()
                        * CastItemDataHelper.getUpgradeLevel(itemStack, EBItems.SIPHON_UPGRADE.get())
                        + player.level().random.nextInt(EBServerConfig.SIPHON_MANA_PER_LEVEL.get());

                if (ArtifactChannel.isEquipped(player, EBItems.RING_SIPHONING.get())) mana *= RING_SIPHONING_BONUS;
                manaItem.rechargeMana(itemStack, (int) mana);
                break;

            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable(getOrCreateDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
