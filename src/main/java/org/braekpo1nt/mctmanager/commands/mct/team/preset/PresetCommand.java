package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetAddSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetJoinSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetLeaveSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.editor.PresetRemoveSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PresetCommand extends CommandManager {
    
    private final PresetController controller;
    
    public PresetCommand(GameManager gameManager, File configDirectory, @NotNull String name) {
        super(name);
        this.controller = new PresetController(configDirectory);
        addSubCommand(new PresetApplySubCommand(gameManager, controller, "apply"));
        addSubCommand(new PresetWhitelistSubCommand(gameManager, controller, "whitelist"));
        
        // editor
        addSubCommand(new PresetAddSubCommand(controller, "add"));
        addSubCommand(new PresetRemoveSubCommand(controller, "remove"));
        addSubCommand(new PresetJoinSubCommand(controller, "join"));
        addSubCommand(new PresetLeaveSubCommand(controller, "leave"));
    }
}
