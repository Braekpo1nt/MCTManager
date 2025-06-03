package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<gameId|all>").of("[configFile.json]"));
        }
        
        String gameID = args[0];
        if (gameID.equals("all")) {
            return gameManager.stopAllGames();
        }
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.text(gameID)
                    .append(Component.text(" is not a valid game")));
        }
        
        String configFile = args[1];
        return gameManager.stopGame(new GameInstanceId(gameType, configFile));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchTabList(
                    gameManager.getActiveGames().stream()
                            .map(id -> id.getGameType().getId())
                            .toList(),
                    args[0]);
        }
        if (args.length == 2) {
            return CommandUtils.partialMatchTabList(
                    gameManager.getActiveGames().stream()
                            .map(GameInstanceId::getConfigFile)
                            .toList(),
                    args[1]);
        }
        
        return Collections.emptyList();
    }
}
