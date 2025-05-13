package org.braekpo1nt.mctmanager.commands.mct.team.preset.editor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

/**
 * lets you remove a team from the preset
 */
public class PresetRemoveSubCommand extends TabSubCommand {
    
    
    private final PresetStorageUtil storageUtil;
    
    public PresetRemoveSubCommand(PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<presetFile.json>").of("<team>"));
        }
        String presetFile = args[0];
        String removeTeamId = args[1];
        return removeTeam(presetFile, removeTeamId);
    }
    
    private @NotNull CommandResult removeTeam(@NotNull String presetFile, @NotNull String teamId) {
        Preset preset;
        try {
            storageUtil.loadPreset(presetFile);
            preset = storageUtil.getPreset();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        
        if (!preset.hasTeamId(teamId)) {
            return CommandResult.failure(Component.text("Team ")
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" does not exist in the preset")));
        }
        
        preset.removeTeam(teamId);
        try {
            storageUtil.savePreset(presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not save preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred saving preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        return CommandResult.success(Component.empty()
                .append(Component.text("Removed "))
                .append(Component.text(teamId)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" from preset"))
        );
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return storageUtil.getPreset().getTeamIds();
    }
}
