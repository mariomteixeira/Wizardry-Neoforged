package com.koomplo.wizardry.content.item.artifact;

import com.koomplo.wizardry.api.content.event.SpellCastEvent;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.core.IArtifactEffect;
import com.koomplo.wizardry.setup.registries.Elements;
import net.minecraft.world.item.ItemStack;

public class RingStormEffect implements IArtifactEffect {
    @Override
    public void onSpellPreCast(SpellCastEvent.Pre e, ItemStack artifact) {
        if (e.getSpell().getElement() == Elements.LIGHTNING && e.getLevel().isThundering()) {
            e.getModifiers().multiply(SpellModifiers.COOLDOWN, 0.3f);
        }
    }
}
