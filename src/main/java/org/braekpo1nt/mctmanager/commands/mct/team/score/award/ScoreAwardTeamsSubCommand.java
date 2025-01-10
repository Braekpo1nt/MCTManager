package org.braekpo1nt.mctmanager.commands.mct.team.score.award;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreAwardTeamsSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public ScoreAwardTeamsSubCommand(GameManager gameManager) {
        super("teams");
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("[<teamId>]").of("<score>"));
        }
        
        Set<String> teamIds = new HashSet<>(args.length - 1);
        for (int i = 0; i < args.length - 1; i++) {
            String teamId = args[i];
            if (!gameManager.hasTeam(teamId)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a team")));
            }
            if (teamIds.contains(teamId)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(teamId))
                        .append(Component.text(" is listed more than once")));
            }
            teamIds.add(teamId);
        }
        
        // the last arg should be the score
        String scoreString = args[args.length - 1];
        if (!CommandUtils.isInteger(scoreString)) {
            return CommandResult.failure(getUsage().of("[<teamId>]").of("<score>"));
        }
        int score = Integer.parseInt(scoreString);
        if (score <= 0) {
            return CommandResult.failure(Component.text("Score value must be positive"));
        }
        if (teamIds.size() == 1) {
            gameManager.awardPointsToTeam(teamIds.stream().findFirst().get(), score);
        } else {
            gameManager.awardPointsToTeams(teamIds, score);
        }
        return CommandResult.success(Component.empty()
                .append(Component.text(score))
                .append(Component.text(" points awarded")));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }
        return CommandUtils.partialMatchTabList(
                gameManager.getTeamIds().stream().toList(), 
                args[args.length - 1]);
    }
}
