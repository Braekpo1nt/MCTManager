package org.braekpo1nt.mctmanager;

import com.github.retrooper.packetevents.PacketEvents;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MockGameManager;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
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
        String databaseMode = getConfig().getString("database.mode", "prod");
        GameStateService gameStateService = new GameStateService(
                databaseMode,
                database
        );
        return new MockGameManager(
                this,
                mctScoreboard,
                new MockGameStateStorageUtil(getLogger(), gameStateService),
                new MockSidebarFactory(),
                config,
                database,
                gameStateService
        );
    }
    
    @Override
    protected Database setupDatabase() throws SQLException {
        String sqlitePath = new File(getDataFolder(), "mctmanager.db").getAbsolutePath();
        String user = getConfig().getString("database.user", "root");
        String password = getConfig().getString("database.password", "");
        String jdbcUrl = "jdbc:sqlite:" + sqlitePath;
        flywayMigration(jdbcUrl, user, password, "", "");
        
        return new Database(sqlitePath);
    }
    
    @Override
    protected void registerCommands(@NotNull BlockEffectsListener blockEffectsListener) {
        // do nothing
    }
}
