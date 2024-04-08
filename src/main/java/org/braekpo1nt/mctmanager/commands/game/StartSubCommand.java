package org.braekpo1nt.mctmanager.commands.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
        if (args.length != 1) {
            sender.sendMessage("Usage: /mct game start <game>");
            return true;
        }
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            sender.sendMessage(Component.text(gameID)
                    .append(Component.text(" is not a valid game"))
                    .color(NamedTextColor.RED));
            return true;
        }
        if (gameManager.getEventManager().eventIsActive()) {
            sender.sendMessage(Component.text("Can't manually start a game while an event is active.")
                    .color(NamedTextColor.RED));
            return true;
        }
        gameManager.startGame(gameType, sender);
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return GameType.GAME_IDS.keySet().stream().sorted().toList();
        }
        return Collections.emptyList();
    }
}
