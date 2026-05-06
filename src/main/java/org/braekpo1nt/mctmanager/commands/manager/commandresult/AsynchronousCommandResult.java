package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface AsynchronousCommandResult extends CommandResult {
    void executeAsync(@NotNull CommandSender sender);
}
