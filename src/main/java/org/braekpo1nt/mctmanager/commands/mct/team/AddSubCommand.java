package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AddSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public AddSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 3) {
            return CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"").of("<color>"));
        }
        
        int[] displayNameIndexes = getDisplayNameIndexes(args);
        int displayNameStart = displayNameIndexes[0];
        int displayNameEnd = displayNameIndexes[1];
        
        if (displayNameStart > 1) {
            return CommandResult.failure(Component.text("Provide a team name"))
                    .and(CommandResult.failure(Component.text("/mct team add ")
                        .append(Component.text("<team>")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" \"<displayName>\" <color>"))));
        }
        String teamName = args[0];
        if (teamName.equals(GameManager.ADMIN_TEAM)) {
            return CommandResult.failure(Component.empty()
                            .append(Component.text(teamName)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" cannot be "))
                            .append(Component.text(GameManager.ADMIN_TEAM)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" because that is reserved for the admin team.")));
        }
        if (!GameManagerUtils.validTeamName(teamName)) {
            return CommandResult.failure(Component.text("Provide a valid team name\n")
                            .append(Component.text(
                                    "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9")));
        }
        
        if (displayNameIndexesAreInvalid(displayNameStart, displayNameEnd)) {
            return CommandResult.failure("Display name must be surrounded by quotation marks")
                    .and(CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"", TextDecoration.BOLD).of("<color>")));
        }
        String teamDisplayName = getDisplayName(args, displayNameStart, displayNameEnd);
        if (teamDisplayName.isEmpty()) {
            return CommandResult.failure("Display name can't be blank")
                    .and(CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"", TextDecoration.BOLD).of("<color>")));
        }
        
        if (args.length < displayNameEnd + 2) {
            return CommandResult.failure("Please provide a color")
                    .and(CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"").of("<color>", TextDecoration.BOLD)));
        }
        String colorString = args[displayNameEnd + 1];
        
        if (!ColorMap.hasNamedTextColor(colorString)) {
            return CommandResult.failure(Component.empty()
                            .append(Component.text(colorString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized color")));
        }
        boolean teamExists = !gameManager.addTeam(teamName, teamDisplayName, colorString);
        if (teamExists) {
            return CommandResult.failure(Component.text("A team already exists with the teamId \"")
                    .append(Component.text(teamName))
                    .append(Component.text("\"")));
        }
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(teamName);
        return CommandResult.success(Component.text("Created team ")
                .append(formattedTeamDisplayName)
                .append(Component.text(" (teamId=\""))
                .append(Component.text(teamName))
                .append(Component.text("\")")));
    }
    
    private static boolean displayNameIndexesAreInvalid(int displayNameStart, int displayNameEnd) {
        return displayNameStart == -1 || displayNameEnd == -1 || displayNameStart > displayNameEnd;
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
            if (!displayNameIndexesAreInvalid(displayNameStart, displayNameEnd)) {
                if (args.length == displayNameEnd + 2) {
                    String colorString = args[displayNameEnd + 1];
                    return ColorMap.getPartiallyMatchingColorStrings(colorString);
                }
            }
        }
        return Collections.emptyList();
    }
}
