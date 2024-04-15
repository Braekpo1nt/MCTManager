package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import java.util.*;

public class PowerupManager implements Listener {
    
    private static final String POWERUP_METADATA_KEY = "powerup";
    private static final String PLAYER_SWAPPER_METADATA_VALUE = "player_swapper";
    private static final String BLOCK_BREAKER_METADATA_VALUE = "block_breaker";
    private final Main plugin;
    private final SpleefStorageUtil storageUtil;
    private List<Player> participants;
    private Map<UUID, Integer> timeSincePowerups;
    private final Random random = new Random();
    private int powerupTimerTaskId;
    
    private final ItemStack playerSwapper;
    private final ItemStack blockBreaker;
    
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
        blockBreaker = new ItemStack(Material.SNOWBALL);
        blockBreaker.editMeta(meta -> {
            meta.displayName(Component.text("Block Breaker"));
            meta.lore(List.of(
                    Component.text("Throw this at a block"),
                    Component.text("to break it.")
            ));
        });
    }
    
    public void start(List<Player> newParticipants) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        participants = new ArrayList<>(newParticipants.size());
        timeSincePowerups = new HashMap<>(newParticipants.size());
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startPowerupTimer();
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        timeSincePowerups.put(participant.getUniqueId(), 0);
        participant.getInventory().addItem(playerSwapper, blockBreaker);
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        timeSincePowerups.clear();
    }
    
    private void resetParticipant(Player participant) {
        // doesn't do anything at this time
        timeSincePowerups.remove(participant.getUniqueId());
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
                for (Player participant : participants) {
                    int timeSincePowerup = timeSincePowerups.get(participant.getUniqueId());
                    if (timeSincePowerup > 0) {
                        timeSincePowerups.put(participant.getUniqueId(), timeSincePowerup - 1);
                        return;
                    }
                    if (hasMaxPowerups(participant)) {
                        return;
                    }
                    boolean gotPowerup = randomlyGivePowerup(participant, storageUtil.getChancePerSecond());
                    if (gotPowerup) {
                        timeSincePowerups.put(participant.getUniqueId(), storageUtil.getMinTimeBetween());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * This may or may not give the participant a powerup based on the provided percent chance. The powerup given is randomly determined by the distribution provided in the config.
     * @param participant the participant to receive a powerup
     * @param chance the percent chance to receive a powerup
     * @return true if the participant was given a powerup, false otherwise.
     */
    private boolean randomlyGivePowerup(Player participant, double chance) {
        if (random.nextDouble() < chance) {
            ItemStack powerup = getRandomPowerup();
            participant.getInventory().addItem(powerup);
            return true;
        }
        return false;
    }
    
    private @NotNull ItemStack getRandomPowerup() {
        List<ItemStack> powerups = List.of(playerSwapper, blockBreaker);
        int[] weights = {1, 1};
        int index = MathUtils.getWeightedRandomIndex(weights);
        return powerups.get(index);
    }
    
    /**
     * @param participant the participant
     * @return true if the participant has the maximum number of powerups allowed in their inventory, false if not. 
     */
    private boolean hasMaxPowerups(Player participant) {
        if (storageUtil.getMaxPowerups() < 0) {
            return false;
        }
        int num = 0;
        for (ItemStack item : participant.getInventory().getContents()) {
            if (item != null && isPowerup(item)) {
                num++;
            }
        }
        return num >= storageUtil.getMaxPowerups();
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
        ItemStack mainHandItem = participant.getInventory().getItemInMainHand();
        ItemStack offHandItem = participant.getInventory().getItemInOffHand();
        if (mainHandItem.equals(playerSwapper) || offHandItem.equals(playerSwapper)) {
            snowball.setMetadata(POWERUP_METADATA_KEY, new FixedMetadataValue(plugin, PLAYER_SWAPPER_METADATA_VALUE));
        } else if (mainHandItem.equals(blockBreaker) || offHandItem.equals(blockBreaker)) {
            snowball.setMetadata(POWERUP_METADATA_KEY, new FixedMetadataValue(plugin, BLOCK_BREAKER_METADATA_VALUE));
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
        String powerupType = metadata.get(0).asString();
        if (event.getHitEntity() instanceof Player target) {
            if (!participants.contains(target)) {
                return;
            }
            if (!powerupType.equals(PLAYER_SWAPPER_METADATA_VALUE)) {
                return;
            }
            swapPlayers(shooter, target);
            return;
        }
        Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            Material hitBlockType = hitBlock.getType();
            if (!hitBlockType.equals(storageUtil.getLayerBlock()) && !hitBlockType.equals(storageUtil.getDecayBlock())) {
                return;
            }
            if (!powerupType.equals(BLOCK_BREAKER_METADATA_VALUE)) {
                return;
            }
            hitBlock.setType(Material.AIR);
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
        return item.equals(playerSwapper) || item.equals(blockBreaker);
    }
    
}
