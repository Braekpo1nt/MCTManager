package org.braekpo1nt.mctmanager.commands.mct.debug;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
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
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("debug")
                .then(Permissioned.literal("log")
                        .then(Permissioned.argument("logType", new EnumArgumentType<>(LogType.class, LogType.values()))
                                .then(Permissioned.argument("active", BoolArgumentType.bool())
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
