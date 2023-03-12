package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MCTCommand implements TabExecutor {

    Map<String, CommandExecutor> subCommands = new HashMap<>();
    
    public MCTCommand(Main plugin, GameManager gameManager) {
        plugin.getCommand("mct").setExecutor(this);
        subCommands.put("startgame", new StartGameSubCommand(gameManager));
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct <option>");
            return false;
        }
        if (!subCommands.containsKey(args[0])) {
            sender.sendMessage(String.format("Argument %s is not recognized.", args[0]));
        }
        
        return subCommands.get(args[0]).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subCommandNames = subCommands.keySet().stream().sorted().toList();
            return subCommandNames;
        }
        if (args.length > 1) {
            if (!subCommands.containsKey(args[0])) {
                return null;
            }
            CommandExecutor subCommand = subCommands.get(args[0]);
            if (subCommands instanceof TabExecutor) {
                TabExecutor subTabCommand = ((TabExecutor) subCommand);
                return subTabCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return null;
    }
}
