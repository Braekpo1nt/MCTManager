package org.braekpo1nt.mctmanager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.braekpo1nt.mctmanager.commands.MCTCommand;
import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.listeners.HubBoundaryListener;
import org.braekpo1nt.mctmanager.listeners.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin {
    
    public static MultiverseCore multiverseCore;
    
    private final static PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 200, true, false, false);
    private final static PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 1000000, 200, true, false, false);
    private final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 3, true, false, false);
    private final static PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000000, 1, true, false, false);
    private final static PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 1000000, 250, true, false, false);
    
    @Override
    public void onEnable() {
    
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (multiversePlugin == null) {
            Bukkit.getLogger().severe("[MCTManager] Cannot find Multiverse-Core. [MCTManager] depends on it and cannot proceed without it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Main.multiverseCore = ((MultiverseCore) multiversePlugin);
        
        Bukkit.getLogger().info(String.format("%s -- %s", Bukkit.getVersion(), Bukkit.getBukkitVersion()));
        
        GameManager gameManager = new GameManager(this);
        
        // Listeners
        HubBoundaryListener hubBoundaryListener = new HubBoundaryListener(this);
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        new PlayerJoinListener(this);
        
        // Commands
        new MCTDebugCommand(this);
        new MCTCommand(this, gameManager, hubBoundaryListener, blockEffectsListener);

        File dataFolder = getDataFolder();
        initializeStatusEffectScheduler();
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
