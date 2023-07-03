package org.braekpo1nt.mctmanager.ui;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.UUID;

public class MockFastBoardManager extends FastBoardManager {
    
    
    public MockFastBoardManager() {
        super(null);
    }
    
    @Override
    protected synchronized void addBoard(Player player) {
        MockFastBoardWrapper newBoard = new MockFastBoardWrapper();
        newBoard.updateTitle(this.EVENT_TITLE);
        String[] mainLines = getHeader(player.getUniqueId(), HeaderType.PERSONAL);
        String teamLine = mainLines[0];
        String scoreLine = mainLines[1];
        newBoard.updateLines(
                teamLine,
                scoreLine
        );
        boards.put(player.getUniqueId(), newBoard);
        headerTypes.put(player.getUniqueId(), HeaderType.PERSONAL);
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
