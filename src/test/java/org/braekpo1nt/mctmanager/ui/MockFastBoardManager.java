package org.braekpo1nt.mctmanager.ui;

import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MockFastBoardManager extends FastBoardManager {
    
    private final ConcurrentHashMap<UUID, MockFastBoard> boards = new ConcurrentHashMap<>();
    
    public MockFastBoardManager() {
        super(null);
    }
    
    @Override
    protected synchronized void updateMainBoardForPlayer(Player player) {
        boolean playerHasBoard = givePlayerBoardIfAbsent(player);
        if (!playerHasBoard) {
            return;
        }
        UUID playerUniqueId = player.getUniqueId();
        MockFastBoard board = boards.get(playerUniqueId);
        String[] mainLines = getMainLines(playerUniqueId);
        String teamLine = mainLines[0];
        String scoreLine = mainLines[1];
        board.updateLine(0, teamLine);
        board.updateLine(1, scoreLine);
    }
    
    @Override
    protected synchronized boolean givePlayerBoardIfAbsent(Player player) {
        UUID playerUniqueId = player.getUniqueId();
        if (boards.containsKey(playerUniqueId)) {
            return true;
        }
        if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            addBoard(player);
            return true;
        }
        return false;
    }
    
    @Override
    protected synchronized void addBoard(Player player) {
        MockFastBoard newBoard = new MockFastBoard();
        newBoard.updateTitle(this.EVENT_TITLE);
        String[] mainLines = getMainLines(player.getUniqueId());
        String teamLine = mainLines[0];
        String scoreLine = mainLines[1];
        newBoard.updateLines(
                teamLine,
                scoreLine
        );
        boards.put(player.getUniqueId(), newBoard);
    }
    
    @Override
    public synchronized void updateLines(UUID playerUniqueId, String... lines) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        MockFastBoard board = boards.get(playerUniqueId);
        String[] mainLines = getMainLines(playerUniqueId);
        String[] linesPlusMainLines = new String[lines.length + 2];
        linesPlusMainLines[0] = mainLines[0];
        linesPlusMainLines[1] = mainLines[1];
        System.arraycopy(lines, 0, linesPlusMainLines, 2, lines.length);
        board.updateLines(linesPlusMainLines);
    }
    
    @Override
    public synchronized void updateLine(UUID playerUniqueId, int line, String text) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        MockFastBoard board = boards.get(playerUniqueId);
        int subLine = line + 2;
        board.updateLine(subLine, text);
    }
    
    @Override
    public synchronized void removeBoard(UUID playerUniqueId) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        MockFastBoard board = boards.get(playerUniqueId);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }
    
    @Override
    public synchronized void removeAllBoards() {
        Iterator<Map.Entry<UUID, MockFastBoard>> iterator = boards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MockFastBoard> entry = iterator.next();
            MockFastBoard board = entry.getValue();
            if (!board.isDeleted()) {
                board.delete();
            }
            iterator.remove();
        }
    }
}
