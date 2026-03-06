package org.braekpo1nt.mctmanager.commands.manager.brigadier.debug;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements BrigadierSubCommand {
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("debug")
                .then(Commands.literal("log")
                        .then(Commands.argument("logType", new EnumArgumentType<>(LogType.class, LogType.values()))
                                .then(Commands.argument("active", BoolArgumentType.bool())
                                        .executes(BrigadierAdapters.wraps(this::executeLog))
                                )
                        )
                );
    }
    
    private CommandResult executeLog(CommandContext<CommandSourceStack> ctx) {
        LogType logType = ctx.getArgument("logType", LogType.class);
        boolean active = ctx.getArgument("active", Boolean.class);
        Main.setLogTypeActive(logType, active);
        return CommandResult.success(Component.empty()
                .append(Component.text(logType.getId()))
                .append(Component.text(" set to "))
                .append(Component.text(active))
        );
    }
}
