package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;


import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;

import java.util.*;

public class AnimalGrower extends Powerup {
    
    private final Map<UUID, Ageable> affectedEntities = new HashMap<>();
    /**
     * how many cycles before running a probability check
     */
    private final int interval;
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
    /**
     * number of {@link #performAction()} cycles before running a probability check
     */
    private int count;
    
    public AnimalGrower(Location location, double radius, int interval, double ageMultiplier, double breedMultiplier) {
        super(location.getWorld(), location, radius);
        this.interval = interval;
        this.ageMultiplier = ageMultiplier;
        this.breedMultiplier = breedMultiplier;
    }
    
    @Override
    public void performAction() {
        if (count > 0) {
            count--;
            return;
        }
        count = interval;
        
        Collection<Ageable> newAgeables = world.getNearbyEntitiesByType(Ageable.class, location, radius, ageable -> !ageable.isAdult());
        Main.logger().info(String.format("Detected %s ageables", newAgeables.size()));
        // cows that have just walked into the radius should have their age multiplied
        // cows that are already in the radius before this scan should be unchanged
        // cows that have just walked out of the radius should have their age un-multiplied
        for (Ageable ageable : newAgeables) {
            if (!affectedEntities.containsKey(ageable.getUniqueId())) {
                int oldAge = ageable.getAge();
                int newAge = (int) (oldAge * ageMultiplier);
                ageable.setAge(newAge);
                affectedEntities.put(ageable.getUniqueId(), ageable);
                Main.logger().info(String.format("IN %d->%d - %s", oldAge, newAge, ageable.getUniqueId()));
            }
        }
        
       // if an affected entity is not in the radius, we must reset them
        for (Ageable ageable : affectedEntities.values()) {
            if (!newAgeables.contains(ageable)) {
                int oldAge = ageable.getAge();
                int newAge = (int) (oldAge / ageMultiplier);
                ageable.setAge(newAge);
                affectedEntities.remove(ageable.getUniqueId());
                Main.logger().info(String.format("OUT %d->%d - %s", oldAge, newAge, ageable.getUniqueId()));
            }
        }
        
    }
    
    @Override
    public Type getType() {
        return Type.ANIMAL_GROWER;
    }
}
