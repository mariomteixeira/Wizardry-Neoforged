package com.binaris.wizardry.content.loot;

import com.binaris.wizardry.api.content.util.RegistryUtils;
import com.binaris.wizardry.core.EBLogger;
import com.binaris.wizardry.api.content.data.SpellManagerData;
import com.binaris.wizardry.api.content.spell.Element;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellContext;
import com.binaris.wizardry.api.content.spell.SpellTier;
import com.binaris.wizardry.content.item.ScrollItem;
import com.binaris.wizardry.content.item.SpellBookItem;
import com.binaris.wizardry.core.integrations.ArtifactChannel;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.EBItems;
import com.binaris.wizardry.setup.registries.EBLootFunctions;
import com.binaris.wizardry.setup.registries.Spells;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomSpellFunction extends LootItemConditionalFunction {

    private static final Codec<Spell> SPELL_CODEC = ResourceLocation.CODEC.xmap(Services.REGISTRY_UTIL::getSpell, Spell::getLocation);
    private static final Codec<SpellTier> TIER_CODEC = ResourceLocation.CODEC.xmap(Services.REGISTRY_UTIL::getTier, SpellTier::getOrCreateLocation);
    private static final Codec<Element> ELEMENT_CODEC = ResourceLocation.CODEC.xmap(Services.REGISTRY_UTIL::getElement, Element::getLocation);

    public static final MapCodec<RandomSpellFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(inst.group(
                    SPELL_CODEC.listOf().optionalFieldOf("spells").forGetter(f -> Optional.ofNullable(f.spells)),
                    Codec.BOOL.fieldOf("ignore_weighting").orElse(false).forGetter(f -> f.ignoreWeighting),
                    Codec.FLOAT.fieldOf("undiscovered_bias").orElse(0F).forGetter(f -> f.undiscoveredBias),
                    TIER_CODEC.listOf().optionalFieldOf("tiers").forGetter(f -> Optional.ofNullable(f.tiers)),
                    ELEMENT_CODEC.listOf().optionalFieldOf("elements").forGetter(f -> Optional.ofNullable(f.elements))
            )).apply(inst, (conditions, spells, ignoreWeighting, undiscoveredBias, tiers, elements) ->
                    new RandomSpellFunction(conditions, spells.orElse(null), ignoreWeighting, undiscoveredBias, tiers.orElse(null), elements.orElse(null))));

    private final @Nullable List<Spell> spells;
    private final @Nullable List<Element> elements;
    private final @Nullable List<SpellTier> tiers;
    private final boolean ignoreWeighting;
    private final float undiscoveredBias;

    protected RandomSpellFunction(List<LootItemCondition> conditions, @Nullable List<Spell> spells, boolean ignoreWeighting, float undiscoveredBias, @Nullable List<SpellTier> tiers, @Nullable List<Element> elements) {
        super(conditions);
        this.spells = spells;
        this.ignoreWeighting = ignoreWeighting;
        this.undiscoveredBias = undiscoveredBias;
        this.tiers = tiers;
        this.elements = elements;
    }

    public static LootItemConditionalFunction.Builder<?> setRandomSpell(List<Spell> spells, boolean ignoreWeighting, float undiscoveredBias, List<SpellTier> tiers, List<Element> elements) {
        return simpleBuilder((conditions) ->
                new RandomSpellFunction(conditions, spells, ignoreWeighting, undiscoveredBias, tiers, elements));
    }

    @Override
    public @NotNull LootItemFunctionType<RandomSpellFunction> getType() {
        return EBLootFunctions.RANDOM_SPELL;
    }

    @Override
    protected @NotNull ItemStack run(ItemStack stack, @NotNull LootContext lootContext) {
        if (!(stack.getItem() instanceof SpellBookItem) && !(stack.getItem() instanceof ScrollItem))
            EBLogger.warn("Applying the random_spell loot function to an item that isn't a spell book or scroll.");

        SpellContext context = !lootContext.hasParam(LootContextParams.THIS_ENTITY) ? SpellContext.TREASURE : SpellContext.LOOTING;
        Player player = null;
        if (lootContext.hasParam(LootContextParams.THIS_ENTITY) && lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player1) {
            player = player1;
        }

        Spell spell = pickRandomSpell(stack, lootContext.getRandom(), context, player);

        if (spell == Spells.NONE) return RegistryUtils.setSpell(stack, Spells.MAGIC_MISSILE);
        return RegistryUtils.setSpell(stack, spell);
    }

    private Spell pickRandomSpell(ItemStack stack, RandomSource random, SpellContext context, @Nullable Player player) {
        ArrayList<Spell> possibleSpells = new ArrayList<>(Services.REGISTRY_UTIL.getSpells());

        // Checking spells, if the spells list is specified
        if (spells != null && !spells.isEmpty()) possibleSpells.retainAll(spells);

        possibleSpells.removeIf(possibleSpell -> !possibleSpell.isEnabled(context));

        // Checking tiers, if the tiers list is specified
        if (tiers != null && !tiers.isEmpty()) {
            possibleSpells.removeIf(possibleSpell -> !tiers.contains(possibleSpell.getTier()));
        }

        // Checking elements, if the elements list is specified
        if (elements != null && !elements.isEmpty()) {
            possibleSpells.removeIf(possibleSpell -> !elements.contains(possibleSpell.getElement()));
        }

        if (stack.getItem() instanceof SpellBookItem)
            possibleSpells.removeIf(spell -> !spell.isEnabled(SpellContext.BOOK));
        if (stack.getItem() instanceof ScrollItem)
            possibleSpells.removeIf(spell -> !spell.isEnabled(SpellContext.SCROLL));

        if (player != null && undiscoveredBias > 0) {
            float bias = undiscoveredBias;
            if (ArtifactChannel.isEquipped(player, EBItems.CHARM_SPELL_DISCOVERY.get()))
                bias = Math.min(bias + 0.4f, 0.9f);
            if (bias > 0) {
                SpellManagerData data = Services.OBJECT_DATA.getSpellManagerData(player);
                int discoveredCount = (int) possibleSpells.stream().filter(data::hasSpellBeenDiscovered).count();
                if (discoveredCount > 0 && discoveredCount < possibleSpells.size()) {
                    boolean keepDiscovered = random.nextFloat() > 0.5f + 0.5f * undiscoveredBias;
                    possibleSpells.removeIf(s -> keepDiscovered != data.hasSpellBeenDiscovered(s));
                }
            }
        }

        if (possibleSpells.isEmpty()) return Spells.NONE; // don't worry, this is converted to Magic Missile in run();
        return possibleSpells.get(random.nextInt(possibleSpells.size()));
    }
}
