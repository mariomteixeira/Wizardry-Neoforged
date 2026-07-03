package com.binaris.wizardry.setup.registries;

import com.binaris.wizardry.content.command.*;
import com.binaris.wizardry.content.command.debug.*;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Command definitions consumed by the {@code RegisterCommandsEvent} handler in
 * {@link com.binaris.wizardry.WizardryForgeEvents.ForgeBusEvents}.
 */
public final class EBCommands {
    // TODO CastCommand::register
    public static final List<BiConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext>> COMMANDS_TO_REGISTER = ImmutableList.of(
            AllyCommand::register,
            DiscoverSpellCommand::register,
            UnDiscoverSpellCommand::register,
            MagicAttributeCommand::register
    );
    public static final List<BiConsumer<CommandDispatcher<CommandSourceStack>, CommandBuildContext>> DEBUG_COMMANDS = ImmutableList.of(
            CastCommand::register,
            ForfeitTestCommand::register,
            GenWandCommand::register,
            GenArmorCommand::register,
            GenSpellBookCommand::register,
            GenScrollCommand::register,
            GenWizardCommand::register,
            DataDurabilityCommand::register
    );

    private EBCommands() {
    }
}
