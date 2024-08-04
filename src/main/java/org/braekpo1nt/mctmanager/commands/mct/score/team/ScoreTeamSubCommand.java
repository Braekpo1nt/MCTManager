package org.braekpo1nt.mctmanager.commands.mct.score.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreTeamSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public ScoreTeamSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[<teamId>]"));
        }
        String team;
        if (args.length == 0) {
            if (!(sender instanceof Player participant)) {
                return CommandResult.failure(getUsage().of("<teamId>"));
            }
            if (!gameManager.isParticipant(participant.getUniqueId())) {
                return CommandResult.failure(Component.text("You are not a participant"));
            }
            team = gameManager.getTeamName(participant.getUniqueId());
        } else {
            team = args[0];
            if (team.equals("all")) {
                return CommandResult.success(getAllTeamScores());
            }
            if (!gameManager.hasTeam(team)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(team)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid team ID")));
            }
        }
        
        Component displayName = gameManager.getFormattedTeamDisplayName(team);
        int score = gameManager.getScore(team);
        return CommandResult.success(Component.empty()
                .append(displayName)
                .append(Component.text(": "))
                .append(Component.text(score)
                        .color(NamedTextColor.GOLD)));
    }
    
    /**
     * @return a component with all teams and their scores in order from highest to lowest, ties broken alphabetically.
     */
    private Component getAllTeamScores() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Team Scores:")
                        .decorate(TextDecoration.BOLD));
        List<String> sortedTeams = GameManagerUtils.getSortedTeams(gameManager);
        for (String team : sortedTeams) {
            Component displayName = gameManager.getFormattedTeamDisplayName(team);
            int score = gameManager.getScore(team);
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(displayName)
                    .append(Component.text(": "))
                    .append(Component.text(score)
                            .color(NamedTextColor.GOLD)));
        }
        return builder.build();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(Collections.singletonList("all"));
        result.addAll(gameManager.getTeamNames());
        return CommandUtils.partialMatchTabList(result, args[0]);
    }
}
