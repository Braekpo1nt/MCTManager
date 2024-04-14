package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PowerupManager implements Listener {
    
    private static final String POWERUP_METADATA_KEY = "powerup";
    private static final String PLAYER_SWAPPER_METADATA_VALUE = "player_swapper";
    private final Main plugin;
    private final SpleefStorageUtil storageUtil;
    private List<Player> participants;
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
        participants = new ArrayList<>(newParticipants.size());
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participant.getInventory().addItem(playerSwapper);
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
    }
    
    private void resetParticipant(Player participant) {
        // doesn't do anything at this time
    }
    
    public void onParticipantJoin(Player participant) {
        initializeParticipant(participant);
    }
    
    public void onParticipantQuit(Player participant) {
        resetParticipant(participant);
        participants.remove(participant);
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
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (!(event.getEntity() instanceof Snowball snowball)) {
            return;
        }
        if (participant.getInventory().getItemInMainHand().equals(playerSwapper) || participant.getInventory().getItemInOffHand().equals(playerSwapper)) {
            snowball.setMetadata(POWERUP_METADATA_KEY, new FixedMetadataValue(plugin, PLAYER_SWAPPER_METADATA_VALUE));
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) {
            return;
        }
        if (!(snowball.getShooter() instanceof Player shooter)) {
            return;
        }
        if (!participants.contains(shooter)) {
            return;
        }
        List<MetadataValue> metadata = snowball.getMetadata(POWERUP_METADATA_KEY);
        if (metadata.isEmpty()) {
            return;
        }
        if (event.getHitEntity() instanceof Player target) {
            if (!participants.contains(target)) {
                return;
            }
            String powerupType = metadata.get(0).asString();
            if (powerupType.equals(PLAYER_SWAPPER_METADATA_VALUE)) {
                plugin.getLogger().info(String.format("%s threw a player swapper at %s", shooter.getName(), target.getName()));
                swapPlayers(shooter, target);
            }
            return;
        }
        if (event.getHitBlock() != null) {
            // check if it's block breaker
        }
    }
    
    private void swapPlayers(Player shooter, Player target) {
        Location shooterLoc = shooter.getLocation();
        Location targetLoc = target.getLocation();
        shooter.teleport(targetLoc);
        target.teleport(shooterLoc);
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
