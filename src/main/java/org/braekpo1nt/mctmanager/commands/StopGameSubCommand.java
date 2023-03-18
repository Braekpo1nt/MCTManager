package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles stopping the current game
 */
public class StopGameSubCommand implements CommandExecutor {
    
    private final GameManager gameManager;
    
    public StopGameSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only a Player can run this command.");
            return true;
        }
        gameManager.stopGame(((Player) sender));
        return true;
    }
}
