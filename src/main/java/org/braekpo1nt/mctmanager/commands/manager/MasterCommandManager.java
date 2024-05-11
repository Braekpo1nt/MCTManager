package org.braekpo1nt.mctmanager.commands.manager;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MasterCommandManager extends CommandManager implements TabExecutor {
    /**
     * Instantiates a new {@link MasterCommandManager} with the given plugin and name
     * @param plugin the plugin to register this command with.
     * @param name the name of this command
     * @throws IllegalArgumentException if the given plugin can't find a command by the given name, or if the given command doesn't have a permission
     */
    public MasterCommandManager(@NotNull JavaPlugin plugin, @NotNull String name) {
        super(name);
        PluginCommand command = plugin.getCommand(getName());
        Preconditions.checkArgument(command != null, "Can't find command %s", getName());
        command.setExecutor(this);
        String permissionNode = command.getPermission();
        Preconditions.checkArgument(permissionNode != null, "Can't find permission for command %s", getName());
        this.setPermissionNode(permissionNode);
    }
    
    /**
     * An overload of {@link CommandManager#onInit()} which performs the normal initialization as well as registers all the permissions with the given PluginManager.
     * This helps other plugins (such as LuckPerms) to see all the appropriate permissions.
     * @param pluginManager the pluginManager to register all the permissions with
     * @see CommandManager#onInit() 
     * @see MasterCommandManager#registerPermissions(PluginManager) 
     */
    public void onInit(PluginManager pluginManager) {
        super.onInit();
        registerPermissions(pluginManager);
    }
    
    /**
     * Register all the downstream permissions as well as this {@link MasterCommandManager}'s permissions (if not already registered).
     * @param pluginManager the pluginManager to register the permissions with
     */
    public void registerPermissions(@NotNull PluginManager pluginManager) {
        if (getPermissionNode() != null && pluginManager.getPermission(getPermissionNode()) == null) {
            pluginManager.addPermission(new Permission(getPermissionNode()));
        }
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.getPermissionNode() != null) {
                pluginManager.addPermission(new Permission(subCommand.getPermissionNode()));
            }
            if (subCommand instanceof CommandManager commandManager) {
                for (String subPermissionNode : commandManager.getSubPermissionNodes()) {
                    pluginManager.addPermission(new Permission(subPermissionNode));
                }
            }
        }
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
}
