package org.braekpo1nt.mctmanager;

import com.github.retrooper.packetevents.PacketEvents;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MockGameManager;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.packetevents.PacketEventsAPIMock;
import org.braekpo1nt.mctmanager.ui.sidebar.MockSidebarFactory;
import org.bukkit.scoreboard.Scoreboard;

import java.util.logging.Level;

public class MockMain extends Main {
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(new PacketEventsAPIMock(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        this.getLogger().setLevel(Level.OFF);
        super.onEnable();
    }
    
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
