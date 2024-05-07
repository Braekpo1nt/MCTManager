package org.braekpo1nt.mctmanager.commands.manager;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SubCommand {
    
    
    /**
     * The name of a {@link SubCommand} is typically used to call it from the command. Analogous to {@link Command#getLabel()}.
     */
    protected final @NotNull String name;
    /**
     * The parent {@link SubCommand} of this {@link SubCommand}. Null if this {@link SubCommand} has no parent.
     */
    protected @Nullable SubCommand parent;
    
    public SubCommand(@NotNull String name) {
        this.name = name;
    }
    
    /**
     * Allows you to specify the this {@link SubCommand}'s parent upon instantiation
     * @param name the name of this command. See {@link SubCommand#getName()}
     * @param parent the parent of this command. See {@link SubCommand#setParent(SubCommand)}
     */
    public SubCommand(@NotNull String name, @Nullable SubCommand parent) {
        this.name = name;
        this.parent = parent;
    }
    
    /**
     * Gives this {@link SubCommand} a reference to its parent command. Can be null if this doesn't have a parent.
     * @param parent the parent {@link SubCommand}. Null if no parent.
     */
    public void setParent(@Nullable SubCommand parent) {
        this.parent = parent;
    }
    
    /**
     * The name of a {@link SubCommand} is typically used to call it from the command. Analogous to {@link Command#getLabel()}.
     * @return the name of this {@link SubCommand}.
     */
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
