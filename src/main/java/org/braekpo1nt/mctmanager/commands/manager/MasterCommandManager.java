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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MasterCommandManager extends CommandManager implements TabExecutor {
    @Getter
    protected final @NotNull String permissionNode;
    
//    protected final @NotNull Map<String, SubCommand> subCommands = new HashMap<>();
//    protected final @NotNull Map<String, String> subCommandPermissionNodes = new HashMap<>();
    
    public MasterCommandManager(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull String permissionNode) {
        super(name);
        this.permissionNode = permissionNode;
        PluginCommand command = plugin.getCommand(getName());
        Preconditions.checkArgument(command != null, "Can't find command %s", getName());
        command.setExecutor(this);
    }
    
    public MasterCommandManager (@NotNull JavaPlugin plugin, @NotNull String name) {
        super(name);
        PluginCommand command = plugin.getCommand(getName());
        Preconditions.checkArgument(command != null, "Can't find command %s", getName());
        command.setExecutor(this);
        String permission = command.getPermission();
        Preconditions.checkArgument(permission != null, "Permission could not be found for command %s");
        this.permissionNode = permission;
    }
    
    public void addSubCommand(@NotNull SubCommand subCommand) {
        super.addSubCommand(subCommand);
        subCommandPermissionNodes.put(subCommand.getName(), String.format("%s.%s", permissionNode, subCommand.getName()));
    }
    
    @Override
    public void registerPermissions(@NotNull PluginManager pluginManager) {
        if (pluginManager.getPermission(permissionNode) == null) {
            pluginManager.addPermission(new Permission(permissionNode));
        }
        for (String subPermissionNode : subCommandPermissionNodes.values()) {
            if (subPermissionNode != null) {
                pluginManager.addPermission(new Permission(subPermissionNode));
            }
        }
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand instanceof CommandManager commandManager) {
                commandManager.registerPermissions(pluginManager);
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
