package org.braekpo1nt.mctmanager.display;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface Display {
    
    void show(@NotNull World world);
    
    void hide();
}
