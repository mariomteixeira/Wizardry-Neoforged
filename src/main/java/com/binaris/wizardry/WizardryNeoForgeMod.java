package com.binaris.wizardry;

import com.binaris.wizardry.client.NotImplementedItems;
import com.binaris.wizardry.client.WizardryForgeClient;
import com.binaris.wizardry.content.menu.BookshelfMenu;
import com.binaris.wizardry.integration.curios.CuriosIntegration;
import com.binaris.wizardry.network.EBForgeNetwork;
import com.binaris.wizardry.registry.EBArgumentTypesForge;
import com.binaris.wizardry.registry.EBRegistriesForge;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.EBDataComponents;
import com.binaris.wizardry.setup.registries.WandUpgrades;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(WizardryMainMod.MOD_ID)
public final class WizardryNeoForgeMod {
    public WizardryNeoForgeMod(IEventBus modBus, ModContainer container) {
        WizardryMainMod.init();

        EBRegistriesForge.tiers(modBus);
        EBRegistriesForge.elements(modBus);
        EBRegistriesForge.spells(modBus);
        EBArgumentTypesForge.register(modBus);
        EBDataComponents.COMPONENTS.register(modBus);
        EBAttachments.ATTACHMENTS.register(modBus);

        modBus.addListener(WizardryNeoForgeMod::commonSetup);
        modBus.addListener(EBForgeNetwork::registerPayloads);
        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(WizardryForgeClient::clientSetup);
        }
    }

    public static void commonSetup(final FMLCommonSetupEvent event) {
        BookshelfMenu.initBookItems();
        WandUpgrades.initUpgrades();
        NotImplementedItems.init();
        CuriosIntegration.load();
    }
}
