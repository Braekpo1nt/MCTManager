package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MockGameManager;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.ui.sidebar.MockSidebarFactory;
import org.bukkit.scoreboard.Scoreboard;

public class MockMain extends Main {
    
    @Override
    protected GameManager initialGameManager(Scoreboard mctScoreboard) {
        GameManager gameManager = new MockGameManager(this, mctScoreboard);
        MockGameStateStorageUtil mockGameStateStorageUtil = new MockGameStateStorageUtil(this);
        gameManager.setGameStateStorageUtil(mockGameStateStorageUtil);
        MockSidebarFactory mockSidebarFactory = new MockSidebarFactory();
        gameManager.setSidebarFactory(mockSidebarFactory);
        return gameManager;
    }
}
