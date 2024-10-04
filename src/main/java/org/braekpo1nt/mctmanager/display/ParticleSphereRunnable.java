package org.braekpo1nt.mctmanager.display;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ParticleSphereRunnable extends BukkitRunnable {
    
    private final Location center;
    private final double radius;
    private final int particlesPerSpawn;
    private final int count;
    private final long duration;
    private final Random random = new Random();
    
    private long timeElapsed = 0;
    
    // Constructor for the ParticleSphereRunnable
    public ParticleSphereRunnable(Location center, double radius, int particlesPerSpawn, long duration, int count) {
        this.center = center;
        this.radius = radius;
        this.particlesPerSpawn = particlesPerSpawn;
        this.duration = duration;
        this.count = count;
    }
    
    @Override
    public void run() {
        // Check if the duration has been reached
        timeElapsed++;
        if (timeElapsed >= duration) {
            this.cancel();
            return;
        }
        
        // World of the particle effect
        World world = center.getWorld();
        
        // Spawn the defined number of particles
        for (int i = 0; i < particlesPerSpawn; i++) {
            // Generate random coordinates within a sphere
            double xOffset = (random.nextDouble() * 2 - 1) * radius;
            double yOffset = (random.nextDouble() * 2 - 1) * radius;
            double zOffset = (random.nextDouble() * 2 - 1) * radius;
            
            // Ensure the point is within the sphere
            if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset <= radius * radius) {
                // Add the random offset to the center location
                Location particleLocation = center.clone().add(xOffset, yOffset, zOffset);
                
                // Spawn the particle
                world.spawnParticle(Particle.HAPPY_VILLAGER, particleLocation.getX(), particleLocation.getY(), particleLocation.getZ(), count);
            }
        }
    }
}
