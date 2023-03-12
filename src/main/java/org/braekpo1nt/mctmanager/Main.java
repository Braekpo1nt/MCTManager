package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.commands.MCTCommand;
import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.braekpo1nt.mctmanager.commands.MCTMVTestCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.HubBoundaryListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        GameManager gameManager = new GameManager(this);
        
        // Commands
        new MCTDebugCommand(this);
        new MCTCommand(this, gameManager);
        new MCTMVTestCommand(this);
        
        // Listeners
        new HubBoundaryListener(this);
    }
}
