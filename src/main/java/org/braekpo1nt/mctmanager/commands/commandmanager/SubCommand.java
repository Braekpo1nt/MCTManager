package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SubCommand {
    
    
    protected final @NotNull String name;
    protected @Nullable SubCommand parent;
    
    public void setParent(@Nullable SubCommand parent) {
        this.parent = parent;
    }
    
    public SubCommand(@NotNull String name) {
        this.name = name;
    }
    
    public @NotNull String getName() {
        return name;
    }
    
    public @NotNull Usage getUsage() {
        if (parent == null) {
            return new Usage(name);
        }
        return parent.getUsage().of(name);
    }
    
    abstract public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args);
}
