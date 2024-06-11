package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
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
    }
}
