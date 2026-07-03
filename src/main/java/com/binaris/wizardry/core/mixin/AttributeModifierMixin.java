package com.binaris.wizardry.core.mixin;

import com.binaris.wizardry.api.content.spell.SpellCondition;
import com.binaris.wizardry.content.WizardryAttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link AttributeModifier} is a {@code final record} in 1.21, so the mod's conditional modifiers can no longer be a
 * subclass. Instead the {@link SpellCondition} is kept side-band in {@link WizardryAttributeModifier#CONDITIONS} and
 * this mixin round-trips it through the vanilla attribute NBT so it survives save/load.
 */
@Mixin(AttributeModifier.class)
public abstract class AttributeModifierMixin {

    // Persist the spell condition into the modifier NBT on save.
    @Inject(method = "save", at = @At("RETURN"))
    private void EBWIZARDRY$attribute_save(CallbackInfoReturnable<CompoundTag> cir) {
        SpellCondition condition = WizardryAttributeModifier.getCondition((AttributeModifier) (Object) this);
        if (condition != null) condition.save(cir.getReturnValue());
    }

    // Re-populate the condition map from the modifier NBT on load.
    @Inject(method = "load", at = @At("RETURN"))
    private static void EBWIZARDRY$attribute_load(CompoundTag nbt, CallbackInfoReturnable<AttributeModifier> cir) {
        AttributeModifier result = cir.getReturnValue();
        if (result == null) return;
        SpellCondition condition = SpellCondition.load(nbt);
        if (condition != null) WizardryAttributeModifier.CONDITIONS.put(result.id(), condition);
    }
}
