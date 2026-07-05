package com.koomplo.wizardry.datagen.provider;

import com.koomplo.wizardry.datagen.provider.loot.EBBlockLootTables;
import com.koomplo.wizardry.datagen.provider.loot.EBChestLootTables;
import com.koomplo.wizardry.datagen.provider.loot.EBSubsetsLootTables;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class EBLootTableProvider {
    private EBLootTableProvider() {
    }

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(EBBlockLootTables::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(provider -> new EBChestLootTables(), LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(provider -> new EBSubsetsLootTables(), LootContextParamSets.EMPTY)

                ), registries);
    }
}
