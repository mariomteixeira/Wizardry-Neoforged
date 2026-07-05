package com.koomplo.wizardry.setup.registries;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.DeferredObject;
import com.koomplo.wizardry.content.item.armor.WizardArmorType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registers a {@link ArmorMaterial} for each {@link WizardArmorType}. In 1.21 {@code ArmorMaterial} is a data record
 * registered in {@link net.minecraft.core.registries.Registries#ARMOR_MATERIAL} and {@code ArmorItem} takes a
 * {@code Holder<ArmorMaterial>} rather than the material directly. Mirrors {@link EBMobEffects} for the
 * DeferredObject/{@link #holder} pattern.
 */
public final class EBArmorMaterials {
    private static final Map<WizardArmorType, DeferredObject<ArmorMaterial>> MATERIALS = new EnumMap<>(WizardArmorType.class);

    static {
        for (WizardArmorType type : WizardArmorType.values()) {
            MATERIALS.put(type, new DeferredObject<>(() -> build(type)));
        }
    }

    private EBArmorMaterials() {
    }

    private static ArmorMaterial build(WizardArmorType type) {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type armorPiece : ArmorItem.Type.values()) {
            defense.put(armorPiece, type.getDefenseForType(armorPiece));
        }
        return new ArmorMaterial(
                defense,
                type.getEnchantmentValue(),
                type.getEquipSound(),
                () -> Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(WizardryMainMod.location(type.name().toLowerCase(java.util.Locale.ROOT)))),
                0,
                0
        );
    }

    // ======= Registry =======
    public static void register(RegisterFunction<ArmorMaterial> function) {
        MATERIALS.forEach((type, material) ->
                function.register(BuiltInRegistries.ARMOR_MATERIAL, WizardryMainMod.location(type.name().toLowerCase(java.util.Locale.ROOT)), material.get()));
    }

    /**
     * Wraps the registered {@link ArmorMaterial} for the given type as a {@link Holder}, as required by the 1.21
     * {@code ArmorItem} constructor.
     */
    public static Holder<ArmorMaterial> holder(WizardArmorType type) {
        return BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(MATERIALS.get(type).get());
    }
}
