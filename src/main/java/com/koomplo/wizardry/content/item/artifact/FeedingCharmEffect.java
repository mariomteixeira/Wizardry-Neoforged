package com.koomplo.wizardry.content.item.artifact;

import com.koomplo.wizardry.content.spell.healing.ReplenishHunger;
import com.koomplo.wizardry.core.IArtifactEffect;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.koomplo.wizardry.core.ArtifactUtils.findMatchingWandAndCast;

public class FeedingCharmEffect implements IArtifactEffect {
    @Override
    public void onTick(Player player, Level level, ItemStack artifact) {
        if (player.tickCount % 100 != 0) return;

        if (player.getFoodData().getFoodLevel() < (20 - Spells.SATIETY.property(ReplenishHunger.HUNGER_POINTS)))
            if (findMatchingWandAndCast(player, Spells.SATIETY)) return;

        if (player.getFoodData().getFoodLevel() < (20 - Spells.REPLENISH_HUNGER.property(ReplenishHunger.HUNGER_POINTS)))
            findMatchingWandAndCast(player, Spells.REPLENISH_HUNGER);

    }
}
