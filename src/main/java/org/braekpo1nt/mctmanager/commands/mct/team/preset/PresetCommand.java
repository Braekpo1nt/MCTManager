package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class PresetCommand extends CommandManager {
    
    public PresetCommand(GameManager gameManager, @NotNull String name) {
        super(name);
    }
}
