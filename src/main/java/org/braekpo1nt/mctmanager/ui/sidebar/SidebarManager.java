package org.braekpo1nt.mctmanager.ui.sidebar;


import java.util.ArrayList;
import java.util.List;

public class SidebarManager {

    List<Sidebar> sidebars = new ArrayList<>();
    
    public synchronized Sidebar createSidebar() {
        Sidebar newSidebar = new Sidebar();
        sidebars.add(newSidebar);
        return newSidebar;
    }
    
    public synchronized boolean deleteSidebar(Sidebar delete) {
        return sidebars.remove(delete);
    }
    
}
