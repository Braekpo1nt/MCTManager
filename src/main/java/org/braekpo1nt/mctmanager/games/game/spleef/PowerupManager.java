package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class PowerupManager implements Listener {
    
    private final Main plugin;
    private final SpleefStorageUtil storageUtil;
    private final Random random = new Random();
    private int powerupTimerTaskId;
    
    private final ItemStack playerSwapper;
    
    public PowerupManager(Main plugin, SpleefStorageUtil storageUtil) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
        playerSwapper = new ItemStack(Material.SNOWBALL);
        playerSwapper.editMeta(meta -> {
            meta.displayName(Component.text("Player Swapper"));
            meta.lore(List.of(
                    Component.text("Throw this at another player"),
                    Component.text("to swap positions with them.")
            ));
        });
    }
    
    public void start(List<Player> newParticipants) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player newParticipant : newParticipants) {
            newParticipant.getInventory().addItem(playerSwapper);
        }
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(powerupTimerTaskId);
    }
    
    private void startPowerupTimer() {
        this.powerupTimerTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void onParticipantBreakBlock(@NotNull Player participant) {
        
    }
    
    @EventHandler
    public void onSnowballThrow(ProjectileLaunchEvent event) {
        
    }
    
    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball) {
            plugin.getLogger().info("Snowball name: " + snowball.getName());
            if (event.getHitEntity() instanceof Player hitPlayer) {
                if (event.getEntity().getShooter() instanceof Player shooter) {
                    plugin.getLogger().info("Snowball thrown by " + shooter.getName() + " hit " + hitPlayer.getName());
                } else {
                    plugin.getLogger().info("Snowball thrown by unknown shooter hit " + hitPlayer.getName());
                }
            } else if (event.getHitBlock() != null) {
                if (event.getEntity().getShooter() instanceof Player shooter) {
                    plugin.getLogger().info("Snowball thrown by " + shooter.getName() + " hit " + event.getHitBlock());
                } else {
                    plugin.getLogger().info("Snowball thrown by unknown shooter hit " + event.getHitBlock());
                }
            } else {
                plugin.getLogger().info("Snowball thrown by unknown shooter hit an unknown location");
            }
        }
    }
    
    /**
     * 
     * @param item the item which might be a powerup
     * @return true if the given item is one of the powerups 
     */
    public boolean isPowerup(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.equals(playerSwapper);
    }
    
}
