package org.braekpo1nt.mctmanager.display;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Something visible, rendered in the world at a location
 */
public interface Renderer {
    
    @NotNull
    Location getLocation();
    
    void show();
    
    boolean showing();
    
    void hide();
}
