package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.color.ColorMap;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class TeamSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public TeamSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct team <options>");
            return true;
        }
        
        String option = args[0];
        switch (option) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage("Usage: /mct team add <team> <displayName> <color>");
                    return true;
                }
                String teamName = args[1];
                String teamDisplayName = args[2];
                String colorString = args[3];
                if (!ColorMap.hasColor(colorString)) {
                    sender.sendMessage(String.format("\"%s\" is not a recognized color.", colorString));
                    return true;
                }
                NamedTextColor color = ColorMap.getColor(colorString);
                try {
                    boolean teamExists = !gameManager.addTeam(teamName, teamDisplayName, color);
                    if (teamExists) {
                        sender.sendMessage(Component.text(String.format("A team already exists with teamName \"%s\"", teamName)));
                        return true;
                    }
                    sender.sendMessage(String.format("Created team \"%s\" with display name \"%s\"", teamName, teamDisplayName));
                } catch (IOException e) {
                    sender.sendMessage("Error creating team. See log for error message.");
                    Bukkit.getLogger().severe("Error saving game state while creating new team.");
                    throw new RuntimeException(e);
                }
                break;
            default:
                sender.sendMessage("Unrecognized option " + args[0]);
                break;
        }
        
        return true;
    }
    
    private static boolean createTestTeam(@NotNull CommandSender sender) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getTeam("testteam") != null) {
            sender.sendMessage("Team already created");
            return true;
        }
        Team testTeam = scoreboard.registerNewTeam("testteam");
        testTeam.displayName(Component.text("Test Team"));
        testTeam.color(NamedTextColor.GREEN);
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
