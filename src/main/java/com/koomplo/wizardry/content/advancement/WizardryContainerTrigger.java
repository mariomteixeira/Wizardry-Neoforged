package com.koomplo.wizardry.content.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Fired when the player crafts/produces an item in one of the mod's containers (arcane workbench, imbuement altar).
 * The registry id is assigned by {@code CriteriaTriggers.register} (see {@code EBAdvancementTriggers}).
 */
public class WizardryContainerTrigger extends SimpleCriterionTrigger<WizardryContainerTrigger.TriggerInstance> {

    @SuppressWarnings("unused")
    public WizardryContainerTrigger(String name) {
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack stack) {
        this.trigger(player, instance -> instance.matches(stack));
    }

    /** Builds a criterion for datagen matching the given item predicate. */
    public Criterion<TriggerInstance> forItem(ItemPredicate item) {
        return this.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(item)));
    }

    /** Builds a criterion for datagen matching any produced item. */
    public Criterion<TriggerInstance> instance() {
        return this.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)
        ).apply(inst, TriggerInstance::new));

        public boolean matches(ItemStack stack) {
            return item.isEmpty() || item.get().test(stack);
        }
    }
}
