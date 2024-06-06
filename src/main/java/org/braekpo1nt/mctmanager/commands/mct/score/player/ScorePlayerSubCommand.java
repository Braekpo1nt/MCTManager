package org.braekpo1nt.mctmanager.commands.mct.score.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.Collections;
import java.util.List;

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
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return gameManager.getAllParticipantNames();
    }
}
