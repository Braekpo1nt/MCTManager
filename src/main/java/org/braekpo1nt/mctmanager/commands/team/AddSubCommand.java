package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.color.ColorMap;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AddSubCommand implements TabExecutor {
    private final GameManager gameManager;
    
    public AddSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /mct team add <team> <displayName> <color>");
            return true;
        }
        String newTeamName = args[0];
        String teamDisplayName = args[1];
        String colorString = args[2];
        if (!ColorMap.hasColor(colorString)) {
            sender.sendMessage(String.format("\"%s\" is not a recognized color.", colorString));
            return true;
        }
        try {
            boolean teamExists = !gameManager.addTeam(newTeamName, teamDisplayName, colorString);
            if (teamExists) {
                sender.sendMessage(Component.text(String.format("A team already exists with teamName \"%s\"", newTeamName)));
                return true;
            }
            sender.sendMessage(String.format("Created team \"%s\" with display name \"%s\"", newTeamName, teamDisplayName));
        } catch (IOException e) {
            sender.sendMessage("Error creating team. See log for error message.");
            Bukkit.getLogger().severe("Error saving game state while creating new team.");
            throw new RuntimeException(e);
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 3) {
            String colorString = args[2];
            return ColorMap.getPartiallyMatchingColorStrings(colorString);
        }
        return Collections.emptyList();
    }
}
