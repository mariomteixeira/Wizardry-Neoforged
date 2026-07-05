package com.koomplo.wizardry.content.spell_tier;

import com.koomplo.wizardry.api.content.spell.SpellTier;
import net.minecraft.ChatFormatting;

public class ApprenticeTier extends SpellTier {
    public ApprenticeTier() {
        super(1000, 5, 5, 1, ChatFormatting.AQUA, 1500);
    }
}
