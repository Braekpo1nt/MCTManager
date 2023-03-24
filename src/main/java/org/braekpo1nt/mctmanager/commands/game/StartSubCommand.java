package org.braekpo1nt.mctmanager.commands.game;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Handles starting games
 */
public class StartSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public StartSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct startgame <game>");
            return true;
        }
        if (args.length == 1) {
            String gameName = args[0];
            gameManager.startGame(gameName, sender);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
//            return Arrays.asList("foot-race", "mecha", "bedwars", "dodgeball", "capture-the-flag", "spleef", "parkour-pathway").stream().sorted().toList();
            return Arrays.asList("foot-race");
        }
        return null;
    }
}
