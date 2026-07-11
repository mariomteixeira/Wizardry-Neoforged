package com.koomplo.wizardry.core.integrations.jei;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.setup.registries.EBDataComponents;
import com.koomplo.wizardry.setup.registries.EBItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Spell books, scrolls and arcane tomes are single items whose variant lives in a data component;
 * without these interpreters JEI collapses every variant into one entry.
 * Only classloaded by JEI itself, so the mod keeps working without JEI installed.
 */
@JeiPlugin
public class EBJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = WizardryMainMod.location("jei");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime jeiRuntime) {
        // Itens sem efeito no port não aparecem no JEI (seguem registrados por compat de mundo)
        var hidden = com.koomplo.wizardry.client.NotImplementedItems.all().stream().map(ItemStack::new).toList();
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK, hidden);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        ISubtypeInterpreter<ItemStack> bySpell = byComponent(EBDataComponents.SPELL.get());
        // Todos os livros/scrolls do ecossistema (inclui os dos addons que estendem as classes base)
        for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            if (item instanceof com.koomplo.wizardry.content.item.SpellBookItem
                    || item instanceof com.koomplo.wizardry.content.item.ScrollItem) {
                registration.registerSubtypeInterpreter(item, bySpell);
            }
        }
        registration.registerSubtypeInterpreter(EBItems.ARCANE_TOME.get(), byComponent(EBDataComponents.TIER.get()));
    }

    private static ISubtypeInterpreter<ItemStack> byComponent(DataComponentType<ResourceLocation> component) {
        return new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(@NotNull ItemStack stack, @NotNull UidContext context) {
                return stack.get(component);
            }

            @SuppressWarnings("removal")
            @Override
            public @NotNull String getLegacyStringSubtypeInfo(@NotNull ItemStack stack, @NotNull UidContext context) {
                ResourceLocation location = stack.get(component);
                return location == null ? "" : location.toString();
            }
        };
    }
}
