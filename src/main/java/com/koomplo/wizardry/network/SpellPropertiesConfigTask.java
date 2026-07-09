package com.koomplo.wizardry.network;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.core.networking.s2c.SpellPropertiesSyncS2C;
import com.koomplo.wizardry.core.platform.Services;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Sincroniza as SpellProperties na fase de CONFIGURATION, antes do packet de recipes do vanilla.
 * O JEI monta a lista de ingredientes ao receber recipes; o sync antigo (só no join, fase PLAY)
 * chegava depois e o índice era construído com properties default (type vazio, tier/elemento errados
 * nos livros de addons — spells de addon têm properties via datapack, não via código).
 */
public record SpellPropertiesConfigTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final Type TYPE = new Type(WizardryMainMod.location("sync_spell_properties"));

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        Map<ResourceLocation, SpellProperties> map = Services.REGISTRY_UTIL.getSpells().stream()
                .collect(Collectors.toMap(Spell::getLocation, Spell::getProperties));
        sender.accept(new SpellPropertiesSyncS2C(map));
        listener.finishCurrentTask(TYPE);
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
