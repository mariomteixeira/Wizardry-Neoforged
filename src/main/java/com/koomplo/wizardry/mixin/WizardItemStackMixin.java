package com.koomplo.wizardry.mixin;

import com.koomplo.wizardry.api.content.item.ICustomDamageItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class WizardItemStackMixin {
    @Unique ItemStack stack = (ItemStack) (Object) this;

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    public void EBWIZARDRY$wandGetMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if (!(stack.getItem() instanceof ICustomDamageItem wizardryItem)) return;
        int maxDamage = wizardryItem.getCustomMaxDamage(stack);
        cir.setReturnValue(maxDamage);
    }

    @Inject(method = "setDamageValue", at = @At("HEAD"), cancellable = true)
    public void EBWIZARDRY$wandSetDamage(int damage, CallbackInfo ci) {
        if (!(stack.getItem() instanceof ICustomDamageItem wizardryItem)) return;
        wizardryItem.setCustomDamage(stack, damage);
        ci.cancel();
    }

    // hurtAndBreak() shrinks the stack itself once damage reaches max; redirect that shrink so custom-damage
    // items (e.g. wands) can stay at 0 durability instead of being destroyed, mirroring ICustomDamageItem#canBreak.
    @Redirect(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void EBWIZARDRY$itemHurtBreak(ItemStack self, int amount) {
        if (self.getItem() instanceof ICustomDamageItem customDamageItem && !customDamageItem.canBreak(self)) return;
        self.shrink(amount);
    }
}
