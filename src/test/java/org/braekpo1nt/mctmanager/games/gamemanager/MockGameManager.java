package org.braekpo1nt.mctmanager.games.gamemanager;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.MockTabList;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
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
            @NotNull HubConfig config,
            @NotNull Database database,
            @NotNull GameStateService gameStateService) {
        super(
                plugin,
                mctScoreboard,
                gameStateStorageUtil,
                sidebarFactory,
                config,
                database,
                gameStateService);
    }
    
    @Override
    public @NotNull TabList createTabList(Main plugin) {
        return new MockTabList(plugin);
    }
    
    @Override
    public List<LeaderboardManager> createLeaderboardManagers() {
        return Collections.emptyList();
    }
    
    @Override
    public ArgumentType<?> getComponentArgumentType() {
        return StringArgumentType.word();
    }
    
    @Override
    public ArgumentType<?> getPlayerArgumentType() {
        return StringArgumentType.word();
    }
}
