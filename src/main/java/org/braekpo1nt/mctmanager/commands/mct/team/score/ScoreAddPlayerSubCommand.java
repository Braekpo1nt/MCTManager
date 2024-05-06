package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
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
        
        if (!gameManager.isParticipant(offlinePlayer.getUniqueId())) {
            return CommandResult.failure(Component.text(playerName)
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
        if (invert) {
            score = -score;
            int currentScore = gameManager.getScore(offlinePlayer.getUniqueId());
            if (currentScore + score < 0) {
                score = -currentScore;
            }
        }
        gameManager.addScore(offlinePlayer.getUniqueId(), score);
        int newScore = gameManager.getScore(offlinePlayer.getUniqueId());
        return CommandResult.success(Component.empty()
                .append(Component.text(playerName))
                .append(Component.text("'s score is now "))
                .append(Component.text(newScore)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getAllParticipantNames();
        }
        return Collections.emptyList();
    }
}
