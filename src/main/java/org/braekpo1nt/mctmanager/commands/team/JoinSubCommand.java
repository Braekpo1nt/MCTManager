package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JoinSubCommand implements TabExecutor {
    private final GameManager gameManager;
    
    public JoinSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /mct team join <team> <member>");
            return true;
        }
        String teamName = args[0];
        if (!gameManager.hasTeam(teamName)) {
            sender.sendMessage(String.format("Team \"%s\" does not exist.", teamName));
            return true;
        }
        String playerName = args[1];
        Player playerToJoin = Bukkit.getPlayer(playerName);
        if (playerToJoin == null) {
            sender.sendMessage(String.format("Player \"%s\" is not online.", playerName));
            return true;
        }
        gameManager.joinPlayerToTeam(playerToJoin, teamName);
        sender.sendMessage(Component.text("Joined ")
                .append(Component.text(playerName))
                .append(Component.text("to team "))
                .append(Component.text(teamName)));
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getTeamNames().stream().sorted().toList();
        }
        if (args.length == 2) {
            return null;
        }
        return Collections.emptyList();
    }
}
