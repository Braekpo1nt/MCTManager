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
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.ValidateResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
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
        String sqlitePath = new File(getDataFolder(), "mctmanager.db").getAbsolutePath();
        String user = getConfig().getString("database.user", "root");
        String password = getConfig().getString("database.password", "");
        String jdbcUrl = "jdbc:sqlite:" + sqlitePath;
        String mode = getConfig().getString("database.mode", "prod");
        try {
            switch (mode) {
                case "test" -> {
                    getLogger().info("Initiating flyway migration for test environments");
                    // include getClass().getClassLoader() for the love of all that's good and holy, 
                    // or flyway won't find your migrations
                    Flyway flyway = Flyway.configure(getClass().getClassLoader())
                            .dataSource(jdbcUrl, user, password)
                            .locations("classpath:db/migration") // migration folder
                            .validateOnMigrate(false) // don't block if scripts change
                            .cleanDisabled(false) // allow wiping DB
                            .placeholders(Map.of(
                                    "engine", "",
                                    "autoincrement", ""
                            ))
                            .load();
                    ValidateResult validateResult = flyway.validateWithResult();
                    if (!validateResult.validationSuccessful) {
                        getLogger().warning("Flyway validation failed in test environment. Cleaning the database.");
                        flyway.clean();
                    }
                    flyway.migrate();
                }
                case "prod" -> {
                    getLogger().info("Initiating flyway migration for production (prod) environments");
                    // include getClass().getClassLoader() for the love of all that's good and holy, 
                    // or flyway won't find your migrations
                    Flyway flyway = Flyway.configure(getClass().getClassLoader())
                            .dataSource(jdbcUrl, user, password)
                            .locations("classpath:db/migration") // migration folder
                            .validateOnMigrate(true)
                            .cleanDisabled(true)
                            .placeholders(Map.of(
                                    "engine", "",
                                    "autoincrement", ""
                            ))
                            .load();
                    flyway.migrate();
                }
                default -> {
                    getLogger().severe("database.mode not set in config.yml. Should be one of \"test\" or \"prod\". Unclear how to proceed");
                    throw new SQLException("Mis-configured database.mode in config.yml. Should be one of \"test\" or \"prod\".");
                }
            }
            
        } catch (FlywayException e) {
            throw new SQLException("An error occurred applying the flyway migration", e);
        }
        getLogger().info("Flyway migrations applied successfully");
        
        return new Database(sqlitePath);
    }
    
    @Override
    protected void registerCommands() {
        // do nothing
    }
}
