package org.braekpo1nt.mctmanager.commands.mct.team.score.set;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ScoreSetTeamSubCommand implements TabExecutor {
    private final GameManager gameManager;
    
    public ScoreSetTeamSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mct team score set team <teamName> <score>"));
            return true;
        }
        String teamName = args[0];
        if (!gameManager.hasTeam(teamName)) {
            sender.sendMessage(Component.text(teamName)
                    .append(Component.text(" is not a team")));
            return true;
        }
        String scoreString = args[1];
        if (!CommandUtils.isInteger(scoreString)) {
            sender.sendMessage(Component.text(scoreString)
                    .append(Component.text(" is not an integer")));
            return true;
        }
        int score = Integer.parseInt(scoreString);
        if (score < 0) {
            sender.sendMessage(Component.text("Value must be positive"));
            return true;
        }
        gameManager.setScore(teamName, score);
        int newScore = gameManager.getScore(teamName);
        sender.sendMessage(Component.empty()
                .append(gameManager.getFormattedTeamDisplayName(teamName))
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getTeamNames().stream().toList();
        }
        return Collections.emptyList();
    }
    
}
