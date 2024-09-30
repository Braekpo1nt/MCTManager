package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.AnimalGrower;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.Powerup;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

/**
 * Makes animals in its radius grow and breed faster
 */
@Data
@Builder
public class AnimalGrowerSpec implements PowerupSpec {
    private final @NotNull Powerup.Type type = Powerup.Type.ANIMAL_GROWER;
    
    private final @NotNull ItemStack item;
    private final @NotNull Recipe recipe;
    private double radius;
    private NamespacedKey recipeKey;
    
    /**
     * how many seconds between scans
     */
    private int seconds;
    /**
     * a growable mob's age is multiplied by this factor. To grow faster,
     * make it a number less than 1. E.g. a mob takes 20 ticks to grow, and
     * ageFactor is .75, it will take 15 ticks to grow when within the
     * {@link #radius}
     */
    private double ageMultiplier;
    /**
     * Works the same as {@link #ageMultiplier}, but for the breeding cooldown
     */
    private double breedMultiplier;
    
    @Override
    public Powerup createPowerup(Location location) {
        return new AnimalGrower(location, radius, seconds, ageMultiplier, breedMultiplier);
    }
}
