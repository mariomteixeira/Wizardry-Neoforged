package com.koomplo.wizardry.datagen;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.datagen.provider.*;
import com.koomplo.wizardry.setup.datagen.EBDataGenProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * <b>Electroblob's Wizardry Internal Use Only</b>
 * <br><br>
 * <p>
 * This is the class that handles the datagen for the mod, we register all the basic features with the class
 * {@link EBDataGenProcessor EBDatagenProcessor} to avoid repetitive code,
 * also, <i>this is generated inside the common part of the mod.</i>
 */
@EventBusSubscriber(modid = WizardryMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class EBForgeDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();


        generator.addProvider(event.includeServer(), new AdvancementProvider(
                packOutput, lookupProvider, existingFileHelper,
                Collections.singletonList(new EBAdvancementsProvider())
        ));

        generator.addProvider(event.includeServer(), EBLootTableProvider.create(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new EBBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new EBItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new EBSpellsProvider(packOutput));
        generator.addProvider(event.includeServer(), new EBSpellDocsProvider(packOutput));
        generator.addProvider(event.includeServer(), new EBArtifactDocsProvider(packOutput));

        EBBlockTagProvider blockTagProvider = generator.addProvider(event.includeServer(), new EBBlockTagProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new EBItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper));

        event.getGenerator().addProvider(event.includeServer(), new EBRecipeProvider(packOutput, lookupProvider));
    }

}
