package org.braekpo1nt.mctmanager.commands.manager;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MasterCommandManager extends CommandManager implements TabExecutor {
    public MasterCommandManager(@NotNull JavaPlugin plugin, @NotNull String name) {
        super(name);
        PluginCommand command = plugin.getCommand(getName());
        Preconditions.checkArgument(command != null, "Can't find command %s", getName());
        command.setExecutor(this);
        String permissionNode = command.getPermission();
        Preconditions.checkArgument(permissionNode != null, "Can't find permission for command %s", getName());
        this.setPermissionNode(permissionNode);
    }
    
    public void onInit(PluginManager pluginManager) {
        super.onInit();
        registerPermissions(pluginManager);
    }
    
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
