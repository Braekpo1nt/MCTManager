package org.braekpo1nt.mctmanager;

import org.braekpo1nt.mctmanager.commands.MCTCommand;
import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.braekpo1nt.mctmanager.commands.utils.UtilsCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

public class Main extends JavaPlugin {

    public static final String CONFIG_VERSION = "0.1.0";
    private GameManager gameManager;
    private boolean saveGameStateOnDisable = true;
    public final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 3, true, false, false);
    private MCTCommand mctCommand;
    
    @Override
    public void onEnable() {
        
        Scoreboard mctScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        ParticipantInitializer.setPlugin(this); //TODO: remove this in favor of death and respawn combination 
        
        gameManager = new GameManager(this, mctScoreboard);
        try {
            if (!gameManager.loadHubConfig()) {
                throw new IllegalArgumentException("Could not load config");
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(String.format("[MCTManager] Could not load hub config, see console for details. %s", e.getMessage()));
            e.printStackTrace();
            saveGameStateOnDisable = false;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        boolean ableToLoadGameSate = gameManager.loadGameState();
        if (!ableToLoadGameSate) {
            Bukkit.getLogger().severe("[MCTManager] Could not load game state from memory. Disabling plugin.");
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
    
        alwaysGiveNightVision();
    }
    
    public MCTCommand getMctCommand() {
        return mctCommand;
    }
    
    private void alwaysGiveNightVision() {
        Bukkit.getLogger().info("[MCTManager] Night vision activated");
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
        if (saveGameStateOnDisable && gameManager != null) {
            gameManager.cancelVote();
            gameManager.cancelAllTasks();
            gameManager.saveGameState();
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
        } else {
            Bukkit.getLogger().info("[MCTManager] Skipping save game state.");
        }
    }
    
    // Testing methods for mocking components
    
    public GameManager getGameManager() {
        return gameManager;
    }
}
