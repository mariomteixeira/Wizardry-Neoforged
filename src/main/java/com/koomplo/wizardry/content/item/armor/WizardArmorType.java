package com.koomplo.wizardry.content.item.armor;


import com.koomplo.wizardry.api.content.spell.Element;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.EBSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Enum defining the different classes of wizard armor. Each class has its own armor material metadata, upgrade item,
 * and armor piece names. The armor piece names are used to construct the registry names for the armor items.
 * <p>
 * In 1.21 {@code ArmorMaterial} is a data record rather than an interface, so this enum no longer implements it;
 * the actual {@link net.minecraft.world.item.ArmorMaterial} instances are built from this metadata and registered in
 * {@link com.koomplo.wizardry.setup.registries.EBArmorMaterials}.
 */
public enum WizardArmorType {
    // upgradeItem is deliberately a lazy Supplier (not a direct EBItems field reference): touching an EBItems
    // static field here would force EBItems to class-init mid-construction of this enum, and EBItems in turn
    // references WizardArmorType constants (e.g. WizardArmorType.SAGE) to build armor items - a circular
    // <clinit> dependency that NPEs because the enum constant field isn't assigned until its constructor returns.
    WIZARD("wizard", () -> null, 15, 0.1F, 0, SoundEvents.ARMOR_EQUIP_DIAMOND, new int[]{2, 4, 5, 2}, 15, "hat", "robe", "leggings", "boots"),
    SAGE("sage", () -> EBItems.RESPLENDENT_THREAD.get(), 15, 0.2f, 0, wrap(EBSounds.ITEM_ARMOUR_EQUIP_SAGE.get()), new int[]{2, 5, 6, 3}, 15, "hat", "robe", "leggings", "boots"),
    BATTLEMAGE("battlemage", () -> EBItems.CRYSTAL_SILVER_PLATING.get(), 15, 0.05f, 0.05f, wrap(EBSounds.ITEM_ARMOUR_EQUIP_BATTLEMAGE.get()), new int[]{3, 6, 8, 3}, 15, "helmet", "chestplate", "leggings", "boots"),
    WARLOCK("warlock", () -> EBItems.ETHEREAL_CRYSTAL_WEAVE.get(), 20, 0.1f, 0.1f, wrap(EBSounds.ITEM_ARMOUR_EQUIP_WARLOCK.get()), new int[]{2, 4, 5, 2}, 15, "hood", "robe", "leggings", "boots");

    final int[] protectionValues;
    final int durabilityMultiplier;
    final String name;
    final Supplier<Item> upgradeItem;
    final float elementalCostReduction;
    final float cooldownReduction;
    @Deprecated
    final Map<EquipmentSlot, String> armourPieceNames;
    final Holder<SoundEvent> equipSound;
    final int enchantability;
    Element element = null;

    WizardArmorType(String name, Supplier<Item> upgradeItem, int durabilityMultiplier, float elementalCostReduction, float cooldownReduction, Holder<SoundEvent> equipSound, int[] protectionValues, int enchantability, String... armourPieceNames) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.upgradeItem = upgradeItem;
        this.elementalCostReduction = elementalCostReduction;
        this.cooldownReduction = cooldownReduction;
        this.equipSound = equipSound;
        this.protectionValues = protectionValues;
        this.enchantability = enchantability;


        if (armourPieceNames.length != 4) {
            throw new IllegalArgumentException("Armour class " + name + " must have exactly 4 armour piece names. Try again!!!!!");
        }
        this.armourPieceNames = new EnumMap<>(EquipmentSlot.class);
        this.armourPieceNames.put(EquipmentSlot.HEAD, armourPieceNames[0]);
        this.armourPieceNames.put(EquipmentSlot.CHEST, armourPieceNames[1]);
        this.armourPieceNames.put(EquipmentSlot.LEGS, armourPieceNames[2]);
        this.armourPieceNames.put(EquipmentSlot.FEET, armourPieceNames[3]);

    }

    private static Holder<SoundEvent> wrap(SoundEvent soundEvent) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent);
    }

    // --------------------------------- Armor material metadata accessors --------------------------------- //

    public int getDefenseForType(ArmorItem.Type type) {
        // protectionValues only covers the 4 wizard-armor slots (helmet/chestplate/leggings/boots); 1.21 added
        // ArmorItem.Type.BODY (wolf/horse armor), which wizard armor sets don't define a piece for.
        int ordinal = type.ordinal();
        return ordinal < protectionValues.length ? protectionValues[ordinal] : 0;
    }

    public int[] getProtectionValues() {
        return protectionValues;
    }

    public int getDurabilityMultiplier() {
        return durabilityMultiplier;
    }

    public int getEnchantmentValue() {
        return enchantability;
    }

    public Holder<SoundEvent> getEquipSound() {
        return equipSound;
    }

    public String getName() {
        if (element != null) {
            return name + "_" + element.getDescriptionFormatted();
        } else {
            return name;
        }
    }

    public Map<EquipmentSlot, String> getArmourPieceNames() {
        return armourPieceNames;
    }
}
