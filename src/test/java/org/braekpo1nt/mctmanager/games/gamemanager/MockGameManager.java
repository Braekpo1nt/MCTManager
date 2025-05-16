package org.braekpo1nt.mctmanager.games.gamemanager;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MockGameManager extends GameManager {
    public MockGameManager(
            Main plugin, 
            Scoreboard mctScoreboard, 
            @NotNull GameStateStorageUtil gameStateStorageUtil, 
            @NotNull SidebarFactory sidebarFactory, 
            @NotNull HubConfig config) {
        super(
                plugin, 
                mctScoreboard, 
                gameStateStorageUtil,
                sidebarFactory, 
                config);
    }
    
    @Override
    public List<LeaderboardManager> createLeaderboardManagers() {
        return Collections.emptyList();
    }
}
