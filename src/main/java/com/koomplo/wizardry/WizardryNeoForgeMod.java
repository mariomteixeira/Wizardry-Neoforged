package com.koomplo.wizardry;

import com.koomplo.wizardry.client.NotImplementedItems;
import com.koomplo.wizardry.client.WizardryForgeClient;
import com.koomplo.wizardry.content.menu.BookshelfMenu;
import com.koomplo.wizardry.integration.curios.CuriosIntegration;
import com.koomplo.wizardry.network.EBForgeNetwork;
import com.koomplo.wizardry.registry.EBArgumentTypesForge;
import com.koomplo.wizardry.registry.EBRegistriesForge;
import com.koomplo.wizardry.setup.registries.EBAttachments;
import com.koomplo.wizardry.setup.registries.EBDataComponents;
import com.koomplo.wizardry.setup.registries.WandUpgrades;
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
