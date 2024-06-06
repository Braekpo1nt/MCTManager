package org.braekpo1nt.mctmanager.commands.mct.score.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScorePlayerSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public ScorePlayerSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player participant)) {
                return CommandResult.failure(getUsage().of("<player>"));
            }
            if (!gameManager.isParticipant(participant.getUniqueId())) {
                return CommandResult.failure(Component.text("You are not a participant"));
            }
            int score = gameManager.getScore(participant.getUniqueId());
            return CommandResult.success(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(Component.text(score)
                            .color(NamedTextColor.GOLD)));
        }
        
        if (args.length != 1) {
            return CommandResult.failure(getUsage().of("[<player>]"));
        }
        
        String playerName = args[0];
        if (playerName.equals("all")) {
            return CommandResult.success(getAllPlayersScores());
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (!gameManager.isParticipant(offlinePlayer.getUniqueId())) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(playerName))
                    .append(Component.text(" is not a participant")));
        }
        int score = gameManager.getScore(offlinePlayer.getUniqueId());
        Player player = offlinePlayer.getPlayer();
        Component displayName;
        if (player != null) {
            displayName = player.displayName();
        } else {
            String team = gameManager.getTeamName(offlinePlayer.getUniqueId());
            NamedTextColor teamColor = gameManager.getTeamNamedTextColor(team);
            displayName = Component.text(playerName).color(teamColor);
        }
        return CommandResult.success(Component.empty()
                .append(displayName)
                .append(Component.text(": "))
                .append(Component.text(score)
                        .color(NamedTextColor.GOLD)));
    }
    
    /**
     * @return a component with all players and their scores in order from highest to lowest, ties broken alphabetically.
     */
    private Component getAllPlayersScores() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Scores:")
                        .decorate(TextDecoration.BOLD));
        List<OfflinePlayer> sortedOfflinePlayers = getSortedOfflinePlayers();
        for (OfflinePlayer participant : sortedOfflinePlayers) {
            Component displayName = gameManager.getDisplayName(participant);
            int score = gameManager.getScore(participant.getUniqueId());
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(displayName)
                    .append(Component.text(": "))
                    .append(Component.text(score)
                            .color(NamedTextColor.GOLD)));
        }
        return builder.build();
    }
    
    /**
     * @return a sorted list of OfflinePlayers representing the participants. Sorted first by score from greatest to least, then alphabetically (A first, Z last).
     */
    private @NotNull List<OfflinePlayer> getSortedOfflinePlayers() {
        List<OfflinePlayer> offlinePlayers = gameManager.getOfflineParticipants();
        offlinePlayers.sort((p1, p2) -> {
            int scoreComparison = gameManager.getScore(p2.getUniqueId()) - gameManager.getScore(p1.getUniqueId());
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            
            String p1Name = p1.getName();
            if (p1Name == null) {
                p1Name = p1.getUniqueId().toString();
            }
            String p2Name = p2.getName();
            if (p2Name == null) {
                p2Name = p2.getUniqueId().toString();
            }
            return p1Name.compareToIgnoreCase(p2Name);
        });
        return offlinePlayers;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return gameManager.getAllParticipantNames();
    }
}
