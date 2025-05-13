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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * lets you leave players from their team by ign
 */
public class PresetLeaveSubCommand extends TabSubCommand {
    
    
    private final PresetStorageUtil storageUtil;
    
    public PresetLeaveSubCommand(PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<presetFile.json>").of("<member>"));
        }
        String presetFile = args[0];
        String ign = args[1];
        return leavePlayer(presetFile, ign);
    }
    
    private @NotNull CommandResult leavePlayer(@NotNull String presetFile, @NotNull String ign) {
        if (ign.isEmpty()) {
            return CommandResult.failure("Player name can't be blank");
        }
        
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
        if (!preset.hasMember(ign)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not in the preset"))
            );
        }
        preset.leaveMember(ign);
        try {
            storageUtil.savePreset(presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not save preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred saving preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        return CommandResult.success(Component.empty()
                .append(Component.text("Left "))
                .append(Component.text(ign)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" from the preset"))
        );
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return storageUtil.getPreset().getMembers();
    }
}
