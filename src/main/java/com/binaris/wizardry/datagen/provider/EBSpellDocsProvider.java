package com.binaris.wizardry.datagen.provider;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Implementation of the spell documentation provider for Electroblob's Wizardry Redux.
 * This generates Markdown documentation for all registered spells in the mod.
 */
public class EBSpellDocsProvider extends SpellDocsProvider {
    public EBSpellDocsProvider(PackOutput output) {
        super(output, WizardryMainMod.MOD_ID, "/resources/img/spell/");
    }

    public EBSpellDocsProvider(PackOutput output, String iconBasePath) {
        super(output, WizardryMainMod.MOD_ID, iconBasePath);
    }

    @Override
    protected void buildSpells(@NotNull Consumer<Spell> consumer) {
        Spells.SPELLS.values().forEach(consumer);
    }
}

