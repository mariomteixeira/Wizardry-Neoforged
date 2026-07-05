package com.koomplo.wizardry.content;

import com.koomplo.wizardry.api.content.event.SpellCastEvent;
import com.koomplo.wizardry.api.content.spell.*;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.setup.registries.EBAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conditional-attribute-modifier support for the {@code /magic_attribute} command.
 * <p>
 * In 1.21 {@link AttributeModifier} became a {@code final record}, so it can no longer be subclassed (the old
 * {@code WizardryAttributeModifier extends AttributeModifier} pattern is impossible). Instead, a modifier's optional
 * {@link SpellCondition} is stored side-band in {@link #CONDITIONS}, keyed by the modifier's stable
 * {@link ResourceLocation} id. The condition is persisted through the vanilla attribute NBT round-trip by
 * {@code com.koomplo.wizardry.core.mixin.AttributeModifierMixin} (which re-populates this map on load and writes the
 * condition on save).
 */
public final class WizardryAttributeModifier {
    private WizardryAttributeModifier() {
    }

    /** Modifier id -> spell condition. See class javadoc. */
    public static final Map<ResourceLocation, SpellCondition> CONDITIONS = new ConcurrentHashMap<>();

    /**
     * Creates an attribute modifier and records its (optional) {@link SpellCondition} against the modifier's id.
     */
    public static AttributeModifier create(ResourceLocation id, double amount, AttributeModifier.Operation operation,
                                           @Nullable SpellCondition condition) {
        AttributeModifier modifier = new AttributeModifier(id, amount, operation);
        if (condition != null) CONDITIONS.put(id, condition);
        return modifier;
    }

    /** Returns the {@link SpellCondition} associated with the given modifier, or {@code null} if it is unconditional. */
    public static @Nullable SpellCondition getCondition(AttributeModifier modifier) {
        return CONDITIONS.get(modifier.id());
    }

    public static void onPreCast(SpellCastEvent.Pre event) {
        if (event.getCaster() == null) return;
        LivingEntity caster = event.getCaster();

        safeAddModifiers(event.getModifiers(), SpellModifiers.POTENCY, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_POTENCY)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.COST, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_COST)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.CHARGEUP, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_CHARGEUP)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.PROGRESSION, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_PROGRESSION)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.DURATION, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_DURATION)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.BLAST, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_BLAST)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.RANGE, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_RANGE)));
        safeAddModifiers(event.getModifiers(), SpellModifiers.COOLDOWN, calculateModifiers(caster, event.getSpell(), EBAttributes.holder(EBAttributes.CAST_COOLDOWN)));
    }

    public static void safeAddModifiers(SpellModifiers modifiers, String key, float value) {
        if (value != 0) modifiers.add(key, value);
    }

    /**
     * Search and load the attribute modifiers saved in the living entity, first, organize the attributes based on the
     * operation order and then values the vanilla attribute modifiers and wizard attribute modifiers (modifiers with
     * conditions). In case the entity doesn't have the attribute instance it will return 0.
     *
     * @param entity    living entity that could have the given attribute
     * @param spell     result of the casting/logic made by the entity
     * @param attribute attribute that needs to be check in order to find its modifiers
     * @return the calculation result of all the modifiers, 0 if there wasn't any modifiers or the entity doesn't have the
     * attribute instance
     */
    public static float calculateModifiers(LivingEntity entity, Spell spell, Holder<Attribute> attribute) {
        double value = 0;
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return (float) value;

        List<AttributeModifier> attributes = instance.getModifiers().stream().sorted(Comparator.comparingInt(m -> m.operation().id())).toList();
        for (AttributeModifier attributeModifier : attributes) {
            SpellCondition condition = getCondition(attributeModifier);
            if (condition == null || condition.isEmpty()) {
                value = apply(value, attributeModifier);
                continue;
            }

            if (condition.test(spell)) {
                value = apply(value, attributeModifier);
            }
        }

        return (float) value;
    }

    /**
     * Search and load the attribute modifiers saved in the living entity, first, organize the attributes based on the
     * operation order and then values the vanilla attribute modifiers and wizard attribute modifiers (modifiers with
     * conditions). In case the entity doesn't have the attribute instance it will return 0.
     *
     * @param entity    living entity that could have the given attribute
     * @param condition filter that goes to test the modifiers result
     * @param attribute attribute that needs to be check in order to find its modifiers
     * @return the calculation result of all the modifiers, 0 if there wasn't any modifiers or the entity doesn't have the
     * attribute instance
     */
    public static float calculateModifiers(LivingEntity entity, SpellCondition condition, Holder<Attribute> attribute) {
        double value = 0;
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return (float) value;

        List<AttributeModifier> attributes = instance.getModifiers().stream().sorted(Comparator.comparingInt(m -> m.operation().id())).toList();
        for (AttributeModifier attributeModifier : attributes) {
            SpellCondition modifierCondition = getCondition(attributeModifier);
            if (modifierCondition == null || modifierCondition.isEmpty()) {
                value = apply(value, attributeModifier);
                continue;
            }

            if (modifierCondition.test(condition)) {
                value = apply(value, attributeModifier);
            }
        }

        return (float) value;
    }

    private static double apply(double value, AttributeModifier modifier) {
        return switch (modifier.operation()) {
            case ADD_VALUE -> value + modifier.amount();
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> value * modifier.amount();
        };
    }
}
