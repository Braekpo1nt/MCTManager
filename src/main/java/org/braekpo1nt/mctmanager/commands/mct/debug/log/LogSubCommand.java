package org.braekpo1nt.mctmanager.commands.mct.debug.log;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LogSubCommand extends TabSubCommand {
    
    public LogSubCommand(@NotNull String name) {
        super(name);
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("<logType>").of("<true|false>"));
        }
        String logTypeString = args[0];
        LogType logType = LogType.byId(logTypeString);
        if (logType == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(logTypeString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid LogType")));
        }
        
        String activeString = args[1];
        Boolean active = CommandUtils.toBoolean(activeString);
        if (active == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(activeString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid boolean"))
            );
        }
        Main.setLogTypeActive(logType, active);
        return CommandResult.success(Component.empty()
                .append(Component.text(logType.getId()))
                .append(Component.text(" set to "))
                .append(Component.text(active))
        );
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchTabList(LogType.getIDs(), args[0]);
        }
        if (args.length == 2) {
            return List.of("true", "false");
        }
        return Collections.emptyList();
    }
}
