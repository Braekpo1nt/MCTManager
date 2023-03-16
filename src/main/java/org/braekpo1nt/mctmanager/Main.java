package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.commands.MCTCommand;
import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.braekpo1nt.mctmanager.commands.MCTMVTestCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.listeners.HubBoundaryListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        GameManager gameManager = new GameManager(this);

        // Listeners
        HubBoundaryListener hubBoundaryListener = new HubBoundaryListener(this);
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        
        // Commands
        new MCTDebugCommand(this);
        new MCTCommand(this, gameManager, hubBoundaryListener, blockEffectsListener);
        new MCTMVTestCommand(this);
        

        File dataFolder = getDataFolder();
    }
}
