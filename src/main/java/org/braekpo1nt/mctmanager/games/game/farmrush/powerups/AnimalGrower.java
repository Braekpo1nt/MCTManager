package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Animals;

import java.util.*;

public class AnimalGrower {
    
    protected static final Random random = new Random();
    /**
     * the world the powerup is in
     */
    protected final World world;
    /**
     * The location of the powerup in the world
     */
    protected final Location location;
    protected final double radius;
    private final Map<UUID, Animals> affectedEntities = new HashMap<>();
    /**
     * a growable mob's age is multiplied by this factor. To grow faster,
     * make it a number less than 1. E.g. a mob takes 20 ticks to grow, and
     * ageFactor is .75, it will take 15 ticks to grow when within the
     * {@link #radius}
     */
    private final double ageMultiplier;
    /**
     * Works the same as {@link #ageMultiplier}, but for the breeding cooldown
     */
    private final double breedMultiplier;
    
    public AnimalGrower(Location location, double radius, double ageMultiplier, double breedMultiplier) {
        this.world = location.getWorld();
        this.location = location;
        this.radius = radius;
        this.ageMultiplier = ageMultiplier;
        this.breedMultiplier = breedMultiplier;
    }
    
    /**
     * Speeds up new animals in the radius, slows down animals no longer in the radius
     */
    public void updateAnimals() {
        
        Collection<Animals> animalsInRange = world.getNearbyEntitiesByType(Animals.class, location, radius, animal -> animal.getAge() != 0);
        // cows that have just walked into the radius should have their age multiplied, and breed time reduced
        // cows that are already in the radius before this scan should be unchanged
        // cows that have just walked out of the radius should have their age un-multiplied
        
        // to speed up baby aging, take the negative age and make it closer to 0 (less negative, i.e. larger)
        // to speed up breed cooldown, take the positive age and make it closer to 0 (i.e. smaller)
        List<Animals> existingAnimals = affectedEntities.values().stream().toList();
        for (Animals animal : existingAnimals) {
            if (animal.getAge() == 0) {
                affectedEntities.remove(animal.getUniqueId());
            } else if (!animalsInRange.contains(animal)) {
                // this animal is no longer in range
                affectedEntities.remove(animal.getUniqueId());
                deBuffAnimal(animal);
            }
        }
        
        for (Animals animal : animalsInRange) {
            if (!affectedEntities.containsKey(animal.getUniqueId())) {
                buffAnimal(animal);
                affectedEntities.put(animal.getUniqueId(), animal);
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
    
    private void buffAnimal(Animals animal) {
        int oldAge = animal.getAge();
        if (animal.isAdult()) {
            // make adults breed faster
            animal.setAge((int) (oldAge * breedMultiplier));
//            Main.logger().info(String.format("Breed faster: %d->%d", oldAge, (int) (oldAge * breedMultiplier)));
        } else {
            // make babies grow up faster
            animal.setAge((int) (oldAge * ageMultiplier));
//            Main.logger().info(String.format("Age faster: %d->%d", oldAge, (int) (oldAge * ageMultiplier)));
        }
    }
    
    private void deBuffAnimal(Animals animal) {
        int oldAge = animal.getAge();
        if (animal.isAdult()) {
            // make adults breed normally
            animal.setAge((int) (oldAge / breedMultiplier));
//            Main.logger().info(String.format("Breed slower: %d->%d", oldAge, (int) (oldAge / breedMultiplier)));
        } else {
            // make babies age normally
            animal.setAge((int) (oldAge / ageMultiplier));
//            Main.logger().info(String.format("Age slower: %d->%d", oldAge, (int) (oldAge / ageMultiplier)));
        }
    }
    
    public PowerupType getType() {
        return PowerupType.ANIMAL_GROWER;
    }
}
