package com.binaris.wizardry.content.spell.earth;

import com.binaris.wizardry.WizardryMainMod;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.api.content.spell.SpellType;
import com.binaris.wizardry.api.content.spell.internal.PlayerCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.api.content.spell.properties.SpellProperties;
import com.binaris.wizardry.api.content.util.BlockUtil;
import com.binaris.wizardry.api.content.util.EntityUtil;
import com.binaris.wizardry.capabilities.WizardDataHolder;
import com.binaris.wizardry.content.entity.living.SpiritWolf;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SummonSpiritWolf extends Spell {

    @Override
    public boolean requiresPacket() {
        return false;
    }

    @Override
    public boolean cast(PlayerCastContext ctx) {
        Player caster = ctx.caster();
        Level world = ctx.world();
        WizardDataHolder data = caster.getData(EBAttachments.WIZARD_DATA);

        if (!world.isClientSide) {
            // Only one spirit wolf per player: re-casting replaces the old one
            Entity oldWolf = EntityUtil.getEntityByUUID(world, data.getSpiritWolfUUID());
            if (oldWolf != null) oldWolf.discard();

            BlockPos pos = BlockUtil.findNearbyFloorSpace(caster, property(DefaultProperties.SUMMON_RADIUS), 4);
            if (pos == null) return false;

            SpiritWolf wolf = new SpiritWolf(world);
            wolf.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            wolf.tame(caster);

            // Potency gives the wolf more strength AND more health (1.12.2: MULTIPLY_CUMULATIVE, amplified 1.5x)
            float potency = ctx.modifiers().get(SpellModifiers.POTENCY);
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(
                    WizardryMainMod.location("potency"), potency - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            wolf.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(
                    WizardryMainMod.location("potency"), (1 + (potency - 1) * 1.5f) - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            wolf.setHealth(wolf.getMaxHealth());

            world.addFreshEntity(wolf);

            data.setSpiritWolfUUID(wolf.getUUID());
        }

        this.playSound(world, caster, ctx.castingTicks(), -1);
        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.APPRENTICE, Elements.EARTH, SpellType.MINION, SpellAction.SUMMON, 25, 0, 100)
                .add(DefaultProperties.SUMMON_RADIUS, 2)
                .build();
    }
}
