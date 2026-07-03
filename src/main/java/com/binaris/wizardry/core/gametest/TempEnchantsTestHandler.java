package com.binaris.wizardry.core.gametest;

import com.binaris.wizardry.api.content.data.ImbuementEnchantData;
import com.binaris.wizardry.api.content.event.EBLivingTick;
import com.binaris.wizardry.core.DataEvents;
import com.binaris.wizardry.core.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("all")
public final class TempEnchantsTestHandler {
    private static final Vec3 PLAYER_POS = new Vec3(1.5, 2.0, 1.5);

    // 1.21: enchantments are datapack-driven; resolve a ResourceKey to its registry Holder for the 1.21 enchant APIs.
    private static Holder<Enchantment> resolve(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    static void simpleEnchantTest(GameTestHelper helper, Item itemToEnchant, ResourceKey<Enchantment> tempEnchant) {
        // The check is done in setupTest
        TestContext ctx = setupTest(helper, itemToEnchant.getDefaultInstance(), tempEnchant);
        helper.succeed();
    }


    static void simpleEnchantCompanyTest(GameTestHelper helper, Item itemToEnchant, ResourceKey<Enchantment> tempEnchant, ResourceKey<Enchantment> normalEnchant) {
        TestContext ctx = setupTest(helper, itemToEnchant.getDefaultInstance(), tempEnchant);
        Holder<Enchantment> normal = resolve(helper, normalEnchant);
        ctx.stack.enchant(normal, 1);
        GST.assertTrue(helper, "Imbuement enchant not present after applying", EnchantmentHelper.getEnchantmentLevel(ctx.enchantment, ctx.player) > 0);
        GST.assertTrue(helper, "Normal enchant not present after applying with imbuement enchant", EnchantmentHelper.getEnchantmentLevel(normal, ctx.player) > 0);
        helper.succeed();
    }


    static void imbuementTickTest(GameTestHelper helper, Item itemToEnchant, ResourceKey<Enchantment> tempEnchant) {
        TestContext ctx = setupTest(helper, itemToEnchant.getDefaultInstance(), tempEnchant);

        helper.onEachTick(() -> DataEvents.onPlayerTick(new EBLivingTick(ctx.player, ctx.player.level())));

        helper.runAtTickTime(60, () -> {
            DataEvents.onPlayerTick(new EBLivingTick(ctx.player, ctx.player.level()));
            GST.assertFalse(helper, "Imbuement enchant should be removed after 40 ticks (end time)", EnchantmentHelper.getEnchantmentLevel(ctx.enchantment, ctx.player) > 0);
            helper.succeed();
        });
    }


    static void imbuementTickCompanyTest(GameTestHelper helper, Item itemToEnchant, ResourceKey<Enchantment> tempEnchantment, ResourceKey<Enchantment> enchantment) {
        TestContext ctx = setupTest(helper, itemToEnchant.getDefaultInstance(), tempEnchantment);
        Holder<Enchantment> normal = resolve(helper, enchantment);
        ctx.stack.enchant(normal, 1);

        helper.onEachTick(() -> DataEvents.onPlayerTick(new EBLivingTick(ctx.player, ctx.player.level())));

        helper.runAtTickTime(60, () -> {
            DataEvents.onPlayerTick(new EBLivingTick(ctx.player, ctx.player.level()));
            GST.assertFalse(helper, "Imbuement enchant should be removed after 40 ticks (end time)", EnchantmentHelper.getEnchantmentLevel(ctx.enchantment, ctx.player) > 0);
            GST.assertTrue(helper, "Normal enchant not present after removing imbuement", EnchantmentHelper.getEnchantmentLevel(normal, ctx.player) > 0);
            helper.succeed();
        });
    }

    static TestContext setupTest(GameTestHelper helper, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        Player player = GST.mockPlayer(helper, PLAYER_POS);
        player.setItemSlot(EquipmentSlot.MAINHAND, stack);
        ImbuementEnchantData data = Services.OBJECT_DATA.getImbuementData(stack);
        Holder<Enchantment> holder = resolve(helper, enchantment);
        stack.enchant(holder, 1);
        data.addImbuement(holder, player.level().getGameTime() + 40);

        GST.assertTrue(helper, "Imbuement enchant not present after applying", EnchantmentHelper.getEnchantmentLevel(holder, player) > 0);
        return new TestContext(player, stack, holder, data);
    }

    record TestContext(Player player, ItemStack stack, Holder<Enchantment> enchantment, ImbuementEnchantData data) {
    }

    private TempEnchantsTestHandler() {
    }
}
