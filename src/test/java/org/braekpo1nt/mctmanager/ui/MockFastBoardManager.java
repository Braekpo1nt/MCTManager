package org.braekpo1nt.mctmanager.ui;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MockFastBoardManager extends FastBoardManager {
    
    private final ConcurrentHashMap<UUID, String[]> boards = new ConcurrentHashMap<>();
    
    public MockFastBoardManager() {
        super(null);
    }
    
    @Override
    protected synchronized void updateMainBoardForPlayer(Player player) {
//        boolean playerHasBoard = givePlayerBoardIfAbsent(player);
//        if (!playerHasBoard) {
//            return;
//        }
//        UUID playerUniqueId = player.getUniqueId();
//        String[] board = boards.get(playerUniqueId);
        
    }
    
    @Override
    protected synchronized boolean givePlayerBoardIfAbsent(Player player) {
        return false;
    }
    
    @Override
    protected synchronized void addBoard(Player player) {
        
    }
    
    @Override
    protected String[] getMainLines(UUID playerUniqueId) {
        return null;
    }
    
    @Override
    public synchronized void updateLines(UUID playerUniqueId, String... lines) {
        
    }
    
    @Override
    public synchronized void updateLine(UUID playerUniqueId, int line, String text) {
        
    }
    
    @Override
    public synchronized void removeBoard(UUID playerUniqueId) {
        
    }
    
    @Override
    public synchronized void removeAllBoards() {
        
    }
}
