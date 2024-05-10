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
public abstract class CommandManager extends TabSubCommand {
    
    /**
     * Your super command's sub commands. You use your command manager to call one of these commands.
     * The key is the command's name (what you type in the chat to reference this command).format
     */
    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    
    public CommandManager(@NotNull String name) {
        super(name);
    }
    
    /**
     * Initialize this subCommand
     */
    public void onInit() {
        for (SubCommand subCommand : subCommands.values()) {
            subCommand.setParent(this);
            subCommand.setPermissionNode(String.format("%s.%s", getPermissionNode(), subCommand.getName()));
            if (subCommand instanceof CommandManager commandManager) {
                commandManager.onInit();
            }
        }
    }
    
    /**
     * Add a new {@link SubCommand} implementation to the {@link CommandManager#subCommands} map.
     * This can now be called as a sub command using {@link SubCommand#getName()}.
     * @param subCommand the implementation of {@link SubCommand} to add
     */
    public void addSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }
    
    /**
     * @return every down-stream permissionNode
     */
    public @NotNull List<@NotNull String> getSubPermissionNodes() {
        List<String> result = new ArrayList<>(subCommands.size());
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.getPermissionNode() != null) {
                result.add(subCommand.getPermissionNode());
            }
            if (subCommand instanceof CommandManager commandManager) {
                result.addAll(commandManager.getSubPermissionNodes());
            }
        }
        return result;
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
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of(getSubCommandUsageArg()));
        }
        String subCommandName = args[0];
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null || !subCommand.hasPermission(sender)) {
            return CommandResult.failure(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized.")));
        }
        return subCommand.onSubCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream().filter(subCommandName -> subCommands.get(subCommandName).hasPermission(sender)).sorted().toList();
        }
        if (args.length > 1) {
            String subCommandName = args[0];
            SubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand == null || !subCommand.hasPermission(sender)) {
                return Collections.emptyList();
            }
            if (subCommand instanceof TabCompleter subTabCommand) {
                return subTabCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
}
