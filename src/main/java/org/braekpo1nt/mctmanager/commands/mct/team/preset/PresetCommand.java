package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.experimental.OptCommandManager;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetAddSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetJoinSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetLeaveSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetRemoveSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PresetCommand extends OptCommandManager {
    
    private final Main plugin;
    
    public PresetCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name, "<presetFile.json>");
        this.plugin = plugin;
        CommandUtils.refreshPresetFiles(plugin);
        PresetStorageUtil storageUtil = new PresetStorageUtil(plugin);
        addSubCommand(new PresetApplySubCommand(plugin, gameManager, storageUtil, "apply"));
        addSubCommand(new PresetWhitelistSubCommand(storageUtil, "whitelist"));
        
        // editor
        addSubCommand(new PresetAddSubCommand(storageUtil, "add"));
        addSubCommand(new PresetRemoveSubCommand(storageUtil, "remove"));
        addSubCommand(new PresetJoinSubCommand(storageUtil, "join"));
        addSubCommand(new PresetLeaveSubCommand(storageUtil, "leave"));
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandUtils.refreshPresetFiles(plugin);
        return super.onSubCommand(sender, command, label, args);
    }
    
    @Override
    protected @Nullable List<String> onTabCompleteOpt(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String opt) {
        return CommandUtils.getPresetFiles();
    }
}
