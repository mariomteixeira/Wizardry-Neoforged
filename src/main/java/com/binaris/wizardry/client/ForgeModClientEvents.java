package com.binaris.wizardry.client;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.item.ICastItem;
import com.binaris.wizardry.setup.registries.client.EBKeyBinding;
import com.binaris.wizardry.setup.registries.client.EBMenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = WizardryMainMod.MOD_ID)
public class ForgeModClientEvents {
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CROSSHAIR, WizardryMainMod.location("charge_meter"), (guiGraphics, deltaTracker) -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack wand = player.getMainHandItem();

            if (!(wand.getItem() instanceof ICastItem castingItem && castingItem.showSpellHUD(player, wand))) {
                wand = player.getOffhandItem();
                if (!(wand.getItem() instanceof ICastItem castingItem && castingItem.showSpellHUD(player, wand)))
                    return;
            }

            ItemStack finalWand = wand;
            SpellGUIDisplay.renderChargeMeter(guiGraphics.pose(), player, finalWand,
                    guiGraphics.guiWidth(), guiGraphics.guiHeight(), deltaTracker.getGameTimeDeltaPartialTick(false));
        });
        event.registerBelow(VanillaGuiLayers.HOTBAR, WizardryMainMod.location("spell_hud"), (guiGraphics, deltaTracker) -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack wand = player.getMainHandItem();
            boolean mainHand;

            if (!(wand.getItem() instanceof ICastItem castingItem && castingItem.showSpellHUD(player, wand))) {
                wand = player.getOffhandItem();
                mainHand = false;
                if (!(wand.getItem() instanceof ICastItem castingItem && castingItem.showSpellHUD(player, wand)))
                    return;
            } else {
                mainHand = true;
            }

            ItemStack finalWand = wand;
            SpellGUIDisplay.renderSpellHUD(guiGraphics, guiGraphics.pose(), player, finalWand, mainHand,
                    guiGraphics.guiWidth(), guiGraphics.guiHeight(), deltaTracker.getGameTimeDeltaPartialTick(false), true);
        });
        event.registerBelow(VanillaGuiLayers.HOTBAR, WizardryMainMod.location("spell_hud_text"), (guiGraphics, deltaTracker) -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack wand = player.getMainHandItem();
            boolean mainHand;

            if (!(wand.getItem() instanceof ICastItem castingItem && castingItem.showSpellHUD(player, wand))) {
                wand = player.getOffhandItem();
                mainHand = false;
                if (!(wand.getItem() instanceof ICastItem castingItem && castingItem.showSpellHUD(player, wand)))
                    return;
            } else {
                mainHand = true;
            }

            ItemStack finalWand = wand;
            SpellGUIDisplay.renderSpellHUD(guiGraphics, guiGraphics.pose(), player, finalWand, mainHand,
                    guiGraphics.guiWidth(), guiGraphics.guiHeight(), deltaTracker.getGameTimeDeltaPartialTick(false), false);
        });
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        EBMenuScreens.init();
        EBMenuScreens.register((menuType, screenFactory) -> event.register(menuType, screenFactory::create));
    }

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(EBKeyBinding.NEXT_SPELL);
        event.register(EBKeyBinding.PREVIOUS_SPELL);
        for (int i = 0; i < EBKeyBinding.SPELL_QUICK_ACCESS.length; i++) {
            event.register(EBKeyBinding.SPELL_QUICK_ACCESS[i]);
        }
    }
}
