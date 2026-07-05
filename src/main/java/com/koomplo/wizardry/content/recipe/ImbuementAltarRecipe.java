package com.koomplo.wizardry.content.recipe;

import com.koomplo.wizardry.setup.registries.EBRecipeTypes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Recipe used by the Imbuement Altar. It is not a standard grid recipe: matching is done through the custom
 * {@link #matches(ItemStack, ItemStack[])} against the four receptacle stacks plus the central stack, so the
 * vanilla {@link Recipe#matches(RecipeInput, Level)} entry point is unused.
 */
public class ImbuementAltarRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> receptacleIngredients;
    private final Ingredient centerIngredient;
    private final ItemStack output;

    public ImbuementAltarRecipe(NonNullList<Ingredient> receptacleIngredients, Ingredient centerIngredient, ItemStack output) {
        this.receptacleIngredients = receptacleIngredients;
        this.centerIngredient = centerIngredient;
        this.output = output;
    }

    public boolean matches(ItemStack centerStack, ItemStack[] receptacleStacks) {
        if (receptacleStacks.length != 4) return false;
        if (!centerIngredient.test(centerStack)) return false;

        boolean[] matched = new boolean[4];
        for (int i = 0; i < 4; i++) {
            if (receptacleStacks[i].isEmpty()) return false;

            for (int j = 0; j < receptacleIngredients.size(); j++) {
                if (!matched[j] && receptacleIngredients.get(j).test(receptacleStacks[i])) {
                    matched[j] = true;
                    break;
                }
            }
        }

        for (boolean m : matched) {
            if (!m) return false;
        }

        return true;
    }

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput input, HolderLookup.@NotNull Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return output.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return EBRecipeTypes.IMBUEMENT_ALTAR_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return EBRecipeTypes.IMBUEMENT_ALTAR;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public NonNullList<Ingredient> getReceptacleIngredients() {
        return receptacleIngredients;
    }

    public Ingredient getCenterIngredient() {
        return centerIngredient;
    }

    public static class Serializer implements RecipeSerializer<ImbuementAltarRecipe> {
        private static final MapCodec<ImbuementAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("receptacles")
                        .flatXmap(Serializer::toNonNull, Serializer::fromNonNull)
                        .forGetter(recipe -> recipe.receptacleIngredients),
                Ingredient.CODEC.fieldOf("center").forGetter(recipe -> recipe.centerIngredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
        ).apply(instance, ImbuementAltarRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ImbuementAltarRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        private static DataResult<NonNullList<Ingredient>> toNonNull(List<Ingredient> list) {
            if (list.size() != 4) {
                return DataResult.error(() -> "Imbuement recipe must have exactly 4 receptacle ingredients");
            }
            NonNullList<Ingredient> receptacles = NonNullList.withSize(4, Ingredient.EMPTY);
            for (int i = 0; i < 4; i++) receptacles.set(i, list.get(i));
            return DataResult.success(receptacles);
        }

        private static DataResult<List<Ingredient>> fromNonNull(NonNullList<Ingredient> receptacles) {
            return DataResult.success(List.copyOf(receptacles));
        }

        private static ImbuementAltarRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            NonNullList<Ingredient> receptacleIngredients = NonNullList.withSize(4, Ingredient.EMPTY);
            for (int i = 0; i < 4; i++) {
                receptacleIngredients.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            }
            Ingredient centerIngredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            return new ImbuementAltarRecipe(receptacleIngredients, centerIngredient, output);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, ImbuementAltarRecipe recipe) {
            for (Ingredient ingredient : recipe.receptacleIngredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.centerIngredient);
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
        }

        @Override
        public @NotNull MapCodec<ImbuementAltarRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ImbuementAltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
