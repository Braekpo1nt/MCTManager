package org.braekpo1nt.mctmanager.commands.mct.team.preset.editor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.team.AddSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Lets you add a team to the preset
 */
public class PresetAddSubCommand extends TabSubCommand {
    
    
    private final PresetStorageUtil storageUtil;
    
    public PresetAddSubCommand(PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 4) {
            return CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"").of("<color>"));
        }
        
        int[] displayNameIndexes = AddSubCommand.getDisplayNameIndexes(args);
        int displayNameStart = displayNameIndexes[0];
        int displayNameEnd = displayNameIndexes[1];
        
        if (displayNameStart > 2) {
            return CommandResult.failure(Component.text("Provide a team name"))
                    .and(CommandResult.failure(Component.text("/mct team add ")
                            .append(Component.text("<team>")
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" \"<displayName>\" <color>"))));
        }
        
        String presetFile = args[0];
        
        String teamId = args[1];
        
        if (AddSubCommand.displayNameIndexesAreInvalid(displayNameStart, displayNameEnd)) {
            return CommandResult.failure("Display name must be surrounded by quotation marks")
                    .and(CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"", TextDecoration.BOLD).of("<color>")));
        }
        String teamDisplayName = AddSubCommand.getDisplayName(args, displayNameStart, displayNameEnd);
        if (teamDisplayName.isEmpty()) {
            return CommandResult.failure("Display name can't be blank")
                    .and(CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"", TextDecoration.BOLD).of("<color>")));
        }
        
        if (args.length < displayNameEnd + 2) {
            return CommandResult.failure("Please provide a color")
                    .and(CommandResult.failure(getUsage().of("<team>").of("\"<displayName>\"").of("<color>", TextDecoration.BOLD)));
        }
        String colorString = args[displayNameEnd + 1];
        
        return addTeam(presetFile, teamId, teamDisplayName, colorString);
    }
    
    private @NotNull CommandResult addTeam(@NotNull String presetFile, @NotNull String teamId, @NotNull String displayName, @NotNull String colorString) {
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (preset.hasTeamId(teamId)) {
                return CommandResult.failure(Component.text("A team already exists with the teamId \"")
                        .append(Component.text(teamId))
                        .append(Component.text("\" in the preset")));
            }
            
            if (teamId.equals(GameManager.ADMIN_TEAM)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" cannot be "))
                        .append(Component.text(GameManager.ADMIN_TEAM)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" because that is reserved for the admin team.")));
            }
            
            if (!GameManagerUtils.validTeamId(teamId)) {
                return CommandResult.failure(Component.text("Provide a valid team name\n")
                        .append(Component.text(
                                "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9")));
            }
            
            if (displayName.isEmpty()) {
                return CommandResult.failure("Display name can't be blank");
            }
            
            if (!ColorMap.hasNamedTextColor(colorString)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(colorString)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a recognized color")));
            }
            
            preset.addTeam(teamId, displayName, colorString);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Added "))
                    .append(Component.text(displayName)
                            .color(ColorMap.getNamedTextColor(colorString)))
                    .append(Component.text(" to the preset")));
        });
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 4) {
            int[] displayNameIndexes = AddSubCommand.getDisplayNameIndexes(args);
            int displayNameStart = displayNameIndexes[0];
            int displayNameEnd = displayNameIndexes[1];
            if (!AddSubCommand.displayNameIndexesAreInvalid(displayNameStart, displayNameEnd)) {
                if (args.length == displayNameEnd + 2) {
                    String colorString = args[displayNameEnd + 1];
                    return ColorMap.getPartiallyMatchingColorStrings(colorString);
                }
            }
        }
        return Collections.emptyList();
    }
    
}
