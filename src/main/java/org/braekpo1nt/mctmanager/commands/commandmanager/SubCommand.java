package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface SubCommand {
    @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args);
}
