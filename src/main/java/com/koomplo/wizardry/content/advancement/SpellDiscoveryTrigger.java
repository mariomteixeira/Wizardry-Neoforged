package com.koomplo.wizardry.content.advancement;

import com.koomplo.wizardry.api.content.event.EBDiscoverSpellEvent;
import com.koomplo.wizardry.api.content.spell.Spell;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Optional;

/**
 * Fired when a player discovers a spell (by casting, identification scroll, command, etc.). The registry id is assigned
 * by {@code CriteriaTriggers.register} (see {@code EBAdvancementTriggers}).
 */
public class SpellDiscoveryTrigger extends SimpleCriterionTrigger<SpellDiscoveryTrigger.TriggerInstance> {

    private static final Codec<EBDiscoverSpellEvent.Source> SOURCE_CODEC = Codec.STRING.xmap(
            s -> {
                EBDiscoverSpellEvent.Source src = EBDiscoverSpellEvent.Source.byName(s);
                return src == null ? EBDiscoverSpellEvent.Source.OTHER : src;
            },
            s -> s.name().toLowerCase(Locale.ROOT));

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Spell spell, EBDiscoverSpellEvent.Source source) {
        this.trigger(player, instance -> instance.matches(spell, source));
    }

    /** Builds a criterion for datagen matching the given spell predicate and discovery source. */
    public Criterion<TriggerInstance> forSpell(SpellPredicate spell, EBDiscoverSpellEvent.Source source) {
        return this.createCriterion(new TriggerInstance(Optional.empty(), spell, source));
    }

    /** Builds a criterion for datagen matching any spell discovered via the given source. */
    public Criterion<TriggerInstance> discoverSpell(EBDiscoverSpellEvent.Source source) {
        return forSpell(SpellPredicate.any(), source);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, SpellPredicate spell, EBDiscoverSpellEvent.Source source)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                SpellPredicate.CODEC.optionalFieldOf("spell", SpellPredicate.any()).forGetter(TriggerInstance::spell),
                SOURCE_CODEC.fieldOf("source").forGetter(TriggerInstance::source)
        ).apply(inst, TriggerInstance::new));

        public boolean matches(Spell spell, EBDiscoverSpellEvent.Source source) {
            return this.spell.test(spell) && source == this.source;
        }
    }
}
