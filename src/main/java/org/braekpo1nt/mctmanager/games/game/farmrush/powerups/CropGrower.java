package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;


public class CropGrower {
    private static final Random random = new Random();
    
    /**
     * the world the powerup is in
     */
    protected final World world;
    /**
     * The location of the powerup in the world
     */
    protected final Location location;
    protected final double radius;
    /**
     * the probability per {@link #growAttempt()} of a crop increasing in age
     */
    private final double growthChance;
    
    /**
     * 
     * @param location the location of the crop grower
     * @param radius the radius of effect
     * @param growthChance the chance per cycle of a crop growing to the next level
     */
    public CropGrower(Location location, double radius, double growthChance) {
        this.world = location.getWorld();
        this.location = location;
        this.radius = radius;
        this.growthChance = growthChance;
    }
    
    public PowerupType getType() {
        return PowerupType.CROP_GROWER;
    }
    
    public void growAttempt() {
        List<Block> crops = detectCrops();
        for (Block crop : crops) {
            if (crop.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    if (random.nextDouble() <= growthChance) {
                        ageable.setAge(ageable.getAge() + 1);
                        crop.setBlockData(ageable);
                    }
                }
            }
        }
    }
    
    /**
     * Spawns random particles in the radius of the animal grower
     * @param numOfParticles the number of times to spawn a particle group
     * @param particle the particle type to spawn
     * @param count the number of units in each particle group
     */
    public void displayRadius(int numOfParticles, Particle particle, int count) {
        for (int i = 0; i < numOfParticles; i++) {
            // Generate random coordinates within a sphere (location + offset)
            double xOffset = location.x() + (random.nextDouble() * 2 - 1) * radius;
            double yOffset = location.y() + (random.nextDouble() * 2 - 1) * radius;
            double zOffset = location.z() + (random.nextDouble() * 2 - 1) * radius;
            
            world.spawnParticle(particle, xOffset, yOffset, zOffset, count);
        }
    }
    
    private @NotNull List<Block> detectCrops() {
        return BlockPlacementUtils.getBlocksInRadius(location, radius, Tag.CROPS);
    }
}

