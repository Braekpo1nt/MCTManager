package org.braekpo1nt.mctmanager.commands.team;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    private final Map<String, CommandExecutor> subCommands = new HashMap<>();
    
    public TeamSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
        subCommands.put("add", new AddSubCommand(gameManager));
        subCommands.put("join", new JoinSubCommand(gameManager));
        subCommands.put("leave", new LeaveSubCommand(gameManager));
        subCommands.put("list", new ListSubCommand(gameManager));
        subCommands.put("remove", new RemoveSubCommand(gameManager));
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct team <options>");
            return true;
        }
        String subCommandName = args[0];
        if (!subCommands.containsKey(subCommandName)) {
            sender.sendMessage(String.format("Argument %s is not recognized.", subCommandName));
        }
        
        return subCommands.get(subCommandName).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subCommandNames = subCommands.keySet().stream().sorted().toList();
            return subCommandNames;
        }
        if (args.length > 1) {
            String subCommandName = args[0];
            if (!subCommands.containsKey(subCommandName)) {
                return null;
            }
            CommandExecutor subCommand = subCommands.get(subCommandName);
            if (subCommand instanceof TabExecutor) {
                TabExecutor subTabCommand = ((TabExecutor) subCommand);
                return subTabCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return null;
    }
}
