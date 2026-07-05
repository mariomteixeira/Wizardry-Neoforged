package com.koomplo.wizardry.content.spell.magic;

import com.koomplo.wizardry.api.content.spell.SpellAction;
import com.koomplo.wizardry.api.content.spell.SpellType;
import com.koomplo.wizardry.api.content.spell.internal.CastContext;
import com.koomplo.wizardry.api.content.spell.internal.SpellModifiers;
import com.koomplo.wizardry.api.content.spell.properties.SpellProperties;
import com.koomplo.wizardry.content.entity.projectile.ForceArrow;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.content.spell.abstr.ArrowSpell;
import com.koomplo.wizardry.setup.registries.Elements;
import com.koomplo.wizardry.setup.registries.SpellTiers;
import org.jetbrains.annotations.NotNull;

public class ForceArrowSpell extends ArrowSpell<ForceArrow> {
    public ForceArrowSpell() {
        super(ForceArrow::new);
    }

    @Override
    protected void addArrowExtras(CastContext ctx, ForceArrow arrow) {
        arrow.setMana((int) (this.getCost() * ctx.modifiers().get(SpellModifiers.COST)));
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.SORCERY, SpellType.PROJECTILE, SpellAction.POINT, 15, 0, 20)
                .add(DefaultProperties.RANGE, 20f)
                .add(DefaultProperties.DAMAGE, 7f)
                .build();
    }
}
