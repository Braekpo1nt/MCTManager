package org.braekpo1nt.mctmanager.commands.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A bukkit command responsible for managing a set of sub commands.
 * Add classes which implement {@link SubCommand} to the {@link CommandManager#subCommands} map to add executable sub commands. 
 * Implement {@link TabSubCommand} in your sub command to provide tab completion
 */
public abstract class CommandManager extends TabSubCommand implements CommandExecutor {
    
    /**
     * Your super command's sub commands. You use your command manager to call one of these commands.
     * The key is the command's name (what you type in the chat to reference this command).format
     */
    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    protected final Map<String, String> subCommandPermissionNodes = new HashMap<>();
    protected final @NotNull String permissionNode;
    
    public CommandManager(@NotNull String name, @NotNull String permissionNode) {
        super(name);
        this.permissionNode = permissionNode;
    }
    
    public CommandManager(@NotNull String name) {
        super(name);
        this.permissionNode = name;
    }
    
    /**
     * Add a new {@link SubCommand} implementation to the {@link CommandManager#subCommands} map.
     * This can now be called as a sub command using {@link SubCommand#getName()}.
     * @param subCommand the implementation of {@link SubCommand} to add
     */
    public void addSubCommand(SubCommand subCommand) {
        subCommand.setParent(this);
        subCommands.put(subCommand.getName(), subCommand);
        subCommandPermissionNodes.put(subCommand.getName(), String.format("%s.%s", permissionNode, subCommand.getName()));
    }
    
    /**
     * Returns the {@link SubCommand}s as a usage arg
     * This is used as the options of the usage message for if the sender doesn't provide any options.
     * @return this {@link CommandManager}'s sub-commands as an options-list style argument (in the form {@code "<arg1|arg2|arg3>"}). {@code "<options>"} if there are no subCommands. 
     */
    protected @NotNull Usage getSubCommandUsageArg() {
        return Usage.toArgOptions(subCommands.keySet());
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandResult commandResult = onSubCommand(sender, command, label, args);
        Component message = commandResult.getMessage();
        if (message != null) {
            sender.sendMessage(message);
        }
        return true;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(permissionNode)) {
            return CommandResult.failure("Unknown or incomplete command.");
        }
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of(getSubCommandUsageArg()));
        }
        String subCommandName = args[0];
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null || hasPermission(sender, subCommandName)) {
            return CommandResult.failure(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized.")));
        }
        return subCommand.onSubCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }
    
    protected boolean hasPermission(@NotNull CommandSender sender, String subCommandName) {
        String permissionNode = subCommandPermissionNodes.get(subCommandName);
        if (permissionNode == null) {
            return true;
        }
        return sender.hasPermission(permissionNode);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream().sorted().toList();
        }
        if (args.length > 1) {
            String subCommandName = args[0];
            if (!subCommands.containsKey(subCommandName)) {
                return Collections.emptyList();
            }
            SubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand instanceof TabCompleter subTabCommand) {
                return subTabCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
}
