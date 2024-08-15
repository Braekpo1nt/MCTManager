package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class MockSidebar extends Sidebar {
    @Override
    public synchronized void addPlayer(@NotNull Player player) {
        if (boardsLines.containsKey(player.getUniqueId())) {
            logUIError("player with UUID \"%s\" already has a board in this manager", player.getUniqueId());
            return;
        }
        FastBoardWrapper newBoard = new MockFastBoardWrapper();
        newBoard.setPlayer(player);
        newBoard.updateTitle(this.title);
        boards.put(player.getUniqueId(), newBoard);
        List<Component> lines = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lines.add(Component.empty());
        }
        boardsLines.put(player.getUniqueId(), lines);
    }
    
    public void assertLine(UUID playerUniqueId, String key, String expectedText) {
        MockFastBoardWrapper board = (MockFastBoardWrapper) boards.get(playerUniqueId);
        Assertions.assertNotNull(board);
        int index = keyToIndex.get(key);
        String text = board.getLine(index);
        Assertions.assertEquals(expectedText, text);
    }
    
    @Override
    protected void logUIError(@NotNull String reason, Object... args) {
        super.logUIError(reason, args);
        throw new SidebarException(String.format(reason, args));
    }
}
