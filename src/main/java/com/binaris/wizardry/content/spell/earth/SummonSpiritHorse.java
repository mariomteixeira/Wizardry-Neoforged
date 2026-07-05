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
import com.binaris.wizardry.content.entity.living.SpiritHorse;
import com.binaris.wizardry.content.spell.DefaultProperties;
import com.binaris.wizardry.setup.registries.EBAttachments;
import com.binaris.wizardry.setup.registries.Elements;
import com.binaris.wizardry.setup.registries.SpellTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SummonSpiritHorse extends Spell {

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
            // Only one spirit horse per player: re-casting replaces the old one
            Entity oldHorse = EntityUtil.getEntityByUUID(world, data.getSpiritHorseUUID());
            if (oldHorse != null) oldHorse.discard();

            BlockPos pos = BlockUtil.findNearbyFloorSpace(caster, property(DefaultProperties.SUMMON_RADIUS), 4);
            if (pos == null) return false;

            SpiritHorse horse = new SpiritHorse(world);
            horse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            horse.setTamed(true);
            horse.setOwnerUUID(caster.getUUID());
            horse.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.NEUTRAL);
            world.addFreshEntity(horse);

            // 1.12.2: speed scales fully with potency; jump strength at 25% effect (increases ridiculously fast)
            float potency = ctx.modifiers().get(SpellModifiers.POTENCY);
            horse.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier(
                    WizardryMainMod.location("potency"), potency - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            horse.getAttribute(Attributes.JUMP_STRENGTH).addPermanentModifier(new AttributeModifier(
                    WizardryMainMod.location("potency"), (1 + (potency - 1) * 0.25f) - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            data.setSpiritHorseUUID(horse.getUUID());
        }

        this.playSound(world, caster, ctx.castingTicks(), -1);
        return true;
    }

    @Override
    protected @NotNull SpellProperties properties() {
        return SpellProperties.builder()
                .assignBaseProperties(SpellTiers.ADVANCED, Elements.EARTH, SpellType.MINION, SpellAction.SUMMON, 50, 10, 150)
                .add(DefaultProperties.SUMMON_RADIUS, 2)
                .build();
    }
}
