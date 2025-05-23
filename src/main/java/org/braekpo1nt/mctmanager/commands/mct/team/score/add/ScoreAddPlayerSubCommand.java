package org.braekpo1nt.mctmanager.commands.mct.team.score.add;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ScoreAddPlayerSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    private final boolean invert;

    public ScoreAddPlayerSubCommand(GameManager gameManager, boolean invert, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.invert = invert;
    }


    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<playerName>").of("<score>"));
        }
        String playerName = args[0];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(offlinePlayer.getUniqueId());
        if (offlineParticipant == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(playerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a participant")));
        }
        String scoreString = args[1];
        if (!CommandUtils.isInteger(scoreString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(scoreString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int score = Integer.parseInt(scoreString);
        int currentScore = offlineParticipant.getScore();
        if (invert) {
            score = -score;
        }
        if (currentScore + score < 0) {
            score = -currentScore;
        }
        int newScore = gameManager.addScore(offlineParticipant, score);
        return CommandResult.success(Component.empty()
                .append(Component.text(playerName))
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchTabList(gameManager.getAllParticipantNames(), args[0]);
        }
        return Collections.emptyList();
    }
}
