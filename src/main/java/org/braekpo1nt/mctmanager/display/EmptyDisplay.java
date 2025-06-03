package org.braekpo1nt.mctmanager.display;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Used to hold other displays
 */
public class EmptyDisplay implements Display {
    
    public EmptyDisplay() {
        // TODO: implement this
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    public void addChild(@NotNull Display child) {
        
    }
    
    @Override
    public void show(@NotNull World world) {
        
    }
    
    @Override
    public void hide() {
        
    }
}
