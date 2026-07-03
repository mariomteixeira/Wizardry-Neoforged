package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.DeferredObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class EBEnchantments {
    static Map<String, DeferredObject<Enchantment>> ENCHANTMENTS = new HashMap<>();

    private EBEnchantments() {
    }

    // ======= Registry =======
    public static void register(RegisterFunction<Enchantment> function) {
        // 1.21: enchantments are datapack-driven (Registries.ENCHANTMENT has no writable code registry).
        // The mod registers no custom enchantments in code, so this is intentionally a no-op.
    }

    // ======= Helpers =======
    static DeferredObject<Enchantment> enchantment(String name, Enchantment enchantment) {
        DeferredObject<Enchantment> deferredEnchant = new DeferredObject<>(() -> enchantment);
        ENCHANTMENTS.put(name, deferredEnchant);
        return deferredEnchant;
    }
}
