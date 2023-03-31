package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.color.ColorMap;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
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
            sender.sendMessage(
                    Component.text("Usage: /mct team add <team> <\"display name\"> <color>")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        int[] displayNameIndexes = getDisplayNameIndexes(args);
        int displayNameStart = displayNameIndexes[0];
        int displayNameEnd = displayNameIndexes[1];
        
        if (displayNameStart > 1) {
            sender.sendMessage(Component.text("Provide a team name")
                    .color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/mct team add ")
                    .append(Component.text("<team>")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" <\"display name\"> <color>"))
                    .color(NamedTextColor.RED));
            return true;
        }
        String teamName = args[0];
        if (!validTeamName(teamName)) {
            sender.sendMessage(Component.text("Provide a valid team name\n")
                            .append(Component.text(
                                    "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9"))
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (!displayNameIndexesAreValid(displayNameStart, displayNameEnd)) {
            sender.sendMessage(Component.text("Display name must be quoted")
                    .color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/mct team add <team> ")
                    .append(Component.text("<\"display name\">")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" <color>"))
                    .color(NamedTextColor.RED));
            return true;
        }
        String teamDisplayName = getDisplayName(args, displayNameStart, displayNameEnd);
        if (teamDisplayName.isEmpty()) {
            sender.sendMessage(Component.text("Display name cannot be blank")
                    .color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/mct team add <team> ")
                    .append(Component.text("<\"display name\">")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" <color>"))
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < displayNameEnd + 2) {
            sender.sendMessage(Component.text("Please provide a color")
                    .color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /mct team add <team> <\"display name\"> ")
                    .append(Component.text("<color>")
                            .decorate(TextDecoration.BOLD))
                    .color(NamedTextColor.RED));
            return true;
        }
        String colorString = args[displayNameEnd + 1];
        
        if (!ColorMap.hasNamedTextColor(colorString)) {
            sender.sendMessage(Component.text()
                            .append(Component.text(colorString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized color"))
                    .color(NamedTextColor.RED));
            return true;
        }
        try {
            boolean teamExists = !gameManager.addTeam(teamName, teamDisplayName, colorString);
            if (teamExists) {
                sender.sendMessage(Component.text("A team already exists with the team name \"")
                        .append(Component.text(teamName))
                        .append(Component.text("\""))
                        .color(NamedTextColor.RED));
                return true;
            }
            sender.sendMessage(String.format("Created team \"%s\" with display name \"%s\"", teamName, teamDisplayName));
            sender.sendMessage(Component.text("Created team ")
                    .append(Component.text(teamName))
                    .append(Component.text("\" with display name \""))
                    .append(Component.text(teamDisplayName))
                    .append(Component.text("\""))
                    .color(NamedTextColor.GREEN));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Error creating team. See log for error message.").color(NamedTextColor.RED));
            Bukkit.getLogger().severe("Error saving game state while creating new team.");
            throw new RuntimeException(e);
        }
        return true;
    }
    
    private static boolean displayNameIndexesAreValid(int displayNameStart, int displayNameEnd) {
        return displayNameStart != -1 || displayNameEnd != -1 || displayNameStart <= displayNameEnd;
    }
    
    private boolean validTeamName(String teamName) {
        String regexPattern = "[-+\\._A-Za-z0-9]+";
        return teamName.matches(regexPattern);
    }
    
    @NotNull
    private static String getDisplayName(@NotNull String @NotNull [] args, int displayNameStart, int displayNameEnd) {
        StringBuilder displayNameBuilder = new StringBuilder();
        for (int i = displayNameStart; i <= displayNameEnd; i++) {
            displayNameBuilder.append(args[i]);
            displayNameBuilder.append(" ");
        }
        String messageWithQuotes = displayNameBuilder.toString().trim();
        String displayName = messageWithQuotes.substring(1, messageWithQuotes.length() - 1).trim();
        return displayName;
    }
    
    @NotNull
    private static int[] getDisplayNameIndexes(@NotNull String @NotNull [] args) {
        int startIndex = -1;
        int endIndex = -1;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("\"") && startIndex == -1) {
                startIndex = i;
            }
    
            if (args[i].endsWith("\"") && endIndex == -1) {
                if (i == startIndex) {
                    if (args[i].length() == 1) {
                        // means args[i] == "\""
                        continue;
                    }
                }
                endIndex = i;
                break;
            }
        }
        
        return new int[]{startIndex, endIndex};
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 3) {
            int[] displayNameIndexes = getDisplayNameIndexes(args);
            int displayNameStart = displayNameIndexes[0];
            int displayNameEnd = displayNameIndexes[1];
            if (displayNameIndexesAreValid(displayNameStart, displayNameEnd)) {
                if (args.length == displayNameEnd + 2) {
                    String colorString = args[displayNameEnd + 1];
                    return ColorMap.getPartiallyMatchingColorStrings(colorString);
                }
            }
        }
        return Collections.emptyList();
    }
}
