package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.AnimalGrower;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Makes animals in its radius grow and breed faster
 */
@Data
@Builder
public class AnimalGrowerSpec implements PowerupSpec {
    private final @NotNull PowerupType type = PowerupType.ANIMAL_GROWER;
    
    private final @NotNull Recipe recipe;
    private double radius;
    private NamespacedKey recipeKey;
    
    /**
     * how many cycles between scans for animals
     * Defaults to 5
     */
    @Builder.Default
    private int scanCycles = 5;
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
    
    public AnimalGrower createPowerup(Location location) {
        return new AnimalGrower(location, radius, scanCycles, ageMultiplier, breedMultiplier);
    }
    
    @Override
    public boolean isItem(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        return PowerupManager.animalGrowerItem.getItemMeta().equals(itemMeta);
    }
}
