package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VoteSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public VoteSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull String getName() {
        return "vote";
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return getUsage().with("<duration>").with("<one or more games...>");
        }
        String durationString = args[0];
        if (!CommandUtils.isInteger(durationString)) {
            return CommandResult.failure(Component.text("Duration ")
                    .append(Component.text(durationString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int duration = Integer.parseInt(durationString);
        if (duration <= 0) {
            return CommandResult.failure(Component.text("Duration must be greater than 0"));
        }
        List<GameType> votingPool = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String gameID = args[i];
            GameType gameType = GameType.fromID(gameID);
            if (gameType == null) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(gameID)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a recognized game name.")));
            }
            votingPool.add(gameType);
        }
        gameManager.manuallyStartVote(sender, votingPool, duration);
        return CommandResult.success();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return Collections.emptyList();
        }
        return getGamesNotInArgs(args).stream().toList();
    }

    private List<String> getGamesNotInArgs(String[] args) {
        List<String> gamesNotInArgs = new ArrayList<>();
        
        for (String game : GameType.GAME_IDS.keySet()) {
            if (!argsContains(args, game)) {
                gamesNotInArgs.add(game);
            }
        }
        
        return gamesNotInArgs;
    }
    
    private static boolean argsContains(String[] arr, String str) {
        for (String s : arr) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }
}
