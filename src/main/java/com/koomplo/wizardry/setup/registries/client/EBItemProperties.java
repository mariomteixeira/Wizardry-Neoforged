package com.koomplo.wizardry.setup.registries.client;

import com.koomplo.wizardry.api.content.data.ConjureData;
import com.koomplo.wizardry.core.mixin.accessor.ItemPropertiesAccessor;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.datagen.EBDataGenProcessor;
import com.koomplo.wizardry.setup.registries.EBItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class EBItemProperties {
    private EBItemProperties() {
    }

    public static void register() {
        EBDataGenProcessor.wandItems().forEach((s, i) ->
                registerWandProperties(i.get()));

        // Other items with pulling/conjure properties
        pullingItem(EBItems.FLAMECATCHER.get());
        conjureItem(EBItems.FLAMECATCHER.get());

        conjureItem(EBItems.FROST_AXE.get());
        conjureItem(EBItems.FLAMING_AXE.get());

        conjureItem(EBItems.SPECTRAL_PICKAXE.get());

        pullingItem(EBItems.SPECTRAL_BOW.get());
        conjureItem(EBItems.SPECTRAL_BOW.get());
    }

    private static void registerWandProperties(Item wand) {
        ItemPropertiesAccessor.callRegister(wand, ResourceLocation.parse("casting"), (stack, clientLevel, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    private static void pullingItem(Item item) {
        ItemPropertiesAccessor.callRegister(item, ResourceLocation.parse("pull"), (stack, clientLevel, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getUseItem() != stack ? 0.0F : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
            }
        });

        ItemPropertiesAccessor.callRegister(item, ResourceLocation.parse("pulling"), (stack, clientLevel, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

    }

    private static void conjureItem(Item item) {
        ItemPropertiesAccessor.callRegister(item, ResourceLocation.parse("conjure"), (stack, clientLevel, entity, seed) -> {
            ConjureData data = Services.OBJECT_DATA.getConjureData(stack);
            if (data != null && data.isSummoned()) {
                int frames = 8;
                int remaining = data.getRemainingLifetime(clientLevel.getGameTime());
                int duration = data.getDuration();
                int elapsed = duration - remaining;
                return elapsed < frames ? (float) elapsed / frames : (float) remaining / frames;
            }
            return 0.0F;
        });

        ItemPropertiesAccessor.callRegister(item, ResourceLocation.parse("conjuring"), (stack, clientLevel, entity, seed) -> {
            ConjureData data = Services.OBJECT_DATA.getConjureData(stack);
            if (data != null && data.isSummoned()) {
                int frames = 8;
                int remaining = data.getRemainingLifetime(clientLevel.getGameTime());
                int duration = data.getDuration();
                int elapsed = duration - remaining;
                return elapsed < frames || remaining < frames ? 1.0F : 0.0F;
            }
            return 0.0F;
        });
    }


}
