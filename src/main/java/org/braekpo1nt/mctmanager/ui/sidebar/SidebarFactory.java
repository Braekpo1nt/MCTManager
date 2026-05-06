package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class SidebarFactory {
    
    // be careful when creating new methods, make sure they're overridden properly by MockSidebarFactory
    
    public synchronized Sidebar createSidebar() {
        return new Sidebar();
    }
    
    public synchronized Sidebar createSidebar(@NotNull Component title) {
        return new Sidebar(title);
    }
    
}
