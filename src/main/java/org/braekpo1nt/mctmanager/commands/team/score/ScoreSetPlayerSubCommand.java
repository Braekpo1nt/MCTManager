package org.braekpo1nt.mctmanager.commands.team.score;

import net.kyori.adventure.text.Component;
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

public class ScoreSetPlayerSubCommand implements TabExecutor {
    private final GameManager gameManager;
    
    public ScoreSetPlayerSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mct team score set player <playerName> <score>"));
            return true;
        }
        String playerName = args[0];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!gameManager.isParticipant(offlinePlayer.getUniqueId())) {
            sender.sendMessage(Component.text(playerName)
                    .append(Component.text(" is not a participant")));
            return true;
        }
        String scoreString = args[1];
        try {
            int score = Integer.parseInt(scoreString);
            if (score < 0) {
                sender.sendMessage(Component.text("Value must be positive"));
                return true;
            }
            gameManager.setScore(offlinePlayer.getUniqueId(), score);
            int newScore = gameManager.getScore(offlinePlayer.getUniqueId());
            sender.sendMessage(Component.empty()
                    .append(Component.text(playerName))
                    .append(Component.text(" score is now "))
                    .append(Component.text(newScore)));
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text(scoreString)
                    .append(Component.text(" is not an integer")));
            return true;
        } catch (IOException e) {
            sender.sendMessage(Component.text("Error occurred saving game state. See console for details."));
            Bukkit.getLogger().severe("Error occurred saving game state.");
            e.printStackTrace();
            return true;
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getOnlineParticipantNames();
        }
        return Collections.emptyList();
    }
}
