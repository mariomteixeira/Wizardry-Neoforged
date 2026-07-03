package com.binaris.wizardry.api.content.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Interface to allow items to add/remove entity attributes dynamically based on the item stack.
 * <p>
 * In 1.21 attribute modifiers are supplied via the {@link ItemAttributeModifiers} component
 * ({@code IItemExtension.getDefaultAttributeModifiers(ItemStack)}), so the slot is encoded in the modifiers
 * themselves rather than passed separately.
 * <p>
 * This is only implemented for Wizard armor items.
 */
public interface ICustomAttributesItem {
    ItemAttributeModifiers getCustomAttributes(ItemStack stack);
}
