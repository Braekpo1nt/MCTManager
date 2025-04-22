package org.braekpo1nt.mctmanager.commands.mct.hub;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.jetbrains.annotations.NotNull;

public class HubCommand extends CommandManager {
    
    public HubCommand(@NotNull GameManager gameManager, @NotNull BlockEffectsListener blockEffectsListener, @NotNull String name) {
        super(name);
        addSubCommand(new HubOptionSubCommand(blockEffectsListener, "option"));
    }
    
}
