package org.braekpo1nt.mctmanager.commands.game;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles stopping the current game
 */
public class StopSubCommand implements CommandExecutor {
    
    private final GameManager gameManager;
    
    public StopSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        gameManager.manuallyStopGame(sender);
        return true;
    }
}
