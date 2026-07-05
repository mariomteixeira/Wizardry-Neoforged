package com.koomplo.wizardry.client;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.item.ICastItem;
import com.koomplo.wizardry.client.model.armor.RobeArmorModel;
import com.koomplo.wizardry.client.model.armor.WizardArmorModel;
import com.koomplo.wizardry.content.item.armor.SpectralArmorItem;
import com.koomplo.wizardry.content.item.armor.WizardArmorItem;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.client.EBKeyBinding;
import com.koomplo.wizardry.setup.registries.client.EBMenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
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
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (((SpectralArmorItem) itemStack.getItem()).getEquipmentSlot() == EquipmentSlot.LEGS) {
                    return original;
                }

                ModelPart part = Minecraft.getInstance().getEntityModels().bakeLayer(
                        equipmentSlot == EquipmentSlot.LEGS ? ModelLayers.PLAYER_INNER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR);
                HumanoidModel<LivingEntity> chosen = new BlendingHumanoidModel(part);
                chosen.setAllVisible(false);
                switch (equipmentSlot) {
                    case HEAD -> {
                        chosen.head.visible = true;
                        chosen.hat.visible = true;
                    }
                    case CHEST -> {
                        chosen.body.visible = true;
                        chosen.rightArm.visible = true;
                        chosen.leftArm.visible = true;
                    }
                    case LEGS, FEET -> {
                        chosen.rightLeg.visible = true;
                        chosen.leftLeg.visible = true;
                    }
                }
                return chosen;
            }
        }, EBItems.SPECTRAL_HELMET.get(), EBItems.SPECTRAL_CHESTPLATE.get(), EBItems.SPECTRAL_LEGGINGS.get(), EBItems.SPECTRAL_BOOTS.get());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                WizardArmorItem item = (WizardArmorItem) itemStack.getItem();
                if (item.getEquipmentSlot() == EquipmentSlot.LEGS) {
                    return original;
                }

                switch (item.getWizardArmorType()) {
                    case BATTLEMAGE, WARLOCK -> {
                        RobeArmorModel<?> robeArmor = new RobeArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(RobeArmorModel.LAYER_LOCATION), true);
                        robeArmor.armorHead.visible = equipmentSlot == EquipmentSlot.HEAD;
                        robeArmor.armorBody.visible = equipmentSlot == EquipmentSlot.CHEST;
                        robeArmor.robe.visible = equipmentSlot == EquipmentSlot.CHEST;
                        robeArmor.left_arm.visible = equipmentSlot == EquipmentSlot.CHEST;
                        robeArmor.right_arm.visible = equipmentSlot == EquipmentSlot.CHEST;
                        robeArmor.rightLeg.visible = equipmentSlot == EquipmentSlot.LEGS;
                        robeArmor.leftLeg.visible = equipmentSlot == EquipmentSlot.LEGS;
                        robeArmor.right_shoe.visible = equipmentSlot == EquipmentSlot.FEET;
                        robeArmor.left_shoe.visible = equipmentSlot == EquipmentSlot.FEET;
                        return robeArmor;
                    }
                    default -> {
                        WizardArmorModel<?> wizardArmor = new WizardArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(WizardArmorModel.LAYER_LOCATION));
                        wizardArmor.hat.visible = equipmentSlot == EquipmentSlot.HEAD;
                        wizardArmor.armorBody.visible = equipmentSlot == EquipmentSlot.CHEST;
                        wizardArmor.robe.visible = equipmentSlot == EquipmentSlot.CHEST;
                        wizardArmor.left_arm.visible = equipmentSlot == EquipmentSlot.CHEST;
                        wizardArmor.right_arm.visible = equipmentSlot == EquipmentSlot.CHEST;
                        wizardArmor.rightLeg.visible = equipmentSlot == EquipmentSlot.LEGS;
                        wizardArmor.leftLeg.visible = equipmentSlot == EquipmentSlot.LEGS;
                        wizardArmor.right_shoe.visible = equipmentSlot == EquipmentSlot.FEET;
                        wizardArmor.left_shoe.visible = equipmentSlot == EquipmentSlot.FEET;
                        return wizardArmor;
                    }
                }
            }
        }, BuiltInRegistries.ITEM.stream().filter(item -> item instanceof WizardArmorItem).toArray(Item[]::new));
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
