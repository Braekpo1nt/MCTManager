package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.UsageCommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class SubCommand {
    
    
    protected final @NotNull String name;
    
    public SubCommand(@NotNull String name) {
        this.name = name;
    }
    
    public @NotNull String getName() {
        return name;
    }
    
    /**
     * @return the usage of this command
     */
    public @NotNull UsageCommandResult getUsage() {
        return new UsageCommandResult(getName());
    }
    
    abstract public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args);
}
