package org.braekpo1nt.mctmanager.ui.sidebar;

public class SidebarFactory {
    
    public synchronized Sidebar createSidebar() {
        return new Sidebar();
    }
    
}
