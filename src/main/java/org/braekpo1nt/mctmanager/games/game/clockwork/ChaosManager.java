package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ChaosManager implements Listener {
    private int minArrows = 1;
    private int maxArrows = 5;
    private long minDelay = 15L;
    private long maxDelay = 30L;
    private final Location center;
    private final int radius = 23;
    private final int minY = 7;
    private final int maxY = 13;
    private int scheduleArrowsSummonTaskId;
    private final Random random = new Random();
    
    private final Main plugin;
    
    public ChaosManager(Main plugin, Location center) {
        this.plugin = plugin;
        this.center = center;
    }
    
    public void incrementChaos() {
        minArrows += 3;
        maxArrows += 4;
        
        minDelay -= 3L;
        if (minDelay < 5L) {
            minDelay = 5L;
        }
        maxDelay -= 4L;
        if (maxDelay < 10L) {
            maxDelay = 10L;
        }
        Bukkit.getLogger().info(String.format("Delay[%s, %s] - Arrows[%s, %s]", minDelay, maxDelay, minArrows, maxArrows));
    }
    
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        scheduleArrowSummonTask();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        removeAllArrowsInCylinder();
    }
    
    private void scheduleArrowSummonTask() {
        long randomDelay = random.nextLong(minDelay, maxDelay);
        this.scheduleArrowsSummonTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                summonArrows();
                scheduleArrowSummonTask();
            }
        }.runTaskLater(plugin, randomDelay).getTaskId();
    }
    
    private void summonArrows() {
        int numArrows = random.nextInt(minArrows, maxArrows + 1);
        World world = center.getWorld();
        for (int i = 0; i < numArrows; i++) {
            double randomRadius = radius * Math.sqrt(random.nextDouble()); // generate a "uniformly random" radius (see https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly)
            double randomAngle = random.nextDouble() * 2 * Math.PI;
            double x = center.getX() + randomRadius * Math.cos(randomAngle);
            double z = center.getZ() + randomRadius * Math.sin(randomAngle);
            double y = random.nextDouble(minY, maxY);
            Location spawnLocation = new Location(world, x, y, z);
            if (spawnLocation.getBlock().getType().equals(Material.AIR)) {
                Arrow arrow = world.spawnArrow(spawnLocation, new Vector(0, -1, 0), 0.6f, 12);
                arrow.setGravity(true);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
        }
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
    
    private void removeAllArrowsInCylinder() {
        World world = center.getWorld();
        for (Arrow arrow : world.getNearbyEntitiesByType(Arrow.class, center, radius)) {
            arrow.remove();
        }
    }
}
