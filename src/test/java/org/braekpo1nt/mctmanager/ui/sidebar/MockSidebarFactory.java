package org.braekpo1nt.mctmanager.ui.sidebar;

public class MockSidebarFactory extends SidebarFactory {
    @Override
    public synchronized Sidebar createSidebar() {
        return new MockSidebar();
    }
}
