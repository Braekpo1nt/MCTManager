package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MockSidebarFactory extends SidebarFactory {
    @Override
    public synchronized Sidebar createSidebar() {
        return new MockSidebar();
    }
}
