package org.braekpo1nt.mctmanager.games.game.spleef.powerup;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.participant.Participant;
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
    private final SpleefConfig config;
    private Map<UUID, Participant> participants = new HashMap<>();
    /**
     * for each participant UUID, the system time of the moment they last received a powerup
     */
    private Map<UUID, Long> lastPowerupTimestamps = new HashMap<>();
    private final Random random = new Random();
    private int powerupTimerTaskId;
    private boolean shouldGivePowerups = false;
    
    /**
     * a list of all powerups
     */
    private static final List<Powerup> powerups;
    /**
     * a map of {@link Powerup.Type} to their respective {@link Powerup} (for convenience)
     */
    private static final Map<Powerup.Type, Powerup> typeToPowerup;
    static {
        ItemStack playerSwapperItem = new ItemStack(Material.SNOWBALL);
        playerSwapperItem.editMeta(meta -> {
            meta.displayName(Component.text("Player Swapper"));
            meta.lore(List.of(
                    Component.text("Throw this at another player"),
                    Component.text("to swap positions with them.")
            ));
            meta.setCustomModelData(2);
        });
        Powerup playerSwapper = new Powerup(playerSwapperItem, Powerup.Type.PLAYER_SWAPPER);
        
        ItemStack blockBreakerItem = new ItemStack(Material.SNOWBALL);
        blockBreakerItem.editMeta(meta -> {
            meta.displayName(Component.text("Block Breaker"));
            meta.lore(List.of(
                    Component.text("Throw this at a block"),
                    Component.text("to break it.")
            ));
            meta.setCustomModelData(1);
        });
        Powerup blockBreaker = new Powerup(blockBreakerItem, Powerup.Type.BLOCK_BREAKER);
    
        ItemStack shieldItem = new ItemStack(Material.LAPIS_LAZULI);
        shieldItem.editMeta(meta -> {
            meta.displayName(Component.text("Swap Shield"));
            meta.lore(List.of(
                    Component.text("- Activates automatically"),
                    Component.text("- Single use")
            ));
            meta.setCustomModelData(3);
        });
        Powerup shield = new Powerup(shieldItem, Powerup.Type.SHIELD);
        
        powerups = List.of(playerSwapper, blockBreaker, shield);
        typeToPowerup = Map.of(
                Powerup.Type.PLAYER_SWAPPER, playerSwapper,
                Powerup.Type.BLOCK_BREAKER, blockBreaker,
                Powerup.Type.SHIELD, shield
        );
    }
    
    public PowerupManager(Main plugin, SpleefConfig config) {
        this.plugin = plugin;
        this.config = config;
    }
    
    public <T extends Participant> void start(Collection<T> newParticipants) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        participants = new HashMap<>(newParticipants.size());
        lastPowerupTimestamps = new HashMap<>(newParticipants.size());
        setUpPowerups();
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        startPowerupTimer();
    }
    
    private void initializeParticipant(Participant participant) {
        participants.put(participant.getUniqueId(), participant);
        for (Map.Entry<Powerup.Type, Integer> entry : config.getInitialLoadout().entrySet()) {
            Powerup.Type type = entry.getKey();
            int amount = entry.getValue();
            ItemStack powerup = typeToPowerup.get(type).getItem().asQuantity(amount);
            participant.getInventory().addItem(powerup);
        }
        lastPowerupTimestamps.put(participant.getUniqueId(), System.currentTimeMillis());
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        shouldGivePowerups = false;
        participants.clear();
        lastPowerupTimestamps.clear();
    }
    
    private void resetParticipant(Participant participant) {
        lastPowerupTimestamps.remove(participant.getUniqueId());
    }
    
    /**
     * if the participant isn't already in this manager, adds them to it. Otherwise, does nothing.
     * @param participant the participant to add
     */
    public void addParticipant(Participant participant) {
        if (participants.containsKey(participant.getUniqueId())) {
            return;
        }
        initializeParticipant(participant);
    }
    
    /**
     * if the participant is in this manager, removes them from it. Otherwise, does nothing. 
     * @param participant the participant to remove
     */
    public void removeParticipant(Participant participant) {
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
    }
    
    /**
     * @param shouldGivePowerups false means no powerups should be given, true means they should be given
     */
    public void setShouldGivePowerups(boolean shouldGivePowerups) {
        this.shouldGivePowerups = shouldGivePowerups;
    }
    
    /**
     * sets up the powerups using the default values and values from the config
     */
    private void setUpPowerups() {
        for (Powerup powerup : powerups) {
            powerup.setUserSound(config.getUserSound(powerup.getType()));
            powerup.setAffectedSound(config.getAffectedSound(powerup.getType()));
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(powerupTimerTaskId);
    }
    
    private void startPowerupTimer() {
        this.powerupTimerTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!shouldGivePowerups) {
                    return;
                }
                long currentTime = System.currentTimeMillis();
                for (Participant participant : participants.values()) {
                    if (canReceivePowerup(participant, currentTime)) {
                        randomlyGivePowerup(participant, Powerup.Source.GENERAL, currentTime);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * This may or may not give the participant a powerup based on the provided percent chance. The powerup given is random according to the weights provided in the config.
     * If the participant receives a powerup, their {@link PowerupManager#lastPowerupTimestamps} is reset.
     * @param participant the participant to receive a powerup
     * @param source the source from which to receive a powerup (null indicates any source)
     * @param currentTime the current system time in milliseconds
     */
    private void randomlyGivePowerup(@NotNull Participant participant, @NotNull Powerup.Source source, long currentTime) {
        if (random.nextDouble() < config.getChance(source)) {
            ItemStack powerup = getRandomPowerup(source);
            participant.getInventory().addItem(powerup);
            lastPowerupTimestamps.put(participant.getUniqueId(), currentTime);
        }
    }
    
    /**
     * @param source the source from which to receive a powerup (null indicates any source)
     * @return a random powerup item from the available powerups, according to the weights provided in the config
     */
    private @NotNull ItemStack getRandomPowerup(@Nullable Powerup.Source source) {
        Powerup.Type selectedType = MathUtils.getWeightedRandomValue(config.getPowerupWeights(source));
        Powerup selectedPowerup = typeToPowerup.get(selectedType);
        return selectedPowerup.getItem();
    }
    
    /**
     * @param participant the participant
     * @param currentTime the current system time in milliseconds
     * @return true if the participant is allowed to receive a powerup (e.g. they've met all requirements)
     */
    private boolean canReceivePowerup(Participant participant, long currentTime) {
        long lastPowerupTimestamp = lastPowerupTimestamps.get(participant.getUniqueId());
        boolean enoughTimeHasPassed = currentTime - lastPowerupTimestamp >= config.getMinTimeBetween();
        return enoughTimeHasPassed && !hasMaxPowerups(participant);
    }
    
    /**
     * @param participant the participant
     * @return true if the participant has the maximum number of powerups allowed in their inventory, false if not. 
     */
    private boolean hasMaxPowerups(Participant participant) {
        if (config.getMaxPowerups() < 0) {
            return false;
        }
        int num = 0;
        for (ItemStack item : participant.getInventory().getContents()) {
            if (item != null && isPowerup(item)) {
                num += item.getAmount();
            }
        }
        return num >= config.getMaxPowerups();
    }
    
    public void onParticipantBreakBlock(@NotNull Participant participant) {
        if (!shouldGivePowerups) {
            return;
        }
        if (!canReceivePowerup(participant, System.currentTimeMillis())) {
            return;
        }
        randomlyGivePowerup(participant, Powerup.Source.BREAK_BLOCK, System.currentTimeMillis());
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) {
            return;
        }
        if (!(snowball.getShooter() instanceof Player player)) {
            return;
        }
        Participant participant = participants.get(player.getUniqueId());
        if (participant == null) {
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
            case PLAYER_SWAPPER -> snowball.setMetadata(
                    POWERUP_METADATA_KEY, 
                    new FixedMetadataValue(plugin, PLAYER_SWAPPER_METADATA_VALUE));
            case BLOCK_BREAKER -> snowball.setMetadata(
                    POWERUP_METADATA_KEY, 
                    new FixedMetadataValue(plugin, BLOCK_BREAKER_METADATA_VALUE));
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
        if (!participants.containsKey(shooter.getUniqueId())) {
            return;
        }
        List<MetadataValue> metadata = snowball.getMetadata(POWERUP_METADATA_KEY);
        if (metadata.isEmpty()) {
            return;
        }
        String powerupType = metadata.getFirst().asString();
        if (event.getHitEntity() instanceof Player target) {
            onProjectileHitPlayer(shooter, target, powerupType);
            return;
        }
        Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            onProjectileHitBlock(shooter, hitBlock, powerupType);
        }
    }
    
    /**
     * Performs necessary actions when a powerup projectile entity hits a player
     * @param shooter the shooter of the projectile entity
     * @param target the player who was hit by the projectile entity
     * @param powerupTypeMetadataValue the metadata about the projectile entity which was used to hit a player
     */
    private void onProjectileHitPlayer(@NotNull Player shooter, @NotNull Player target, @NotNull String powerupTypeMetadataValue) {
        if (!participants.containsKey(target.getUniqueId())) {
            return;
        }
        if (!powerupTypeMetadataValue.equals(PLAYER_SWAPPER_METADATA_VALUE)) {
            return;
        }
        if (hasShield(target)) {
            useShield(shooter, target);
        } else {
            swapPlayers(shooter, target);
        }
    }
    
    /**
     * Performs necessary actions when a powerup projectile entity hits a block
     * @param powerupTypeMetadataValue the metadata about the projectile entity which was used to hit a block
     * @param hitBlock the block that was hit by the projectile entity
     */
    private void onProjectileHitBlock(Player shooter, @NotNull Block hitBlock, @NotNull String powerupTypeMetadataValue) {
        Material hitBlockType = hitBlock.getType();
        if (!hitBlockType.equals(config.getLayerBlock()) && !hitBlockType.equals(config.getDecayBlock())) {
            return;
        }
        if (!powerupTypeMetadataValue.equals(BLOCK_BREAKER_METADATA_VALUE)) {
            return;
        }
        hitBlock.setType(Material.AIR);
        Powerup blockBreaker = typeToPowerup.get(Powerup.Type.BLOCK_BREAKER);
        if (blockBreaker.getAffectedSound() != null) {
            config.getWorld().playSound(blockBreaker.getAffectedSound(), hitBlock.getX(), hitBlock.getY(), hitBlock.getZ());
        }
        if (blockBreaker.getUserSound() != null) {
            shooter.playSound(blockBreaker.getUserSound());
        }
    }
    
    
    /**
     * @param participant the participant
     * @return true if the player has a shield on in their inventory
     */
    private boolean hasShield(Player participant) {
        for (ItemStack item : participant.getInventory().getContents()) {
            if (isPowerup(item, Powerup.Type.SHIELD)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes a shield from the target's inventory, and plays both involved players the appropriate sound
     * @param shooter the player who was thwarted by the shield 
     * @param target the player who had a shield and used it
     */
    private void useShield(Player shooter, Player target) {
        target.getInventory().removeItemAnySlot(typeToPowerup.get(Powerup.Type.SHIELD).getItem());
        Powerup shield = typeToPowerup.get(Powerup.Type.SHIELD);
        if (shield.getUserSound() != null) {
            target.playSound(shield.getUserSound());
        }
        if (shield.getAffectedSound() != null) {
            shooter.playSound(shield.getAffectedSound());
        }
    }
    
    private void swapPlayers(Player shooter, Player target) {
        Location shooterLoc = shooter.getLocation();
        Location targetLoc = target.getLocation();
        shooter.teleport(targetLoc);
        target.teleport(shooterLoc);
        Powerup playerSwapper = typeToPowerup.get(Powerup.Type.PLAYER_SWAPPER);
        if (playerSwapper.getUserSound() != null) {
            target.playSound(playerSwapper.getUserSound());
        }
        if (playerSwapper.getAffectedSound() != null) {
            shooter.playSound(playerSwapper.getAffectedSound());
        }
    }
    
    /**
     *
     * @param item the item which might be a powerup of the given type
     * @param type the type of powerup to check for
     * @return true if the given item is the given type of powerup 
     */
    private boolean isPowerup(@Nullable ItemStack item, @NotNull Powerup.Type type) {
        if (item == null) {
            return false;
        }
        Powerup shield = itemToPowerup(item);
        if (shield == null) {
            return false;
        }
        return shield.getType().equals(type);
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
