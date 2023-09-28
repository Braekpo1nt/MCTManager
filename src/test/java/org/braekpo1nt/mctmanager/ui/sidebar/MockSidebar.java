package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class MockSidebar extends Sidebar {
    @Override
    public synchronized void addPlayer(@NotNull Player player) {
        Preconditions.checkArgument(!boardsLines.containsKey(player.getUniqueId()), "player with UUID \"%s\" already has a board in this manager", player.getUniqueId());
        FastBoardWrapper newBoard = new MockFastBoardWrapper();
        newBoard.setPlayer(player);
        newBoard.updateTitle(this.title);
        boards.put(player.getUniqueId(), newBoard);
        List<String> lines = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lines.add("");
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
}
