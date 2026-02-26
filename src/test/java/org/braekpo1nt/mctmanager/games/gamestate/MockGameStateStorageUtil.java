package org.braekpo1nt.mctmanager.games.gamestate;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.service.GameStateService;

public class MockGameStateStorageUtil extends GameStateStorageUtil {
    
    public MockGameStateStorageUtil(Main plugin, GameStateService gameStateService) {
        super(plugin, gameStateService);
    }
    
    @Override
    public void loadGameState() {
        this.gameState = new GameState();
    }
    
    public void saveGameState() {
    }
}
