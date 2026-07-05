package com.koomplo.wizardry.content.item;

import com.koomplo.wizardry.api.content.util.MagicDamageSource;
import com.koomplo.wizardry.content.spell.DefaultProperties;
import com.koomplo.wizardry.setup.registries.EBDamageSources;
import com.koomplo.wizardry.setup.registries.EBItems;
import com.koomplo.wizardry.setup.registries.Spells;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

public class FlamingAxeItem extends AxeItem {

    public FlamingAxeItem() {
        super(MagicItemTier.TIER, new Properties().durability(12000).rarity(Rarity.UNCOMMON).attributes(AxeItem.createAttributes(MagicItemTier.TIER, 8, -3)));
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity entity) {
        if (!MagicDamageSource.isEntityImmune(EBDamageSources.FIRE, target))
            target.igniteForSeconds(Spells.FLAMING_AXE.property(DefaultProperties.EFFECT_DURATION));
        return false;
    }


    @Override
    public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack stack1) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }
}
