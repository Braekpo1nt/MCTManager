package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.commands.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCTManager extends JavaPlugin {
    
    @Override
    public void onEnable() {
        new TestCommand(this);
    }
}
