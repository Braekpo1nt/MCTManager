package org.braekpo1nt.mctmanager;

import com.github.retrooper.packetevents.PacketEvents;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.ImmediateExecutorService;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MockGameManager;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.packetevents.PacketEventsAPIMock;
import org.braekpo1nt.mctmanager.ui.sidebar.MockSidebarFactory;
import org.bukkit.Server;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
                gameStateService,
                Runnable::run
        );
    }
    
    @Override
    protected String getMigrationLocation() {
        return "classpath:db/migration/test";
    }
    
    @Override
    protected @NotNull ExecutorService createDatabaseExecutor() {
        // the tests shouldn't use a threaded executor service
        // Cast the server to the MyCustomServerMock implementation so that you can mark non-thread-safe operations
        if (!(getServer() instanceof MyCustomServerMock server)) {
            throw new IllegalStateException("MockMain requires an implementation of MyCustomServerMock to function properly. Use MockBukkit.mock(new MyCustomServerMock()) instead of another Server implementation.");
        }
        return new ImmediateExecutorService(server);
    }
    
    @Override
    protected @NotNull Executor createMainThreadExecutor() {
        return Runnable::run;
    }
    
    @Override
    protected Database setupDatabase() throws SQLException {
        String sqlitePath = new File(getDataFolder(), "mctmanager.db").getAbsolutePath();
        String user = getConfig().getString("database.user", "root");
        String password = getConfig().getString("database.password", "");
        boolean baselineOnMigrate = getConfig().getBoolean("database.baselineOnMigrate", false);
        String jdbcUrl = "jdbc:sqlite:" + sqlitePath;
        flywayMigration(jdbcUrl, user, password, "", "", baselineOnMigrate);
        
        // Use a basic executor so that tests will run sequentially, rather than relying on unpredictable
        // threads and asynchronous timing
        return new Database(sqlitePath, Runnable::run);
    }
    
    @Override
    protected void alwaysGiveNightVision() {
        // do nothing
    }
    
    @Override
    protected void registerCommands(@NotNull BlockEffectsListener blockEffectsListener) {
        // do nothing
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public Database getDatabase() {
        return database;
    }
    
    @Override
    public ArgumentType<?> getUUIDArgumentType() {
        return StringArgumentType.word();
    }
}
