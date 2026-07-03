package com.binaris.wizardry.core;

import com.binaris.wizardry.content.recipe.ImbuementAltarRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImbuementAltarRecipeBuilder implements RecipeBuilder {
    private final Ingredient centerIngredient;
    private final Ingredient[] receptacleIngredients;
    private final Item result;
    private final int count;
    @Nullable
    private CompoundTag nbt;
    @Nullable
    private String group;

    private ImbuementAltarRecipeBuilder(Ingredient centerIngredient, Ingredient[] receptacleIngredients, ItemLike result, int count) {
        if (receptacleIngredients.length != 4) {
            throw new IllegalArgumentException("ImbuementAltarRecipe must have exactly 4 receptacle ingredients");
        }
        this.centerIngredient = centerIngredient;
        this.receptacleIngredients = receptacleIngredients;
        this.result = result.asItem();
        this.count = count;
    }

    public static ImbuementAltarRecipeBuilder imbuement(Ingredient centerIngredient, Ingredient receptacle, ItemLike result) {
        return new ImbuementAltarRecipeBuilder(centerIngredient, new Ingredient[]{receptacle, receptacle, receptacle, receptacle}, result, 1);
    }

    public static ImbuementAltarRecipeBuilder imbuement(Ingredient centerIngredient, Ingredient receptacle1, Ingredient receptacle2, Ingredient receptacle3, Ingredient receptacle4, ItemLike result) {
        return new ImbuementAltarRecipeBuilder(centerIngredient, new Ingredient[]{receptacle1, receptacle2, receptacle3, receptacle4}, result, 1);
    }

    public static ImbuementAltarRecipeBuilder imbuement(Ingredient centerIngredient, Ingredient receptacle1, Ingredient receptacle2, Ingredient receptacle3, Ingredient receptacle4, ItemLike result, int count) {
        return new ImbuementAltarRecipeBuilder(centerIngredient, new Ingredient[]{receptacle1, receptacle2, receptacle3, receptacle4}, result, count);
    }

    public ImbuementAltarRecipeBuilder withNbt(CompoundTag nbt) {
        this.nbt = nbt;
        return this;
    }

    @Override
    public @NotNull ImbuementAltarRecipeBuilder unlockedBy(@NotNull String criterionName, @NotNull Criterion<?> criterionTrigger) {
        return this;
    }

    @Override
    public @NotNull ImbuementAltarRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result;
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull ResourceLocation recipeId) {
        NonNullList<Ingredient> receptacles = NonNullList.withSize(4, Ingredient.EMPTY);
        for (int i = 0; i < 4; i++) {
            receptacles.set(i, this.receptacleIngredients[i]);
        }

        ItemStack output = new ItemStack(this.result, this.count);
        if (this.nbt != null) {
            output.set(DataComponents.CUSTOM_DATA, CustomData.of(this.nbt));
        }

        ImbuementAltarRecipe recipe = new ImbuementAltarRecipe(receptacles, this.centerIngredient, output);
        recipeOutput.accept(recipeId, recipe, null);
    }
}
