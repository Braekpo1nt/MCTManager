package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.UsageCommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface SubCommand {
    
    /**
     * @return the name of the command
     */
    @NotNull String getName();
    
    /**
     * @return the usage of this command
     */
    @NotNull UsageCommandResult getUsage();
    
    @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args);
}
