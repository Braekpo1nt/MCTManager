package org.braekpo1nt.mctmanager.commands.team.score;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ScoreAddPlayerSubCommand implements TabExecutor {
    private final GameManager gameManager;
    private final boolean invert;

    public ScoreAddPlayerSubCommand(GameManager gameManager, boolean invert) {
        this.gameManager = gameManager;
        this.invert = invert;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mct team score add player <playerName> <score>"));
            return true;
        }
        String playerName = args[0];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!gameManager.isParticipant(offlinePlayer.getUniqueId())) {
            sender.sendMessage(Component.text(playerName)
                    .append(Component.text(" is not a participant"))
                    .color(NamedTextColor.RED));
            return true;
        }
        String scoreString = args[1];
        try {
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
            sender.sendMessage(Component.empty()
                    .append(Component.text(playerName))
                    .append(Component.text(" score is now "))
                    .append(Component.text(newScore)));
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(scoreString)
                    .append(Component.text(" is not an integer")));
            return true;
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getAllParticipantNames();
        }
        return Collections.emptyList();
    }
}
