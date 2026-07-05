package com.koomplo.wizardry.registry;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.spell.Element;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.SpellTier;
import com.koomplo.wizardry.core.registry.EBRegistries;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class EBRegistriesForge {
    private static final DeferredRegister<Element> ELEMENTS = DeferredRegister.create(EBRegistries.ELEMENT, WizardryMainMod.MOD_ID);
    public static final Supplier<Registry<Element>> ELEMENT = ELEMENTS.getRegistry();

    private static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(EBRegistries.SPELL, WizardryMainMod.MOD_ID);
    public static final Supplier<Registry<Spell>> SPELL = SPELLS.getRegistry();

    private static final DeferredRegister<SpellTier> TIERS = DeferredRegister.create(EBRegistries.TIER, WizardryMainMod.MOD_ID);
    public static final Supplier<Registry<SpellTier>> TIER = TIERS.getRegistry();

    static {
        // Modded, code-defined registries (not saved to disk). Synced so the client can resolve them.
        ELEMENTS.makeRegistry(builder -> builder.sync(true));
        SPELLS.makeRegistry(builder -> builder.sync(true));
        TIERS.makeRegistry(builder -> builder.sync(true));
    }

    private EBRegistriesForge() {
    }

    public static void elements(IEventBus modBus) {
        ELEMENTS.register(modBus);
    }

    public static void spells(IEventBus modBus) {
        SPELLS.register(modBus);
    }

    public static void tiers(IEventBus modBus) {
        TIERS.register(modBus);
    }
}
