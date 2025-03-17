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
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Team;
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
        String teamId;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                return CommandResult.failure(getUsage().of("<teamId>"));
            }
            OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(player.getUniqueId());
            if (offlineParticipant == null) {
                return CommandResult.failure(Component.text("You are not a participant"));
            }
            teamId = offlineParticipant.getTeamId();
        } else {
            teamId = args[0];
            if (teamId.equals("all")) {
                return CommandResult.success(getAllTeamScores());
            }
        }
        Team team = gameManager.getTeam(teamId);
        if (team == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid teamId ID")));
        }
        
        return CommandResult.success(Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD)));
    }
    
    /**
     * @return a component with all teams and their scores in order from highest to lowest, ties broken alphabetically.
     */
    private Component getAllTeamScores() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Team Scores:")
                        .decorate(TextDecoration.BOLD));
        List<Team> sortedTeams = GameManagerUtils.getSortedTeams(gameManager);
        for (Team team : sortedTeams) {
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
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
        result.addAll(gameManager.getTeamIds());
        return CommandUtils.partialMatchTabList(result, args[0]);
    }
}
