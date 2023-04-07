package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListSubCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    public ListSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            sender.sendMessage("Usage: /mct team list");
            return true;
        }
        displayTeams(sender);
        return true;
    }
    
    private void displayTeams(CommandSender sender) {
        TextComponent.Builder messageBuilder = Component.text().append(Component.text("TEAMS\n")
                    .decorate(TextDecoration.BOLD));
        List<OfflinePlayer> offlinePlayers = gameManager.getOfflinePlayers();
        List<String> teamNames = gameManager.getTeamNames().stream().toList();
        
        for (String teamName : teamNames) {
            int teamScore = gameManager.getScore(teamName);
            messageBuilder.append(gameManager.getFormattedTeamDisplayName(teamName)
                            .append(Component.text(" - "))
                            .append(Component.text(teamScore))
                    .append(Component.text(":\n")));
            for (OfflinePlayer offlinePlayer : offlinePlayers) {
                String playerTeam = gameManager.getTeamName(offlinePlayer.getUniqueId());
                int score = gameManager.getScore(offlinePlayer.getUniqueId());
                if (playerTeam.equals(teamName)) {
                    messageBuilder.append(Component.empty()
                            .append(Component.text("  "))
                            .append(Component.text(offlinePlayer.getName()))
                            .append(Component.text(" - "))
                            .append(Component.text(score))
                            .append(Component.text("\n")));
                }
            }
        }
        
        Component message = messageBuilder.build();
        sender.sendMessage(message);
    }
}
