package org.braekpo1nt.mctmanager.games.gamestate;

import org.braekpo1nt.mctmanager.database.service.GameStateService;

import java.sql.SQLException;
import java.util.logging.Logger;

public class MockGameStateStorageUtil extends GameStateStorageUtil {
    
    public MockGameStateStorageUtil(Logger logger, GameStateService gameStateService) {
        super(logger, gameStateService, false);
    }

//    @Override
//    public void loadGameState() {
//        this.gameState = new GameState();
//    }
    
}
