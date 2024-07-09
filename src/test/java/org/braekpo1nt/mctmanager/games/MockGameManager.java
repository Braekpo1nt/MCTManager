package org.braekpo1nt.mctmanager.games;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.braekpo1nt.mctmanager.hub.MockHubManager;
import org.bukkit.scoreboard.Scoreboard;

public class MockGameManager extends GameManager {
    
    public MockGameManager(Main plugin, Scoreboard mctScoreboard) {
        super(plugin, mctScoreboard);
    }
    
    @Override
    protected HubManager initializeHubManager(Main plugin, GameManager gameManager) {
        return new MockHubManager(plugin, gameManager);
    }
}
