package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.CropGrower;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.Powerup;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

/**
 * Changes the speed of crop growth in a range
 */
@Data
@Builder
public class CropGrowerSpec implements PowerupSpec {
    /**
     * the item used to place the powerup
     */
    private final @NotNull ItemStack item;
    private final @NotNull Powerup.Type type;
    private final @NotNull Recipe recipe;
    /**
     * How far the effects reach
     */
    private double radius;
    private NamespacedKey recipeKey;
    private int count;
    private double probability;
    
    @Override
    public Powerup createPowerup(Location location) {
        return new CropGrower(location, radius, count, probability);
    }
}
