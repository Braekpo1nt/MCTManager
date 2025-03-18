package org.braekpo1nt.mctmanager.commands.mct.team.score.award;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreAwardPlayersSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public ScoreAwardPlayersSubCommand(GameManager gameManager) {
        super("players");
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("[<playerName>]").of("<score>"));
        }
        
        // loop through all the names
        Set<Player> participants = new HashSet<>(args.length - 1);
        for (int i = 0; i < args.length - 1; i++) {
            String playerName = args[i];
            Player participant = Bukkit.getPlayer(playerName);
            if (participant == null) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName))
                        .append(Component.text(" is not online.")));
            }
            if (!gameManager.isParticipant(participant.getUniqueId())) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a participant")));
            }
            if (participants.contains(participant)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName))
                        .append(Component.text(" is listed more than once")));
            }
            participants.add(participant);
        }
        
        // the last arg should be the score
        String scoreString = args[args.length - 1];
        if (!CommandUtils.isInteger(scoreString)) {
            return CommandResult.failure(getUsage().of("[<playerName>]").of("<score>"));
        }
        int score = Integer.parseInt(scoreString);
        if (score <= 0) {
            return CommandResult.failure(Component.text("Score value must be positive"));
        }
        if (participants.size() == 1) {
            gameManager.awardPointsToParticipant(participants.stream().findFirst().get(), score);
        } else {
            gameManager.awardPointsToParticipants(participants, score);
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
        return null;
    }
}
