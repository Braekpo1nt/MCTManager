package org.braekpo1nt.mctmanager;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.extern.java.Log;
import org.braekpo1nt.mctmanager.commands.bugreport.BugReportCommand;
import org.braekpo1nt.mctmanager.commands.database.DatabaseCommand;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.FailureCommandResult;
import org.braekpo1nt.mctmanager.commands.mct.MCTCommand;
import org.braekpo1nt.mctmanager.commands.mctdebug.MCTDebugCommand;
import org.braekpo1nt.mctmanager.commands.notready.NotReadyCommand;
import org.braekpo1nt.mctmanager.commands.readyup.ReadyUpCommand;
import org.braekpo1nt.mctmanager.commands.readyup.UnReadyCommand;
import org.braekpo1nt.mctmanager.commands.teammsg.TeamMsgCommand;
import org.braekpo1nt.mctmanager.commands.utils.UtilsCommand;
import org.braekpo1nt.mctmanager.commands.utils.UtilsDebugCommand;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.config.HubConfigController;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.ValidateResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class Main extends JavaPlugin {
    
    public static final List<String> VALID_CONFIG_VERSIONS = List.of("0.1.0", "0.1.1", "0.1.2");
    
    /**
     * A default Gson instance for general use
     */
    public static final Gson GSON = new Gson();
    /**
     * A pretty printing Gson instance for general use
     */
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
    protected GameManager gameManager;
    protected Database database;
    public final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 3, true, false, false);
    /**
     * This should be the application-wide logger used to print logs to the console or standard out.
     * Initialized to Lombok log value so that tests don't trigger NullPointerExceptions
     */
    private static Logger logger = log;
    private static final Map<LogType, @NotNull Boolean> logTypeActive = new HashMap<>();
    private @Nullable UtilsDebugCommand utilsDebugCommand;
    
    protected GameManager initialGameManager(Scoreboard mctScoreboard, @NotNull HubConfig config, Database database) {
        String databaseMode = getConfig().getString("database.mode", "prod");
        GameStateService gameStateService = new GameStateService(
                databaseMode,
                database
        );
        return new GameManager(
                this,
                mctScoreboard,
                new GameStateStorageUtil(getLogger(), gameStateService),
                new SidebarFactory(),
                config,
                database,
                gameStateService);
    }
    
    /**
     * @return the plugin's logger
     */
    public static Logger logger() {
        return Main.logger;
    }
    
    /**
     * Use the plugin's logger to send the log message at the info level
     * @param message the message with {@link String#format(String, Object...)} style patterns
     * @param args the args to {@link String#format(String, Object...)}
     */
    public static void logf(String message, Object... args) {
        logger().info(String.format(message, args));
    }
    
    public static void setLogTypeActive(@NotNull LogType logType, boolean active) {
        logTypeActive.put(logType, active);
    }
    
    /**
     * Logs the message if the given {@link LogType} should be logged (as determined by {@link #logTypeActive})
     * @param logType the {@link LogType} of the message
     * @param message the message to log
     * @see #setLogTypeActive(LogType, boolean)
     */
    public static void debugLog(@NotNull LogType logType, @NotNull String message) {
        if (logTypeActive.getOrDefault(logType, false)) {
            Main.logger().info(message);
        }
    }
    
    /**
     * Logs the message if the given {@link LogType} should be logged (as determined by {@link #logTypeActive})
     * @param logType the {@link LogType} of the message
     * @param message the message to log.
     * Must be a valid {@link Logger#log(Level, String, Object[])} string.
     * The provided args will be used as the {code Object...}
     * arguments of the format string.
     * @param args the args the arguments of the {@link Logger#log(Level, String, Object[])} which
     * uses the message as the pattern.
     */
    public static void debugLog(@NotNull LogType logType, @NotNull String message, Object... args) {
        if (logTypeActive.get(logType)) {
            Main.logger.log(Level.INFO, message, args);
        }
    }
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        Main.logger = this.getLogger();
        Scoreboard mctScoreboard = this.getServer().getScoreboardManager().getNewScoreboard();
        ParticipantInitializer.setPlugin(this); //TODO: remove this in favor of death and respawn combination 
        
        saveDefaultConfig();
        
        PacketEvents.getAPI().init();
        
        try {
            database = setupDatabase();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "An error occurred connecting to or setting up the database. Is your config.yml set up properly? Disabling the plugin.", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        HubConfig config;
        try {
            config = new HubConfigController(getDataFolder()).getConfig();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load hub config, using default config. See console for details. %s", e.getMessage()), e);
            config = new HubConfigController(getDataFolder()).getDefaultConfig();
        }
        gameManager = initialGameManager(mctScoreboard, config, database);
        
        // Listeners
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        
        // Commands
        new UtilsCommand(this);
        new ReadyUpCommand(this, gameManager);
        new UnReadyCommand(this, gameManager);
        new TopCommand(this, gameManager);
        new TeamMsgCommand(this, gameManager);
        new BugReportCommand(this, gameManager);
        new NotReadyCommand(this, gameManager);
        
        registerCommands(blockEffectsListener);
        
        alwaysGiveNightVision();
    }
    
    protected Database setupDatabase() throws SQLException {
        String host = getConfig().getString("database.host", "localhost");
        String port = getConfig().getString("database.port", "3306");
        String user = getConfig().getString("database.user", "root");
        String password = getConfig().getString("database.password", "");
        String databaseName = getConfig().getString("database.database_name", "challenger_trials");
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", host, port, databaseName);
        flywayMigration(jdbcUrl, user, password, "ENGINE=InnoDB", "AUTO_INCREMENT");
        
        return new Database(
                host,
                port,
                user,
                password,
                databaseName
        );
    }
    
    protected void flywayMigration(String jdbcUrl, String user, String password, String engine, String autoincrement) throws SQLException {
        String mode = getConfig().getString("database.mode", "prod");
        Main.flywayMigration(jdbcUrl, user, password, engine, autoincrement, mode, getLogger(), getClass().getClassLoader());
    }
    
    public static void flywayMigration(String jdbcUrl, String user, String password, String engine, String autoincrement, String mode, Logger logger, ClassLoader classLoader) throws SQLException {
        try {
            switch (mode) {
                case "test" -> {
                    logger.info("Initiating flyway migration for test environments");
                    // include getClass().getClassLoader() for the love of all that's good and holy, 
                    // or flyway won't find your migrations
                    Flyway flyway = Flyway.configure(classLoader)
                            .dataSource(jdbcUrl, user, password)
                            .locations("classpath:db/migration") // migration folder
                            .validateOnMigrate(false) // don't block if scripts change
                            .cleanDisabled(false) // allow wiping DB
                            .placeholders(Map.of(
                                    "engine", engine,
                                    "autoincrement", autoincrement
                            ))
                            .load();
                    ValidateResult validateResult = flyway.validateWithResult();
                    if (!validateResult.validationSuccessful) {
                        logger.warning("Flyway validation failed in test environment. Cleaning the database.");
                        flyway.clean();
                    }
                    flyway.migrate();
                }
                case "prod" -> {
                    logger.info("Initiating flyway migration for production (prod) environments");
                    // include getClass().getClassLoader() for the love of all that's good and holy, 
                    // or flyway won't find your migrations
                    Flyway flyway = Flyway.configure(classLoader)
                            .dataSource(jdbcUrl, user, password)
                            .locations("classpath:db/migration") // migration folder
                            .validateOnMigrate(true)
                            .cleanDisabled(true)
                            .placeholders(Map.of(
                                    "engine", engine,
                                    "autoincrement", autoincrement
                            ))
                            .load();
                    flyway.migrate();
                }
                default -> {
                    logger.severe("database.mode not set in config.yml. Should be one of \"test\" or \"prod\". Unclear how to proceed");
                    throw new SQLException("Mis-configured database.mode in config.yml. Should be one of \"test\" or \"prod\".");
                }
            }
            
        } catch (FlywayException e) {
            throw new SQLException("An error occurred applying the flyway migration", e);
        }
        logger.info("Flyway migrations applied successfully");
    }
    
    protected void registerCommands(@NotNull BlockEffectsListener blockEffectsListener) {
        utilsDebugCommand = new UtilsDebugCommand(this);
        LiteralCommandNode<CommandSourceStack> utilsDebugCommandNode = utilsDebugCommand.build();
        LiteralCommandNode<CommandSourceStack> databaseCommand = new DatabaseCommand(this, gameManager).build();
        LiteralCommandNode<CommandSourceStack> mctDebugCommand = new MCTDebugCommand(this, gameManager).build();
        LiteralCommandNode<CommandSourceStack> mctCommand = new MCTCommand(this, gameManager, blockEffectsListener).build();
        
        // Brigadier commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(utilsDebugCommandNode);
            commands.registrar().register(databaseCommand);
            commands.registrar().register(mctDebugCommand);
            commands.registrar().register(mctCommand);
        });
    }
    
    protected void alwaysGiveNightVision() {
        Main.logger().info("[MCTManager] Night vision activated");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.addPotionEffect(NIGHT_VISION);
                }
            }
        }.runTaskTimer(this, 0L, 60L);
    }
    
    @Override
    public void onDisable() {
        ParticipantInitializer.setPlugin(null); //TODO: remove this in favor of death and respawn combination 
        PacketEvents.getAPI().terminate();
        if (gameManager != null) {
            if (gameManager.eventIsActive()) {
                gameManager.stopEvent();
            }
            gameManager.stopAllGames();
            if (gameManager.editorIsRunning()) {
                gameManager.stopEditor();
            }
            gameManager.cleanup();
        } else {
            Main.logger().info("[MCTManager] Skipping save game state.");
        }
        gameManager = null;
        if (utilsDebugCommand != null) {
            utilsDebugCommand.cleanup();
        }
        logTypeActive.clear();
        try {
//            database.close();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error closing the database on plugin disable", e);
        }
    }
}
