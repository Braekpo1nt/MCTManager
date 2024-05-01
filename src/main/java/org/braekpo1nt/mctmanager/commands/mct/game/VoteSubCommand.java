package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VoteSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public VoteSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mct game vote <duration> <one or more games...>")
                    .color(NamedTextColor.RED));
            return true;
        }
        String durationString = args[0];
        if (!CommandUtils.isInteger(durationString)) {
            sender.sendMessage(Component.text("Duration ")
                    .append(Component.text(durationString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer"))
                    .color(NamedTextColor.RED));
            return true;
        }
        int duration = Integer.parseInt(durationString);
        if (duration <= 0) {
            sender.sendMessage(Component.text("Duration must be greater than 0")
                    .color(NamedTextColor.RED));
            return true;
        }
        List<GameType> votingPool = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String gameID = args[i];
            GameType gameType = GameType.fromID(gameID);
            if (gameType == null) {
                sender.sendMessage(Component.empty()
                        .append(Component.text(gameID)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a recognized game name."))
                        .color(NamedTextColor.RED));
                return true;
            }
            votingPool.add(gameType);
        }
        gameManager.manuallyStartVote(sender, votingPool, duration);
        return true;
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
