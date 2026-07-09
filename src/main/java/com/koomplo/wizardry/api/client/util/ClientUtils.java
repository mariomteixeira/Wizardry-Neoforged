package com.koomplo.wizardry.api.client.util;

import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.util.RegistryUtils;
import com.koomplo.wizardry.client.gui.screens.SpellBookScreen;
import com.koomplo.wizardry.content.data.SpellGlyphData;
import com.koomplo.wizardry.core.mixin.accessor.MerchantMenuAccessor;
import com.koomplo.wizardry.core.networking.s2c.SpellGlyphPacketS2C;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ClientUtils {

    public static boolean isFirstPerson(Entity entity) {
        return entity == Minecraft.getInstance().getCameraEntity() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    public static boolean shouldDisplayDiscovered(Spell spell, @Nullable ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        // Sem player = indexacao de tooltips (JEI na inicializacao): mostrar tudo para o #search funcionar
        if (player == null) return true;

        if (Minecraft.getInstance().screen instanceof MerchantScreen) {
            MerchantOffers recipes = ((MerchantScreen) Minecraft.getInstance().screen).getMenu().getOffers();
            if (recipes.stream().anyMatch(r -> r.getResult() == stack)) {
                return true;
            }
        }

        if (player.containerMenu instanceof MerchantMenu merchantMenu) {
            MerchantContainer tradeContainer = ((MerchantMenuAccessor) merchantMenu).getTradeContainer();
            if (tradeContainer.getItem(2) == stack) {
                return true;
            }
        }

        if (player.isCreative()) return true;
        return Services.OBJECT_DATA.getSpellManagerData(player).hasSpellBeenDiscovered(spell);
    }

    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void handleGlyphDataPacket(SpellGlyphPacketS2C message) {
        SpellGlyphData data = GlyphClientHandler.INSTANCE.getGlyphData();
        data.randomNames = new HashMap<>();
        data.randomDescriptions = new HashMap<>();

        for (Spell spell : Services.REGISTRY_UTIL.getSpells()) {
            ResourceLocation spellId = spell.getLocation();
            String name = message.getNames().get(spellId);
            String description = message.getDescriptions().get(spellId);

            if (name != null) data.randomNames.put(spell, name);
            if (description != null) data.randomDescriptions.put(spell, description);
        }
    }

    public static Component getScrollDisplayName(ItemStack scroll) {
        Spell spell = RegistryUtils.getSpell(scroll);
        return Component.translatable("item.ebwizardry.scroll", getSpellDisplayName(spell, scroll));
    }

    public static Component getBookDisplayName(ItemStack book) {
        Spell spell = RegistryUtils.getSpell(book);
        if (spell == Spells.NONE) return Component.translatable("item.ebwizardry.spell_book.empty");
        return Component.translatable("item.ebwizardry.spell_book", getSpellDisplayName(spell, book));
    }

    private static Component getSpellDisplayName(Spell spell, ItemStack stack) {
        if (shouldDisplayDiscovered(spell, stack)) return spell.getDescriptionFormatted();
        SpellGlyphData glyphData = GlyphClientHandler.INSTANCE.getGlyphData();
        // Antes do SpellGlyphPacketS2C chegar (JEI monta a lista no join, no ClientboundUpdateRecipesPacket)
        // não há glyph data - cair no nome real em vez de NPE/nome vazio
        if (glyphData == null || SpellGlyphData.getGlyphName(spell, glyphData).isEmpty())
            return spell.getDescriptionFormatted();
        return SpellGlyphData.getGlyphNameFormatted(spell, glyphData);
    }

    public static void openSpellBook(ItemStack stack) {
        Minecraft.getInstance().setScreen(new SpellBookScreen(stack));
    }
}
