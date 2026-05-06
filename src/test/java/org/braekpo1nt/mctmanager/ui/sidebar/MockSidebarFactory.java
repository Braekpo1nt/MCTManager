package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class MockSidebarFactory extends SidebarFactory {
    @Override
    public synchronized Sidebar createSidebar() {
        return new MockSidebar();
    }
    
    @Override
    public synchronized Sidebar createSidebar(@NotNull Component title) {
        return new MockSidebar(title);
    }
}
