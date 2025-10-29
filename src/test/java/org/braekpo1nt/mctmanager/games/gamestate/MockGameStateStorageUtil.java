package org.braekpo1nt.mctmanager.games.gamestate;

import org.braekpo1nt.mctmanager.Main;

public class MockGameStateStorageUtil extends GameStateStorageUtil {
    
    public MockGameStateStorageUtil(Main plugin) {
        super(plugin);
    }
    
    @Override
    public void loadGameState() {
        this.gameState = new GameState();
    }
    
    @Override
    public void saveGameState() {
    }
}