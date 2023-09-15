package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ChaosManager implements Listener {
    private final BlockData sandBlockData = Material.SAND.createBlockData();
    private final BlockData anvilBlockData = Material.ANVIL.createBlockData();
    private final ClockworkStorageUtil storageUtil;
    private double minArrows;
    private double maxArrows;
    private double minFallingBlocks;
    private double maxFallingBlocks;
    private double minDelay;
    private double maxDelay;
    private int scheduleArrowsSummonTaskId;
    private final Random random = new Random();
    
    private final Main plugin;
    
    public ChaosManager(Main plugin, ClockworkStorageUtil storageUtil) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
        minArrows = storageUtil.getChaos().arrows().initial().min();
        maxArrows = storageUtil.getChaos().arrows().initial().max();
        minFallingBlocks = storageUtil.getChaos().fallingBlocks().initial().min();
        maxFallingBlocks = storageUtil.getChaos().fallingBlocks().initial().max();
        minDelay += storageUtil.getChaos().summonDelay().initial().min();
        maxDelay += storageUtil.getChaos().summonDelay().initial().max();
    }
    
    public void incrementChaos() {
        minArrows += storageUtil.getChaos().arrows().increment().min();
        maxArrows += storageUtil.getChaos().arrows().increment().max();
        if (minArrows < 0) {
            minArrows = 0;
        }
        if (((int) minArrows) >= ((int) maxArrows + 1)) {
            maxArrows = minArrows;
        }
        
        minFallingBlocks += storageUtil.getChaos().fallingBlocks().increment().min();
        maxFallingBlocks += storageUtil.getChaos().fallingBlocks().increment().max();
        if (minFallingBlocks < 0) {
            minFallingBlocks = 0;
        }
        if ((int) minFallingBlocks >= ((int) maxFallingBlocks + 1)) {
            maxFallingBlocks = minFallingBlocks;
        }
        
        minDelay -= storageUtil.getChaos().summonDelay().decrement().min();
        if (((long) minDelay) < 5.0) {
            minDelay = 5.0;
        }
        maxDelay -= storageUtil.getChaos().summonDelay().decrement().max();
        if (((long) minDelay) >= ((long) maxDelay + 1)) {
            maxDelay = minDelay;
        }
    }
    
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        scheduleSummonTask();
    }
    
    public void stop() {
        // the order of these three lines is very important
        cancelAllTasks();
        removeArrowsAndFallingBlocks();
        HandlerList.unregisterAll(this);
        // the order of the above three lines is very important
        // cancel tasks that create FallingBlock entities, then kill all falling block entities, then cancel the listener that causes FallingBlock entities to be removed a few moments after they have landed. 
    }
    
    private void scheduleSummonTask() {
        if (((long) minDelay) >= ((long) maxDelay + 1)) {
            return;
        }
        long randomDelay = random.nextLong((long) minDelay, (long) maxDelay + 1);
        this.scheduleArrowsSummonTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                summonArrows();
                summonFallingBlocks();
                scheduleSummonTask();
            }
        }.runTaskLater(plugin, randomDelay).getTaskId();
    }
    
    private void summonArrows() {
        if (minArrows < 0 || ((int) minArrows) >= ((int) maxArrows + 1)) {
            return;
        }
        int numArrows = random.nextInt((int) minArrows, (int) (maxArrows + 1));
        for (int i = 0; i < numArrows; i++) {
            Location spawnLocation = randomLocationInCylinder();
            if (spawnLocation.getBlock().getType().equals(Material.AIR)) {
                float arrowSpeed = random.nextFloat(storageUtil.getChaos().arrowSpeed().min(), storageUtil.getChaos().arrowSpeed().max());
                float arrowSpread = random.nextFloat(storageUtil.getChaos().arrowSpread().min(), storageUtil.getChaos().arrowSpread().max());
                Arrow arrow = storageUtil.getWorld().spawnArrow(spawnLocation, new Vector(0, -1, 0), arrowSpeed, arrowSpread);
                arrow.setGravity(true);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
        }
    }
    
    private void summonFallingBlocks() {
        if (minFallingBlocks < 0 || (int) minFallingBlocks >= ((int) maxFallingBlocks + 1)) {
            return;
        }
        int numOfFallingBlocks = random.nextInt((int) minFallingBlocks, (int) maxFallingBlocks + 1);
        for (int i = 0; i < numOfFallingBlocks; i++) {
            Location spawnLocation = randomLocationInCylinder();
            if (spawnLocation.getBlock().getType().equals(Material.AIR)) {
                FallingBlock fallingBlock = storageUtil.getWorld().spawnFallingBlock(spawnLocation, random.nextBoolean() ? sandBlockData : anvilBlockData);
                fallingBlock.setDropItem(false);
            }
        }
    }
    
    @NotNull
    private Location randomLocationInCylinder() {
        double randomRadius = storageUtil.getChaos().cylinder().radius() * Math.sqrt(random.nextDouble()); // generate a "uniformly random" radius (see https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly)
        double randomAngle = random.nextDouble() * 2 * Math.PI;
        double x = storageUtil.getChaos().cylinder().centerX() + randomRadius * Math.cos(randomAngle);
        double z = storageUtil.getChaos().cylinder().centerZ() + randomRadius * Math.sin(randomAngle);
        double y = random.nextDouble(storageUtil.getChaos().cylinder().spawnY().min(), storageUtil.getChaos().cylinder().spawnY().max());
        return new Location(storageUtil.getWorld(), x, y, z);
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.ARROW) {
            Arrow arrow = (Arrow) event.getEntity();
            Bukkit.getScheduler().runTaskLater(plugin, arrow::remove, 5L);
        }
    }
    
    @EventHandler
    public void onFallingBlockEntityLand(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            Material material = fallingBlock.getBlockData().getMaterial();
            switch (material) {
                case SAND, ANVIL -> Bukkit.getScheduler().runTaskLater(plugin, () -> event.getBlock().setType(Material.AIR), 5L);
            }
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(scheduleArrowsSummonTaskId);
    }
    
    private void removeArrowsAndFallingBlocks() {
        for (Entity entity : getEntitiesInCylinder(storageUtil.getWorld(), storageUtil.getChaos().cylinder().centerX(), storageUtil.getChaos().cylinder().centerZ(), storageUtil.getChaos().cylinder().radius(), Arrow.class, FallingBlock.class)) {
            entity.remove();
        }
    }
    
    public static List<Entity> getEntitiesInCylinder(World world, double centerX, double centerZ, double radius, Class<?>... types) {
        Collection<Entity> entities = world.getEntitiesByClasses(types);
        List<Entity> entitiesInCylinder = new ArrayList<>();
        
        double radiusSquared = radius * radius;
        for (Entity entity : entities) {
            double entityX = entity.getLocation().getX();
            double entityZ = entity.getLocation().getZ();
            
            double distanceSquared = (entityX - centerX) * (entityX - centerX) + (entityZ - centerZ) * (entityZ - centerZ);
            
            if (distanceSquared <= radiusSquared) {
                entitiesInCylinder.add(entity);
            }
        }
        Bukkit.getLogger().info(String.format("%s entities in cylinder (%s, %s, %s)", entitiesInCylinder.size(), centerX, centerZ, radius));
        return entitiesInCylinder;
    }
}
