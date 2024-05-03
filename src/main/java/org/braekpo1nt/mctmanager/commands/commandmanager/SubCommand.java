package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.UsageCommandResult;
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
    
    /**
     * @return the usage of this command
     * @deprecated removing this in favor of {@link SubCommand#usage}
     */
    @Deprecated
    public @NotNull UsageCommandResult getUsage() {
        return new UsageCommandResult(getName());
    }
    
    public @NotNull Usage usage() {
        if (parent == null) {
            return new Usage(name);
        }
        return parent.usage().of(name);
    }
    
    abstract public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args);
}
