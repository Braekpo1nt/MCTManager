package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.CropGrower;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.Powerup;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Changes the speed of crop growth in a range
 */
@Data
public class CropGrowerSpec implements PowerupSpec {
    /**
     * the item used to place the powerup
     */
    private final @NotNull ItemStack item;
    private final @NotNull Powerup.Type type;
    /**
     * How far the effects reach
     */
    private double radius;
    private NamespacedKey recipeKey;
    
    @Override
    public Powerup createPowerup(Location location) {
        return new CropGrower(location, radius);
    }
}
