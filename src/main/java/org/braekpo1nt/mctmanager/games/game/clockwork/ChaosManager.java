package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class ChaosManager implements Listener {
    private int minArrows = 1;
    private int maxArrows = 1;
    private long minDelay = 40L;
    private long maxDelay = 80L;
    private Location center;
    private int radius = 22;
    private int minY = 7;
    private int maxY = 13;
    private int arrowsTaskId;
    private final Random random = new Random();
    
    private final Main plugin;
    
    public ChaosManager(Main plugin, Location center) {
        this.plugin = plugin;
        this.center = center;
    }
    
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.arrowsTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::summonArrows, 0L, random.nextLong() % (maxDelay - minDelay + 1) + minDelay).getTaskId();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTask(arrowsTaskId);
        removeAllArrowsInCylinder();
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.ARROW) {
            Arrow arrow = (Arrow) event.getEntity();
            Bukkit.getScheduler().runTaskLater(plugin, arrow::remove, 10L);
        }
    }
    
    private void summonArrows() {
        int numArrows = new Random().nextInt(maxArrows - minArrows + 1) + minArrows;
        World world = center.getWorld();
        for (int i = 0; i < numArrows; i++) {
            double randomRadius = radius * Math.sqrt(random.nextDouble()); // generate a "uniformly random" radius (see https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly)
            double randomAngle = random.nextDouble() * 2 * Math.PI;
            double x = center.getX() + randomRadius * Math.cos(randomAngle);
            double z = center.getZ() + randomRadius * Math.sin(randomAngle);
            double y = Math.random() * (maxY - minY + 1) + minY; // Random y within the defined range
            Location spawnLocation = new Location(world, x, y, z);
            if (spawnLocation.getBlock().getType().equals(Material.AIR)) {
                Arrow arrow = world.spawnArrow(spawnLocation, new Vector(0, -1, 0), 0, 0);
                arrow.setGravity(true);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
        }
    }
    
    private void removeAllArrowsInCylinder() {
        World world = center.getWorld();
        for (Arrow arrow : world.getNearbyEntitiesByType(Arrow.class, center, radius)) {
            arrow.remove();
        }
    }
    
    private void scheduleArrowSummon() {
        this.arrowsTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int numArrows = random.nextInt(maxArrows - minArrows + 1) + minArrows;
            World world = center.getWorld();
            for (int i = 0; i < numArrows; i++) {
                double angle = Math.random() * Math.PI * 2;
                double x = center.getX() + Math.cos(angle) * radius;
                double z = center.getZ() + Math.sin(angle) * radius;
                double y = Math.random() * (maxY - minY + 1) + minY;
                Location spawnLocation = new Location(world, x, y, z);
                world.spawnEntity(spawnLocation, EntityType.ARROW);
            }
        }, minDelay, random.nextLong() % (maxDelay - minDelay + 1) + minDelay).getTaskId();
    }
}
