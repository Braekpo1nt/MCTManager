package org.braekpo1nt.mctmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.java.Log;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.commands.mct.MCTCommand;
import org.braekpo1nt.mctmanager.commands.mctdebug.MCTDebugCommand;
import org.braekpo1nt.mctmanager.commands.readyup.ReadyUpCommand;
import org.braekpo1nt.mctmanager.commands.readyup.UnReadyCommand;
import org.braekpo1nt.mctmanager.commands.utils.UtilsCommand;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

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
    private GameManager gameManager;
    private boolean saveGameStateOnDisable = true;
    public final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 3, true, false, false);
    private MCTCommand mctCommand;
    /**
     * This should be the application-wide logger used to print logs to the console or standard out. 
     * Initialized to Lombok log value so that tests don't trigger NullPointerExceptions
     */
    private static Logger logger = log;
    private static final Map<LogType, @NotNull Boolean> logTypeActive = new HashMap<>();
    
    protected GameManager initialGameManager(Scoreboard mctScoreboard) {
        return new GameManager(this, mctScoreboard);
    }
    
    /**
     * @return the plugin's logger
     */
    public static Logger logger() {
        return Main.logger;
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
     *                Must be a valid {@link Logger#log(Level, String, Object[])} string. 
     *                The provided args will be used as the {code Object...}
     *                arguments of the format string.
     * @param args the args the arguments of the {@link Logger#log(Level, String, Object[])} which 
     *             uses the message as the pattern.
     */
    public static void debugLog(@NotNull LogType logType, @NotNull String message, Object... args) {
        if (logTypeActive.get(logType)) {
            Main.logger.log(Level.INFO, message, args);
        }
    }
    
    @Override
    public void onEnable() {
        Main.logger = this.getLogger();
        Scoreboard mctScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        ParticipantInitializer.setPlugin(this); //TODO: remove this in favor of death and respawn combination 
        
        gameManager = initialGameManager(mctScoreboard);
        try {
            gameManager.loadHubConfig();
        } catch (ConfigException e) {
            Main.logger().severe(String.format("[MCTManager] Could not load hub config, see console for details. %s", e.getMessage()));
            e.printStackTrace();
            saveGameStateOnDisable = false;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        boolean ableToLoadGameSate = gameManager.loadGameState();
        if (!ableToLoadGameSate) {
            Main.logger().severe("[MCTManager] Could not load game state from memory. Disabling plugin.");
            saveGameStateOnDisable = false;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Listeners
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        
        // Commands
        new MCTDebugCommand(this);
        mctCommand = new MCTCommand(this, gameManager, blockEffectsListener);
        new UtilsCommand(this);
        new ReadyUpCommand(this, gameManager);
        new UnReadyCommand(this, gameManager);
        new TopCommand(this, gameManager);
    
        alwaysGiveNightVision();
    }
    
    public MCTCommand getMctCommand() {
        return mctCommand;
    }
    
    private void alwaysGiveNightVision() {
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
        if (saveGameStateOnDisable && gameManager != null) {
            gameManager.cancelVote();
            gameManager.tearDown();
            if (gameManager.getEventManager().eventIsActive()) {
                gameManager.getEventManager().stopEvent(Bukkit.getConsoleSender());
            }
            if (gameManager.getEventManager().colossalCombatIsActive()) {
                gameManager.getEventManager().stopColossalCombat(Bukkit.getConsoleSender());
            }
            if (gameManager.gameIsRunning()) {
                gameManager.manuallyStopGame(false);
            }
            if (gameManager.editorIsRunning()) {
                gameManager.stopEditor(Bukkit.getConsoleSender());
            }
            gameManager.saveGameState();
        } else {
            Main.logger().info("[MCTManager] Skipping save game state.");
        }
    }
    
    // Testing methods for mocking components
    
    public GameManager getGameManager() {
        return gameManager;
    }
}
