package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    private final ClockworkConfig config;
    private double minArrows;
    private double maxArrows;
    private double minFallingBlocks;
    private double maxFallingBlocks;
    private double minDelay;
    private double maxDelay;
    private int summonTaskId;
    private boolean paused;
    private final Random random = new Random();
    
    private final Main plugin;
    
    public ChaosManager(Main plugin, ClockworkConfig config) {
        this.plugin = plugin;
        this.config = config;
        paused = true;
    }
    
    private void initializeValues() {
        minArrows = config.getChaos().arrows().initial().min();
        maxArrows = config.getChaos().arrows().initial().max();
        minFallingBlocks = config.getChaos().fallingBlocks().initial().min();
        maxFallingBlocks = config.getChaos().fallingBlocks().initial().max();
        minDelay += config.getChaos().summonDelay().initial().min();
        maxDelay += config.getChaos().summonDelay().initial().max();
    }
    
    public void incrementChaos() {
        minArrows += config.getChaos().arrows().increment().min();
        maxArrows += config.getChaos().arrows().increment().max();
        if (minArrows < 0) {
            minArrows = 0;
        }
        if (((int) minArrows) >= ((int) maxArrows + 1)) {
            maxArrows = minArrows;
        }
        
        minFallingBlocks += config.getChaos().fallingBlocks().increment().min();
        maxFallingBlocks += config.getChaos().fallingBlocks().increment().max();
        if (minFallingBlocks < 0) {
            minFallingBlocks = 0;
        }
        if ((int) minFallingBlocks >= ((int) maxFallingBlocks + 1)) {
            maxFallingBlocks = minFallingBlocks;
        }
        
        minDelay -= config.getChaos().summonDelay().decrement().min();
        if (((long) minDelay) < 5.0) {
            minDelay = 5.0;
        }
        maxDelay -= config.getChaos().summonDelay().decrement().max();
        if (((long) minDelay) >= ((long) maxDelay + 1)) {
            maxDelay = minDelay;
        }
    }
    
    /**
     * Start the chaos manager
     * @param paused true if this should start in a paused state, false otherwise
     */
    public void start(boolean paused) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeValues();
        this.paused = paused;
        scheduleSummonTask();
    }
    
    /**
     * Pause all chaos
     */
    public void pause() {
        this.paused = true;
    }
    
    /**
     * Resume all chaos
     */
    public void resume() {
        this.paused = false;
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
        this.summonTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!paused) {
                    summonArrows();
                    summonFallingBlocks();
                }
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
                float arrowSpeed = random.nextFloat(config.getChaos().arrowSpeed().min(), config.getChaos().arrowSpeed().max());
                float arrowSpread = random.nextFloat(config.getChaos().arrowSpread().min(), config.getChaos().arrowSpread().max());
                Arrow arrow = config.getWorld().spawnArrow(spawnLocation, new Vector(0, -1, 0), arrowSpeed, arrowSpread);
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
                FallingBlock fallingBlock = config.getWorld().spawn(spawnLocation, FallingBlock.class);
                fallingBlock.setBlockData(random.nextBoolean() ? sandBlockData : anvilBlockData);
                fallingBlock.setDropItem(false);
            }
        }
    }
    
    @NotNull
    private Location randomLocationInCylinder() {
        double randomRadius = config.getChaos().cylinder().radius() * Math.sqrt(random.nextDouble()); // generate a "uniformly random" radius (see https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly)
        double randomAngle = random.nextDouble() * 2 * Math.PI;
        double x = config.getChaos().cylinder().centerX() + randomRadius * Math.cos(randomAngle);
        double z = config.getChaos().cylinder().centerZ() + randomRadius * Math.sin(randomAngle);
        double y = random.nextDouble(config.getChaos().cylinder().spawnY().min(), config.getChaos().cylinder().spawnY().max());
        return new Location(config.getWorld(), x, y, z);
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
        Bukkit.getScheduler().cancelTask(summonTaskId);
    }
    
    private void removeArrowsAndFallingBlocks() {
        for (Entity entity : getEntitiesInCylinder(config.getWorld(), config.getChaos().cylinder().centerX(), config.getChaos().cylinder().centerZ(), config.getChaos().cylinder().radius(), Arrow.class, FallingBlock.class)) {
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
        return entitiesInCylinder;
    }
}
