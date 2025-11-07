package org.braekpo1nt.mctmanager;

import com.github.retrooper.packetevents.PacketEvents;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MockGameManager;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.packetevents.PacketEventsAPIMock;
import org.braekpo1nt.mctmanager.ui.sidebar.MockSidebarFactory;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
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
    protected GameManager initialGameManager(Scoreboard mctScoreboard, @NotNull HubConfig config, Database database) {
        return new MockGameManager(
                this,
                mctScoreboard,
                new MockGameStateStorageUtil(this),
                new MockSidebarFactory(),
                config,
                database);
    }
    
    @Override
    protected Database setupDatabase() throws SQLException {
        return new Database(new File(getDataFolder(), "mctmanager.db").getAbsolutePath());
    }
    
    @Override
    protected void registerCommands() {
        // do nothing
    }
}
