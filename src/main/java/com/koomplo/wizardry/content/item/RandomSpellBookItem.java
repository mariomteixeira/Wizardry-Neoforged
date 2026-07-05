package com.koomplo.wizardry.content.item;

import com.koomplo.wizardry.api.content.event.EBItemPickupEvent;
import com.koomplo.wizardry.api.content.util.RegistryUtils;
import com.koomplo.wizardry.core.EBLogger;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RandomSpellBookItem extends Item {
    public RandomSpellBookItem(Properties properties) {
        super(properties);
    }

    public static void create(Level level, Player player, ItemStack original) {
        ItemStack stack;
        CustomData customData = original.get(DataComponents.CUSTOM_DATA);
        original.shrink(1);
        if (level.isClientSide) return;

        if (customData == null || !customData.contains("LootTable")) {
            stack = RegistryUtils.setSpell(new ItemStack(EBItems.SPELL_BOOK.get()), Spells.MAGIC_MISSILE);
            spawn(level, player.blockPosition(), stack);
            EBLogger.error("Attempted to use a RandomSpellBookItem without a LootTable NBT tag.");
            return;
        }

        String lootTableId = customData.copyTag().getString("LootTable");
        ResourceLocation lootTableLocation = ResourceLocation.tryParse(lootTableId);

        if (lootTableLocation == null) {
            stack = RegistryUtils.setSpell(new ItemStack(EBItems.SPELL_BOOK.get()), Spells.MAGIC_MISSILE);
            spawn(level, player.blockPosition(), stack);
            EBLogger.error("Attempted to use a RandomSpellBookItem with an invalid LootTable NBT tag.");
            return;
        }

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, lootTableLocation));

        LootParams lootParams = new LootParams.Builder((ServerLevel) level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.GIFT);

        List<ItemStack> loot = lootTable.getRandomItems(lootParams);
        loot.forEach(i -> spawn(level, player.blockPosition(), i));
    }

    public static void onPickup(EBItemPickupEvent event) {
        ItemStack stack = event.getItemEntity().getItem();

        if (stack.getItem() instanceof RandomSpellBookItem) {
            RandomSpellBookItem.create(event.getEntity().level(), event.getEntity(), stack);
            event.getEntity().awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), stack.getCount());
            event.getItemEntity().discard();
            event.setCanceled(true);
        }
    }

    private static void spawn(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        level.addFreshEntity(itemEntity);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        create(level, player, stack);
        return InteractionResultHolder.pass(stack);
    }
}
