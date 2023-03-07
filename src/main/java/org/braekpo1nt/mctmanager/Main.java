package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.commands.MCTTestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        new MCTTestCommand(this);
    }
}
