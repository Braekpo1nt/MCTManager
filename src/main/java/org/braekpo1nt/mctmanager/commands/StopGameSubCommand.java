package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StopGameSubCommand implements CommandExecutor {
    
    private final GameManager gameManager;
    
    public StopGameSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        gameManager.stopGame();
        return true;
    }
}
