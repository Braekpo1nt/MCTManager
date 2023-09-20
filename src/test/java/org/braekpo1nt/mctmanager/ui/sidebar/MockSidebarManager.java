package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.ui.FastBoardWrapper;
import org.braekpo1nt.mctmanager.ui.MockFastBoardWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MockSidebarManager extends SidebarManager {
    
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
    
    public void assertLines(UUID playerUniqueId, String... expectedLines) {
        MockFastBoardWrapper board = (MockFastBoardWrapper) boards.get(playerUniqueId);
        Assertions.assertNotNull(board);
        String[] lines = board.getLines();
        Assertions.assertEquals(expectedLines, Arrays.copyOfRange(lines, 2, lines.length));
    }
    
    public void assertLine(UUID playerUniqueId, int line, String expectedText) {
        MockFastBoardWrapper board = (MockFastBoardWrapper) boards.get(playerUniqueId);
        Assertions.assertNotNull(board);
        int subLine = line + 2;
        String text = board.getLine(subLine);
        Assertions.assertEquals(expectedText, text);
    }
}
