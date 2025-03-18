package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetAddSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetJoinSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetLeaveSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetRemoveSubCommand;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.jetbrains.annotations.NotNull;

public class PresetCommand extends CommandManager {
    
    public PresetCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name);
        PresetStorageUtil storageUtil = new PresetStorageUtil(plugin);
        try {
            storageUtil.loadPreset();
        } catch (ConfigException e) {
            plugin.getLogger().severe(String.format("Could not load preset. %s", e.getMessage()));
            e.printStackTrace();
        }
        addSubCommand(new PresetApplySubCommand(plugin, gameManager, storageUtil, "apply"));
        addSubCommand(new PresetWhitelistSubCommand(gameManager, storageUtil, "whitelist"));
        
        // editor
        addSubCommand(new PresetAddSubCommand(storageUtil, "add"));
        addSubCommand(new PresetRemoveSubCommand(storageUtil, "remove"));
        addSubCommand(new PresetJoinSubCommand(storageUtil, "join"));
        addSubCommand(new PresetLeaveSubCommand(storageUtil, "leave"));
        addSubCommand(new PresetLoadSubCommand(storageUtil, "load"));
    }
}
