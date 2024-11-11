package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class SidebarFactory {
    
    public synchronized Sidebar createSidebar() {
        return new Sidebar();
    }
    
    public synchronized Sidebar createSidebar(@NotNull Component title) {
        return new Sidebar(title);
    }
    
}
