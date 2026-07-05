package com.koomplo.wizardry.content.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class MagicItemTier implements Tier {
    public static final Tier TIER = new MagicItemTier();

    @Override
    public int getUses() {
        return 100;
    }

    @Override
    public float getSpeed() {
        return 8.0F;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public float getAttackDamageBonus() {
        return 4.0F;
    }
}
