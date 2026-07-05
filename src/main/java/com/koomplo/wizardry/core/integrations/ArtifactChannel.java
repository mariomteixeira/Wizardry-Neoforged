package com.koomplo.wizardry.core.integrations;

import com.koomplo.wizardry.core.IArtifactEffect;
import com.koomplo.wizardry.core.platform.Services;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.List;

public final class ArtifactChannel {

    public static Item createArtifact(Rarity rarity, IArtifactEffect effect) {
        ArtifactIntegration integration = getIntegration();
        return integration.createArtifact(rarity, effect);
    }

    public static List<ItemStack> getEquippedArtifacts(Player player) {
        ArtifactIntegration integration = getIntegration();
        return integration.getEquippedArtifacts(player);

    }

    public static boolean isEquipped(Player player, Item item){
        ArtifactIntegration integration = getIntegration();
        return integration.isEquipped(player, item);
    }

    public static ArtifactIntegration getIntegration(){
        if (Services.PLATFORM.isModLoaded("curios")) {
            return Services.PLATFORM.getArtifactIntegration();
        }

        return VanillaArtifactIntegration.INSTANCE;
    }

    private ArtifactChannel(){
    }
}
