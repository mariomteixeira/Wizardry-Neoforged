package com.koomplo.wizardry.client;

import com.koomplo.wizardry.setup.registries.EBItems;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Itens sem NENHUM efeito no port (artefatos de effect null e sem gancho via ArtifactChannel,
 * mais wizard_handbook e lightning_hammer). Ficam fora do creative/JEI e ganham tooltip de aviso;
 * seguem registrados para não quebrar mundos existentes.
 */
public final class NotImplementedItems {
    static List<Item> ITEMS = List.of(
            EBItems.CHARM_BLACK_HOLE.get(),
            EBItems.CHARM_SIXTH_SENSE.get(),
            EBItems.CHARM_TRANSPORTATION.get(),
            EBItems.AMULET_RESURRECTION.get(),
            EBItems.RING_DEFENDER.get(),
            EBItems.RING_DISINTEGRATION.get(),
            EBItems.RING_HAMMER.get(),
            EBItems.RING_INTERDICTION.get(),
            EBItems.RING_MIND_CONTROL.get(),
            EBItems.RING_STORMCLOUD.get(),
            EBItems.WIZARD_HANDBOOK.get(),
            EBItems.LIGHTNING_HAMMER.get()
    );

    public static void init() {
    }

    public static boolean notImplemented(Item item) {
        return ITEMS.contains(item);
    }

    public static List<Item> all() {
        return ITEMS;
    }

    private NotImplementedItems() {
    }
}
