package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles starting games
 */
public class StartSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    private final Main plugin;
    
    public StartSubCommand(Main plugin, GameManager gameManager, String name) {
        super(name);
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of("<game>").of("[configFile]").of("[teams...]"));
        }
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.text(gameID)
                    .append(Component.text(" is not a valid game")));
        }
        
        String configFile;
        if (args.length >= 2) {
            configFile = args[1];
        } else {
            configFile = "default.json";
        }
        CommandUtils.refreshGameConfigs(plugin);
        
        if (args.length > 2) {
            Set<String> teamIds = new HashSet<>(Arrays.asList(args).subList(2, args.length));
            return gameManager.startGame(teamIds, Collections.emptyList(), gameType, configFile);
        } else {
            return gameManager.startGame(gameType, configFile);
        }
        
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return GameType.GAME_IDS.keySet().stream().sorted().toList();
        }
        if (args.length == 2) {
            String gameID = args[0];
            return CommandUtils.partialMatchTabList(
                    CommandUtils.getGameConfigs(gameID),
                    args[1]
            );
        }
        if (args.length > 2) {
            return CommandUtils.partialMatchTabList(gameManager.getTeamIds(), args[args.length - 1]);
        }
        return Collections.emptyList();
    }
}
