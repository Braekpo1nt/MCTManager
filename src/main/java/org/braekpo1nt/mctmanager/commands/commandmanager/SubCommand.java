package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SubCommand {
    
    
    protected final @NotNull String name;
    protected @Nullable SubCommand parent;
    
    public SubCommand(@NotNull String name) {
        this.name = name;
    }
    
    public void setParent(@Nullable SubCommand parent) {
        this.parent = parent;
    }
    
    public @NotNull String getName() {
        return name;
    }
    
    /**
     * Used to get the usage message of this command upstream from the current context. 
     * For example, if this sub-command is the "stop" portion of "/mct game stop [true|false]", then this will return "/mct game stop". You can then optionally add on the specific usage arguments the user incorrectly used with {@link Usage#of(String)}, e.g. "getUsage().of("[true|false]")". 
     * This can then be wrapped in a {@link CommandResult}, converted to a {@link net.kyori.adventure.text.Component}, etc.
     * @return the usage of this sub-command from the `/` character up to and including this sub-command's name.
     */
    public @NotNull Usage getUsage() {
        if (parent == null) {
            return new Usage(name);
        }
        return parent.getUsage().of(name);
    }
    
    abstract public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args);
}
