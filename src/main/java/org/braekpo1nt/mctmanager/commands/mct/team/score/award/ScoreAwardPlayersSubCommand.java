package org.braekpo1nt.mctmanager.commands.mct.team.score.award;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScoreAwardPlayersSubCommand extends TabSubCommand {
    private final Main plugin;
    private final GameManager gameManager;
    
    public ScoreAwardPlayersSubCommand(Main plugin, GameManager gameManager) {
        super("players");
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("[<playerName>]").of("<score>"));
        }
        
        // loop through all the names
        Map<UUID, Participant> participants = new HashMap<>(args.length - 1);
        for (int i = 0; i < args.length - 1; i++) {
            String playerName = args[i];
            Player player = plugin.getServer().getPlayer(playerName);
            if (player == null) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName))
                        .append(Component.text(" is not online.")));
            }
            Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
            if (participant == null) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a participant")));
            }
            if (participants.containsKey(player.getUniqueId())) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName))
                        .append(Component.text(" is listed more than once")));
            }
            participants.put(player.getUniqueId(), participant);
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
            gameManager.awardPointsToParticipant(participants.values().stream().findFirst().get(), score);
        } else {
            gameManager.awardPointsToParticipants(participants.values(), score);
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
