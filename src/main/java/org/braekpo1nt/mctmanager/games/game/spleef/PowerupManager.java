package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    
    private static final List<Powerup> powerups;
    static {
        ItemStack playerSwapperItem = new ItemStack(Material.SNOWBALL);
        playerSwapperItem.editMeta(meta -> {
            meta.displayName(Component.text("Player Swapper"));
            meta.lore(List.of(
                    Component.text("Throw this at another player"),
                    Component.text("to swap positions with them.")
            ));
        });
        Powerup playerSwapper = new Powerup(playerSwapperItem, Powerup.Type.PLAYER_SWAPPER);
        
        ItemStack blockBreakerItem = new ItemStack(Material.SNOWBALL);
        blockBreakerItem.editMeta(meta -> {
            meta.displayName(Component.text("Block Breaker"));
            meta.lore(List.of(
                    Component.text("Throw this at a block"),
                    Component.text("to break it.")
            ));
        });
        Powerup blockBreaker = new Powerup(blockBreakerItem, Powerup.Type.BLOCK_BREAKER);
        powerups = List.of(playerSwapper, blockBreaker);
    }
    
    @Getter
    @AllArgsConstructor
    public static class Powerup {
        public enum Type {
            PLAYER_SWAPPER,
            BLOCK_BREAKER
        }
        private final ItemStack item;
        private final Type type;
    }
    
    public PowerupManager(Main plugin, SpleefStorageUtil storageUtil) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
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
        timeSincePowerups.remove(participant.getUniqueId());
    }
    
    /**
     * if the participant isn't already in this manager, adds them to it. Otherwise, does nothing.
     * @param participant the participant to add
     */
    public void addParticipant(Player participant) {
        if (participants.contains(participant)) {
            return;
        }
        initializeParticipant(participant);
    }
    
    /**
     * if the participant is in this manager, removes them from it. Otherwise, does nothing. 
     * @param participant the participant to remove
     */
    public void removeParticipant(Player participant) {
        if (!participants.contains(participant)) {
            return;
        }
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
                    decrementTimeSinceLastPowerup(participant);
                    handleParticipant(participant);
                }
            }
            
            private void handleParticipant(Player participant) {
                if (!canReceivePowerup(participant)) {
                    return;
                }
                randomlyGivePowerup(participant, storageUtil.getChancePerSecond());
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void decrementTimeSinceLastPowerup(Player participant) {
        int timeSincePowerup = timeSincePowerups.get(participant.getUniqueId());
        if (timeSincePowerup > 0) {
            timeSincePowerups.put(participant.getUniqueId(), timeSincePowerup - 1);
        }
    }
    
    private void resetTimeSinceLastPowerup(Player participant) {
        timeSincePowerups.put(participant.getUniqueId(), storageUtil.getMinTimeBetween());
    }
    
    /**
     * This may or may not give the participant a powerup based on the provided percent chance. The powerup given is random according to the weights provided in the config.
     * If the participant receives a powerup, their receive-cool-down is reset.
     * @param participant the participant to receive a powerup
     * @param chance the percent chance to receive a powerup
     */
    private void randomlyGivePowerup(Player participant, double chance) {
        if (random.nextDouble() < chance) {
            ItemStack powerup = getRandomPowerup();
            participant.getInventory().addItem(powerup);
            resetTimeSinceLastPowerup(participant);
        }
    }
    
    /**
     * @return a random powerup item from the available powerups, according to the weights provided in the config
     */
    private @NotNull ItemStack getRandomPowerup() {
        int index = MathUtils.getWeightedRandomIndex(storageUtil.getPowerupWeights());
        return powerups.get(index).getItem();
    }
    
    /**
     * @param participant the participant
     * @return true if the participant is allowed to receive a powerup (e.g. they've met all requirements)
     */
    private boolean canReceivePowerup(Player participant) {
        return timeSincePowerups.get(participant.getUniqueId()) <= 0 && !hasMaxPowerups(participant);
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
                num += item.getAmount();
            }
        }
        return num >= storageUtil.getMaxPowerups();
    }
    
    public void onParticipantBreakBlock(@NotNull Player participant) {
        int timeSincePowerup = timeSincePowerups.get(participant.getUniqueId());
        if (timeSincePowerup > 0) {
            return;
        }
        randomlyGivePowerup(participant, storageUtil.getBlockBreakChance());
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
        Powerup usedPowerup = itemToPowerup(mainHandItem);
        if (usedPowerup == null) {
            usedPowerup = itemToPowerup(offHandItem);
            if (usedPowerup == null) {
                return;
            }
        }
        switch (usedPowerup.getType()) {
            case PLAYER_SWAPPER -> {
                snowball.setMetadata(POWERUP_METADATA_KEY, new FixedMetadataValue(plugin, PLAYER_SWAPPER_METADATA_VALUE));
            }
            case BLOCK_BREAKER -> {
                snowball.setMetadata(POWERUP_METADATA_KEY, new FixedMetadataValue(plugin, BLOCK_BREAKER_METADATA_VALUE));
            }
        }
    }
    
    private @Nullable Powerup itemToPowerup(@NotNull ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        for (Powerup powerup : powerups) {
            if (powerup.getItem().getItemMeta().equals(itemMeta)) {
                return powerup;
            }
        }
        return null;
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
        playSwapSound(shooter);
        playSwapSound(target);
    }
    
    private void playSwapSound(Player participant) {
        participant.playSound(participant, storageUtil.getPlayerSwapSoundType(), storageUtil.getPlayerSwapSoundVolume(), storageUtil.getPlayerSwapSoundPitch());
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
        return itemToPowerup(item) != null;
    }
    
}
