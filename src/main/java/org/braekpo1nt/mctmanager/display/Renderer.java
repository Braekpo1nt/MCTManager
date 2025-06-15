package org.braekpo1nt.mctmanager.display;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Renderer {
    
    @NotNull
    Location getLocation();
    
    void show();
    
    void hide();
}
