package org.braekpo1nt.mctmanager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.braekpo1nt.mctmanager.commands.MCTCommand;
import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.hub.HubBoundaryListener;
import org.braekpo1nt.mctmanager.listeners.PlayerJoinListener;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.io.IOException;

public final class Main extends JavaPlugin {
    
    public static MultiverseCore multiverseCore;
    private GameManager gameManager;
    private boolean saveGameStateOnDisable = true;
    public final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 3, true, false, false);
    
    @Override
    public void onEnable() {
        
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (multiversePlugin == null) {
            Bukkit.getLogger().severe("[MCTManager] Cannot find Multiverse-Core. [MCTManager] depends on it and cannot proceed without it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Main.multiverseCore = ((MultiverseCore) multiversePlugin);
    
        Scoreboard mctScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        gameManager = new GameManager(this, mctScoreboard);
        try {
            gameManager.loadGameState();
        } catch (IOException e) {
            Bukkit.getLogger().severe("[MCTManager] Could not load game state from memory. Printing stack trace below. Disabling plugin.");
            e.printStackTrace();
            saveGameStateOnDisable = false;
            Bukkit.getPluginManager().disablePlugin(this);
        }
        
        // Listeners
        HubBoundaryListener hubBoundaryListener = new HubBoundaryListener(this);
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        new PlayerJoinListener(this, mctScoreboard);
        
        // Commands
        new MCTDebugCommand(this);
        new MCTCommand(this, gameManager, hubBoundaryListener, blockEffectsListener);
    
        alwaysGiveNightVision();
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
            gameManager.cancelFastBoardManager();
            gameManager.cancelVote();
            gameManager.cancelReturnToHub();
            try {
                gameManager.saveGameState();
                if (gameManager.gameIsRunning()) {
                    gameManager.manuallyStopGame(false);
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("[MCTManager] Could not save game state. Printing stack trace below.");
                e.printStackTrace();
            }
        } else {
            Bukkit.getLogger().info("[MCTManager] Skipping save game state.");
        }
    }
}
