package com.koomplo.wizardry.setup.registries;

import com.koomplo.wizardry.WizardryMainMod;
import com.koomplo.wizardry.content.command.argument.ElementArgument;
import com.koomplo.wizardry.content.command.argument.SpellArgument;
import com.koomplo.wizardry.content.command.argument.SpellConditionArgument;
import com.koomplo.wizardry.content.command.argument.SpellTierArgument;
import com.koomplo.wizardry.core.platform.Services;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;

public final class EBArgumentTypeRegistry {
    private EBArgumentTypeRegistry() {
    }

    public static void init() {
        register(WizardryMainMod.location("spell"), SpellArgument.class, SingletonArgumentInfo.contextFree(SpellArgument::new));
        register(WizardryMainMod.location("spell_condition"), SpellConditionArgument.class, SingletonArgumentInfo.contextFree(SpellConditionArgument::new));
        register(WizardryMainMod.location("element"), ElementArgument.class, SingletonArgumentInfo.contextFree(ElementArgument::new));
        register(WizardryMainMod.location("tier"), SpellTierArgument.class, SingletonArgumentInfo.contextFree(SpellTierArgument::new));
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void register(ResourceLocation id, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
        Services.PLATFORM.registerArgumentType(id, clazz, serializer);
    }
}
