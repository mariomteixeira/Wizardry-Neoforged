package com.binaris.wizardry.content.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * General-purpose {@link SimpleCriterionTrigger} for the mod; used to fire simple "advancement events" from mod code
 * with no extra condition data. The registry id is assigned by {@code CriteriaTriggers.register} (see
 * {@code EBAdvancementTriggers}); the constructor name is kept only for readability at the declaration site.
 */
public class WizardryAdvancementTrigger extends SimpleCriterionTrigger<WizardryAdvancementTrigger.TriggerInstance> {

    @SuppressWarnings("unused")
    public WizardryAdvancementTrigger(String name) {
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void triggerFor(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            this.trigger(serverPlayer, instance -> true);
        }
    }

    /** Builds a criterion for datagen. */
    public Criterion<TriggerInstance> instance() {
        return this.createCriterion(new TriggerInstance(Optional.empty()));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
        ).apply(inst, TriggerInstance::new));
    }
}
