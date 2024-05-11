package org.braekpo1nt.mctmanager.commands.mct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ScoreSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public ScoreSubCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[<player>]"));
        }
        Player participant;
        String playerName;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                return CommandResult.failure(getUsage().of("[<player>]"));
            }
            participant = player;
            playerName = participant.getName();
        } else {
            playerName = args[0];
            participant = Bukkit.getPlayer(playerName);
        }
        
        if (participant == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Can't find player with name "))
                    .append(Component.text(playerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(".")));
        }
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Player "))
                    .append(Component.text(playerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a participant and does not have a score.")));
        }
        sender.sendMessage(getScoreDisplay(participant));
        return CommandResult.success();
    }
    
    /**
     * 
     * @param participant the participant to get the score display for
     * @return a component with the first line being the participant's team score, and the second being their personal score
     */
    private Component getScoreDisplay(Player participant) {
        String team = gameManager.getTeamName(participant.getUniqueId());
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(team);
        NamedTextColor teamNamedTextColor = gameManager.getTeamNamedTextColor(team);
        Component participantName = Component.text(participant.getName())
                .color(teamNamedTextColor);
        Component teamScore = Component.text(gameManager.getScore(team))
                .color(NamedTextColor.GOLD);
        Component participantScore = Component.text(gameManager.getScore(participant.getUniqueId()))
                .color(NamedTextColor.GOLD);
        
    
        return Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(": "))
                .append(teamScore)
                .append(Component.newline())
                .append(participantName)
                .append(Component.text(": "))
                .append(participantScore)
        ;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return gameManager.getAllParticipantNames();
    }
}
