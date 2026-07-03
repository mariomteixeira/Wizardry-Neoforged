package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.content.loot.RandomSpellFunction;
import com.binaris.wizardry.content.loot.WizardSpellFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EBLootFunctions {

    private static final Map<ResourceLocation, LootItemFunctionType<?>> FUNCTIONS_TO_REGISTER = new LinkedHashMap<>();

    public static final LootItemFunctionType<RandomSpellFunction> RANDOM_SPELL = register("random_spell", new LootItemFunctionType<>(RandomSpellFunction.CODEC));
    public static final LootItemFunctionType<WizardSpellFunction> WIZARD_SPELL = register("wizard_spell", new LootItemFunctionType<>(WizardSpellFunction.CODEC));

    private EBLootFunctions() {
    }

    private static <T extends LootItemFunctionType<?>> T register(String name, T type) {
        FUNCTIONS_TO_REGISTER.put(WizardryMainMod.location(name), type);
        return type;
    }

    public static void register(RegisterFunction<LootItemFunctionType<?>> function) {
        FUNCTIONS_TO_REGISTER.forEach(((id, loot_function) ->
                function.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id, loot_function)));
    }
}
