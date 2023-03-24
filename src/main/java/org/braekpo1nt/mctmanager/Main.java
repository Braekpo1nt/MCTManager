package org.braekpo1nt.mctmanager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.braekpo1nt.mctmanager.commands.MCTCommand;
import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.listeners.HubBoundaryListener;
import org.braekpo1nt.mctmanager.listeners.PlayerJoinListener;
import org.braekpo1nt.mctmanager.placeholder.ScoreExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin {
    
    public static MultiverseCore multiverseCore;
    
    private final static PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 200, true, false, false);
    private final static PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 1000000, 200, true, false, false);
    private final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 3, true, false, false);
    private final static PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000000, 1, true, false, false);
    private final static PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 1000000, 250, true, false, false);
    private Scoreboard mctScoreboard;
    private GameManager gameManager;
    private boolean saveGameStateOnDisable = true;
    
    @Override
    public void onEnable() {
        
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (multiversePlugin == null) {
            Bukkit.getLogger().severe("[MCTManager] Cannot find Multiverse-Core. [MCTManager] depends on it and cannot proceed without it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Main.multiverseCore = ((MultiverseCore) multiversePlugin);
        Plugin placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderApi == null) {
            Bukkit.getLogger().severe("[MCTManager] Cannot find PlaceholderAPI. [MCTManager] depends on it and cannot proceed without it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        mctScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        gameManager = new GameManager(this, mctScoreboard);
        try {
            gameManager.loadGameState();
        } catch (IOException e) {
            Bukkit.getLogger().severe("[MCTManager] Could not load game state from memory. Printing stack trace below. Disabling plugin.");
            e.printStackTrace();
            saveGameStateOnDisable = false;
            Bukkit.getPluginManager().disablePlugin(this);
        }
        
        // PlaceholderExpansions
        new ScoreExpansion(gameManager).register();
        
        // Listeners
        HubBoundaryListener hubBoundaryListener = new HubBoundaryListener(this);
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        new PlayerJoinListener(this, mctScoreboard);
        
        // Commands
        new MCTDebugCommand(this);
        new MCTCommand(this, gameManager, hubBoundaryListener, blockEffectsListener);
        
        File dataFolder = getDataFolder();
        initializeStatusEffectScheduler();
    }
    
    @Override
    public void onDisable() {
        if (saveGameStateOnDisable && gameManager != null) {
            try {
                gameManager.saveGameState();
            } catch (IOException e) {
                Bukkit.getLogger().severe("[MCTManager] Could not save game state. Printing stack trace below.");
                e.printStackTrace();
            }
        } else {
            Bukkit.getLogger().info("[MCTManager] Skipping save game state.");
        }
    }
    
    public static void giveAmbientStatusEffects(Player player) {
        player.addPotionEffect(RESISTANCE);
        player.addPotionEffect(REGENERATION);
        player.addPotionEffect(NIGHT_VISION);
        player.addPotionEffect(FIRE_RESISTANCE);
        player.addPotionEffect(SATURATION);
    }
    
    private void initializeStatusEffectScheduler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    giveAmbientStatusEffects(player);
                }
            }
        }, 0L, 60L);
    }
}
