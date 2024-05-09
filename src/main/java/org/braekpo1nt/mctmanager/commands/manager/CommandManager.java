package org.braekpo1nt.mctmanager.commands.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.*;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
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
    protected @Nullable String permissionNode;
    
    public CommandManager(@NotNull String name, @Nullable String permissionNode) {
        super(name);
        this.permissionNode = permissionNode;
    }
    
    public CommandManager(@NotNull String name) {
        super(name);
        this.permissionNode = null;
    }
    
    /**
     * Check if the given permissible has the permission to use the given subCommand with the given name.
     * If the subCommand's permission node is null, then that registers as having the permission. 
     * @param permissible the permissible to check if they have the permission
     * @param subCommandName the name of the subCommand to check if the given permissible has permission to use
     * @return true of the given permissible has permission to use the subCommand with the given name, false otherwise. If the given subcommand doesn't have a permission associated with it, then this will return true. 
     */
    protected boolean hasPermission(@NotNull Permissible permissible, String subCommandName) {
        String permissionNode = subCommandPermissionNodes.get(subCommandName);
        if (permissionNode == null) {
            return true;
        }
        return permissible.hasPermission(permissionNode);
    }
    
    protected boolean hasPermission(@NotNull Permissible permissible) {
        if (permissionNode == null) {
            return true;
        }
        return permissible.hasPermission(permissionNode);
    }
    
    /**
     * Register this {@link CommandManager}'s permission nodes with the given PluginManager. 
     * If any subCommands are also {@link CommandManager}s, this registers their permissions as well.
     * @param pluginManager the PluginManager to register the permission nodes with
     */
    public void registerPermissions(PluginManager pluginManager) {
        if (permissionNode != null) {
            pluginManager.addPermission(new Permission(permissionNode));
        }
        for (String subCommandPermissionNode : subCommandPermissionNodes.values()) {
            if (subCommandPermissionNode != null) {
                pluginManager.addPermission(new Permission(subCommandPermissionNode));
            }
        }
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand instanceof CommandManager commandManager) {
                commandManager.registerPermissions(pluginManager);
            }
        }
    }
    
    /**
     * Add a new {@link SubCommand} implementation to the {@link CommandManager#subCommands} map.
     * This can now be called as a sub command using {@link SubCommand#getName()}.
     * @param subCommand the implementation of {@link SubCommand} to add
     */
    public void addSubCommand(SubCommand subCommand) {
        subCommand.setParent(this);
        subCommands.put(subCommand.getName(), subCommand);
        if (permissionNode != null) {
            subCommandPermissionNodes.put(subCommand.getName(), String.format("%s.%s", permissionNode, subCommand.getName()));
        }
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
        if (!hasPermission(sender)) {
            return CommandResult.failure("Unknown or incomplete command.");
        }
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of(getSubCommandUsageArg()));
        }
        String subCommandName = args[0];
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null || !hasPermission(sender, subCommandName)) {
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
