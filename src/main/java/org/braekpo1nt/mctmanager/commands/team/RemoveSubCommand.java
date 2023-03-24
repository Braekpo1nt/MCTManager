package org.braekpo1nt.mctmanager.commands.team;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class RemoveSubCommand implements TabExecutor {
    private final GameManager gameManager;
    
    public RemoveSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct team remove <team>");
            return true;
        }
        String removeTeamName = args[0];
        try {
            boolean teamExists = gameManager.removeTeam(removeTeamName);
            if (!teamExists) {
                sender.sendMessage(String.format("Team \"%s\" does not exist", removeTeamName));
                return true;
            }
            sender.sendMessage(String.format("Removed team \"%s\".", removeTeamName));
        } catch (IOException e) {
            sender.sendMessage("Error removing team. See log for error message.");
            Bukkit.getLogger().severe("Error saving game state while removing team.");
            throw new RuntimeException(e);
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return gameManager.getTeamNames().stream().sorted().toList();
    }
}
