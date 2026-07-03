package com.binaris.wizardry.content.advancement;

import com.binaris.wizardry.api.content.spell.Spell;
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
 * Fired when a player casts a spell. Used by the "cast every spell" style advancements. The registry id is assigned by
 * {@code CriteriaTriggers.register} (see {@code EBAdvancementTriggers}).
 */
public class SpellCastTrigger extends SimpleCriterionTrigger<SpellCastTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Spell spell, ItemStack stack) {
        this.trigger(player, instance -> instance.matches(spell, stack));
    }

    /** Builds a criterion for datagen. */
    public Criterion<TriggerInstance> forSpell(SpellPredicate spell, Optional<ItemPredicate> item) {
        return this.createCriterion(new TriggerInstance(Optional.empty(), spell, item));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, SpellPredicate spell, Optional<ItemPredicate> item)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                SpellPredicate.CODEC.optionalFieldOf("spell", SpellPredicate.any()).forGetter(TriggerInstance::spell),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)
        ).apply(inst, TriggerInstance::new));

        public boolean matches(Spell spell, ItemStack stack) {
            return this.spell.test(spell) && (item.isEmpty() || item.get().test(stack));
        }
    }
}
