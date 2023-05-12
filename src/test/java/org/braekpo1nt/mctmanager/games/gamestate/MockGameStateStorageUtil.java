package org.braekpo1nt.mctmanager.games.gamestate;

import org.braekpo1nt.mctmanager.Main;

import java.io.IOException;

public class MockGameStateStorageUtil extends GameStateStorageUtil {
    
    public MockGameStateStorageUtil(Main plugin) {
        super(plugin);
    }
    
    @Override
    public void loadGameState() throws IOException {
        this.gameState = new GameState();
    }
    
    @Override
    public void saveGameState() throws IOException {}
}