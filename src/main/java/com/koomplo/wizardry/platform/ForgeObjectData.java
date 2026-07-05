package com.koomplo.wizardry.platform;

import com.koomplo.wizardry.api.content.data.*;
import com.koomplo.wizardry.capabilities.*;
import com.koomplo.wizardry.content.spell.abstr.ConjureItemSpell;
import com.koomplo.wizardry.core.platform.services.IObjectData;
import com.koomplo.wizardry.setup.registries.EBAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Arrays;

public class ForgeObjectData implements IObjectData {
    @Override
    public ConjureData getConjureData(ItemStack stack) {
        if (!ConjureItemSpell.isSummonableItem(stack)) return null;
        return ConjureDataHolder.get(stack);
    }

    @Override
    public ImbuementEnchantData getImbuementData(ItemStack stack) {
        if (!(stack.getItem() instanceof TieredItem) || !stack.isEnchantable()) return null;
        return ImbuementEnchantDataHolder.get(stack);
    }

    @Override
    public CastCommandData getCastCommandData(Player player) {
        return player.getData(EBAttachments.CAST_COMMAND_DATA);
    }

    @Override
    public SpellManagerData getSpellManagerData(Player player) {
        return player.getData(EBAttachments.SPELL_MANAGER_DATA);
    }

    @Override
    public WizardData getWizardData(Player player) {
        return player.getData(EBAttachments.WIZARD_DATA);
    }

    @Override
    public MinionData getMinionData(Mob mob) {
        return mob.getData(EBAttachments.MINION_DATA);
    }

    @Override
    public ContainmentData getContainmentData(LivingEntity entity) {
        return entity.getData(EBAttachments.CONTAINMENT_DATA);
    }

    @Override
    public ArcaneLockData getArcaneLockData(BlockEntity blockEntity) {
        return blockEntity.getData(EBAttachments.ARCANE_LOCK_DATA);
    }

    @Override
    public void spellStoredVariables(IStoredSpellVar<?>... variables) {
        SpellManagerDataHolder.storedVariables.addAll(Arrays.asList(variables));
    }

    @Override
    public boolean isMinion(Entity mob) {
        if (!(mob instanceof Mob m)) return false;
        return getMinionData(m).isSummoned();
    }
}
