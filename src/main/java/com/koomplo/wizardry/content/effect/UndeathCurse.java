package com.koomplo.wizardry.content.effect;

import com.koomplo.wizardry.api.content.effect.CurseMobEffect;
import com.koomplo.wizardry.content.item.armor.WizardArmorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UndeathCurse extends CurseMobEffect {
    public UndeathCurse() {
        super(MobEffectCategory.HARMFUL, 0x685c00);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int $$1) {
        if (livingEntity.level().isDay() && !livingEntity.level().isClientSide) {
            float f = livingEntity.getLightLevelDependentMagicValue();

            if (f > 0.5F && livingEntity.level().random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && livingEntity.level()
                    .canSeeSky(new BlockPos((int) livingEntity.getX(), (int) (livingEntity.getY() + (double) livingEntity.getEyeHeight()), (int) livingEntity.getZ()))) {
                boolean flag = true;
                ItemStack itemstack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);

                if (!itemstack.isEmpty()) {
                    if (itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(itemstack.getDamageValue() + livingEntity.level().random.nextInt(2));
                        if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                            if (itemstack.getItem() instanceof WizardArmorItem) {
                                livingEntity.igniteForSeconds(8);
                            } else {
                                livingEntity.onEquippedItemBroken(itemstack.getItem(), EquipmentSlot.HEAD);
                                livingEntity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                            }
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    livingEntity.igniteForSeconds(8);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        return true;
    }


}
