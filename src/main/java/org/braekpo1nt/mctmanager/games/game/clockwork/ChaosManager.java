package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.block.BlockState;
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
    private final World world;
    private int minArrows = 10;//3;
    private int maxArrows = 10;//7;
    private int minFallingBlocks = 5;//0;
    private int maxFallingBlocks = 5;//0;
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
        world = this.center.getWorld();
    }
    
    public void incrementChaos() {
        minArrows += 3;
        maxArrows += 4;
        minFallingBlocks += 1;
        maxFallingBlocks += 2;
    
        minDelay -= 3L;
        if (minDelay < 5L) {
            minDelay = 5L;
        }
        maxDelay -= 4L;
        if (maxDelay < 10L) {
            maxDelay = 10L;
        }
        
//        Bukkit.getLogger().info(String.format("Delay[%s, %s] - Arrows[%s, %s]", minDelay, maxDelay, minArrows, maxArrows));
    }
    
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        scheduleSummonTask();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        clearChaos();
    }
    
    private void scheduleSummonTask() {
        long randomDelay = random.nextLong(minDelay, maxDelay);
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
        if (maxArrows <= 0) {
            return;
        }
        int numArrows = random.nextInt(minArrows, maxArrows + 1);
        for (int i = 0; i < numArrows; i++) {
            Location spawnLocation = randomLocationInCylinder();
            if (spawnLocation.getBlock().getType().equals(Material.AIR)) {
                Arrow arrow = world.spawnArrow(spawnLocation, new Vector(0, -1, 0), 0.6f, 12);
                arrow.setGravity(true);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
        }
    }
    
    @NotNull
    private Location randomLocationInCylinder() {
        double randomRadius = radius * Math.sqrt(random.nextDouble()); // generate a "uniformly random" radius (see https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly)
        double randomAngle = random.nextDouble() * 2 * Math.PI;
        double x = center.getX() + randomRadius * Math.cos(randomAngle);
        double z = center.getZ() + randomRadius * Math.sin(randomAngle);
        double y = random.nextDouble(minY, maxY);
        return new Location(world, x, y, z);
    }
    
    private void summonFallingBlocks() {
        if (maxFallingBlocks <= 0) {
            return;
        }
        int numOfFallingBlocks = random.nextInt(minFallingBlocks, maxFallingBlocks + 1);
        for (int i = 0; i < numOfFallingBlocks; i++) {
            Location spawnLocation = randomLocationInCylinder();
            if (spawnLocation.getBlock().getType().equals(Material.AIR)) {
                FallingBlock fallingBlock = world.spawnFallingBlock(spawnLocation, random.nextBoolean() ? sandBlockData : anvilBlockData);
                fallingBlock.setDropItem(false);
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
    
    private void clearChaos() {
        removeArrowsAndFallingBlocks();
//        removeSandAndAnvils();
    }
    
    private void removeSandAndAnvils() {
        List<Chunk> chunksInRadius = getChunksInRadius(center, radius);
        for (Chunk chunk : chunksInRadius) {
            Collection<BlockState> sandAndAnvils = chunk.getTileEntities(block -> block.getType().equals(Material.SAND) || block.getType().equals(Material.ANVIL), false);
            for (BlockState sandAndAnvil : sandAndAnvils) {
                sandAndAnvil.setType(Material.AIR);
            }
        }
    }
    
    private void removeArrowsAndFallingBlocks() {
        for (Entity entity : getEntitiesInCylinder(world, center.x(), center.z(), radius, Arrow.class, FallingBlock.class)) {
            entity.remove();
        }
    }
    
    public static List<Chunk> getChunksInRadius(Location center, int radius) {
        World centerWorld = center.getWorld();
        int centerX = center.getBlockX() >> 4; // Convert block coordinates to chunk coordinates
        int centerZ = center.getBlockZ() >> 4;
        List<Chunk> chunks = new ArrayList<>();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                Chunk chunk = centerWorld.getChunkAt(x, z);
                chunks.add(chunk);
            }
        }
        Bukkit.getLogger().info(String.format("%s chunks", chunks.size()));
        
        return chunks;
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
