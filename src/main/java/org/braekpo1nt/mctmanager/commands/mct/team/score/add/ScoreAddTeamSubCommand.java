package org.braekpo1nt.mctmanager.commands.mct.team.score.add;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ScoreAddTeamSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    private final boolean invert;

    public ScoreAddTeamSubCommand(GameManager gameManager, boolean invert, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.invert = invert;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("<teamId>").of("<score>"));
        }
        String teamName = args[0];
        if (!gameManager.hasTeam(teamName)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(teamName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a team")));
        }
        String scoreString = args[1];
        if (!CommandUtils.isInteger(scoreString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(scoreString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int score = Integer.parseInt(scoreString);
        if (invert) {
            score = -score;
            int currentScore = gameManager.getScore(teamName);
            if (currentScore + score < 0) {
                score = -currentScore;
            }
        }
        gameManager.addScore(teamName, score);
        int newScore = gameManager.getScore(teamName);
        return CommandResult.success(Component.empty()
                .append(gameManager.getFormattedTeamDisplayName(teamName))
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getTeamNames().stream().toList();
        }
        return Collections.emptyList();
    }
}
