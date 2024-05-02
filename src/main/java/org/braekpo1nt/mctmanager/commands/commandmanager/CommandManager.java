package org.braekpo1nt.mctmanager.commands.commandmanager;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class CommandManager implements TabExecutor {
    
    private final SubCommandManager subCommandManager;
    
    public CommandManager(@NotNull SubCommandManager subCommandManager, @NotNull String name, @NotNull Main plugin) {
        this.subCommandManager = subCommandManager;
        PluginCommand command = plugin.getCommand(name);
        Preconditions.checkArgument(command != null, "Could not find command with name \"%s\"", name);
        command.setExecutor(this);
    }
    
    public abstract Component getUsageMessage();
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        subCommandManager.onCommand(sender, command, label, args);
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return subCommandManager.onTabComplete(sender, command, label, args);
    }
}
