package org.braekpo1nt.mctmanager.games.gamestate;

import org.braekpo1nt.mctmanager.Main;

import java.util.ArrayList;
import java.util.HashMap;

public class MockGameStateStorageUtil extends GameStateStorageUtil {
    
    public MockGameStateStorageUtil(Main plugin) {
        super(plugin);
    }
    
    @Override
    public void loadGameState() {
        this.gameState = new GameState(new HashMap<>(), new HashMap<>(), new ArrayList<>());
    }
    
    @Override
    public void saveGameState() {}
}