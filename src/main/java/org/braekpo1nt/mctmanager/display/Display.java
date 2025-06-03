package org.braekpo1nt.mctmanager.display;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface Display {
    
    void addChild(@NotNull Display child);
    
    void show(@NotNull World world);
    
    void hide();
}
