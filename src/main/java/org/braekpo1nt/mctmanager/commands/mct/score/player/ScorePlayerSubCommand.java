package org.braekpo1nt.mctmanager.commands.mct.score.player;

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
            OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(participant.getUniqueId());
            if (offlineParticipant == null) {
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
        // TODO: replace this with a retrieval of participant by name
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(offlinePlayer.getUniqueId());
        if (offlineParticipant == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(playerName))
                    .append(Component.text(" is not a participant")));
        }
        int score = gameManager.getScore(offlineParticipant.getUniqueId());
        return CommandResult.success(Component.empty()
                .append(offlineParticipant.displayName())
                .append(Component.text(": "))
                .append(Component.text(score)
                        .color(NamedTextColor.GOLD)));
    }
    
    /**
     * @return a component with all players and their scores in order from highest to lowest, ties broken alphabetically.
     */
    private Component getAllPlayersScores() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Player Scores (Un-multiplied):")
                        .decorate(TextDecoration.BOLD));
        List<OfflineParticipant> sortedOfflinePlayers = GameManagerUtils.getSortedOfflineParticipants(gameManager);
        for (OfflineParticipant participant : sortedOfflinePlayers) {
            int score = gameManager.getScore(participant.getUniqueId());
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(participant.displayName())
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
        result.addAll(gameManager.getAllParticipantNames());
        return CommandUtils.partialMatchTabList(result, args[0]);
    }
}
