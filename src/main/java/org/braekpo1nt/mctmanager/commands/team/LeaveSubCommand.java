package org.braekpo1nt.mctmanager.commands.team;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LeaveSubCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    public LeaveSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            sender.sendMessage("Usage: /mct team leave <member>");
            return true;
        }
        String playerName = args[0];
        OfflinePlayer playerToLeave = Bukkit.getOfflinePlayer(playerName);
        if (!gameManager.isParticipant(playerToLeave.getUniqueId())) {
            sender.sendMessage(String.format("Player %s is not on a team.", playerName));
            return true;
        }
        String teamName = gameManager.getTeamName(playerToLeave.getUniqueId());
        gameManager.leavePlayer(playerToLeave);
        sender.sendMessage(String.format("Removed %s from team %s", playerName, teamName));
        return true;
    }
}
