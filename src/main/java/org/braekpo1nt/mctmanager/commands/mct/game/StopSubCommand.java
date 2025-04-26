package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles stopping the current game
 */
public class StopSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public StopSubCommand(GameManager gameManager, String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return gameManager.stopAllGames();
        }
        if (args.length == 1) {
            String gameID = args[0];
            if (gameID.equals("all")) {
                return gameManager.stopAllGames();
            }
            GameType gameType = GameType.fromID(gameID);
            if (gameType == null) {
                return CommandResult.failure(Component.text(gameID)
                        .append(Component.text(" is not a valid game")));
            }
            return gameManager.stopGame(gameType);
        }
        return CommandResult.failure(getUsage().of("[all|gameID]"));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchTabList(
                    gameManager.getActiveGames().stream().map(GameType::getId).toList(), 
                    args[0]);
        }
        
        return Collections.emptyList();
    }
}
