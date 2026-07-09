package com.koomplo.wizardry.content.item;

import com.koomplo.wizardry.api.content.util.RegistryUtils;
import com.koomplo.wizardry.core.EBLogger;
import com.koomplo.wizardry.api.client.util.ClientUtils;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.core.platform.Services;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellBookItem extends Item {
    public SpellBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);
        if (RegistryUtils.getSpell(stack) == Spells.NONE) {
            RegistryUtils.setSpell(stack, Spells.MAGIC_MISSILE);
            EBLogger.warn("SpellBookItem has no spell assigned to it! Defaulting to Magic Missile, where did this item come from?");
        }
        if (level.isClientSide) ClientUtils.openSpellBook(stack);
        return super.use(level, player, interactionHand);
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        super.onCraftedBy(stack, level, player);
        Spell spell = RegistryUtils.getSpell(stack);
        if (spell == Spells.NONE) RegistryUtils.setSpell(stack, Spells.MAGIC_MISSILE);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (Services.PLATFORM.isDedicatedServer()) {
            Spell spell = RegistryUtils.getSpell(stack);
            if (spell == Spells.NONE) return Component.translatable("item.ebwizardry.spell_book.empty");
            return Component.translatable("item.ebwizardry.spell_book", spell.getDescriptionFormatted());
        }
        return ClientUtils.getBookDisplayName(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        // ClientUtils touches Minecraft.getInstance(); never build this tooltip on a dedicated server
        if (Services.PLATFORM.isDedicatedServer()) return;
        Spell spell = RegistryUtils.getSpell(stack);
        if (spell == Spells.NONE) return;
        // Elemento/tipo sem gate de descoberta: classificação não revela o que a spell faz e o JEI
        // cacheia tooltips no join (antes do sync de descobertas) - gate mataria a busca $elemento
        list.add(spell.getTier().getDescriptionFormatted());
        list.add(Component.translatable(spell.getElement().getDescriptionId()).withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable(spell.getType().getDisplayName()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    public ResourceLocation getGuiTexture(Spell spell) {
        ResourceLocation l = spell.getTier().getOrCreateLocation();
        return ResourceLocation.fromNamespaceAndPath(l.getNamespace(), "textures/gui/spell_book_" + l.getPath() + ".png");
    }
}
