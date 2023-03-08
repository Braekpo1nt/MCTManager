package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.commands.MCTMVTestCommand;
import org.braekpo1nt.mctmanager.commands.MCTTestCommand;
import org.braekpo1nt.mctmanager.listeners.HubBoundaryListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Commands
        new MCTTestCommand(this);
        new MCTMVTestCommand(this);
        
        // Listeners
        new HubBoundaryListener(this);
    }
}
