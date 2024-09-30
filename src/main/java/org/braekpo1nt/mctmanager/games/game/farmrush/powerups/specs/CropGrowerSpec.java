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
    private final @NotNull Powerup.Type type = Powerup.Type.CROP_GROWER;
    /**
     * the item used to place the powerup
     */
    private final @NotNull ItemStack item;
    private final @NotNull Recipe recipe;
    private double radius;
    private NamespacedKey recipeKey;
    
    private int seconds;
    private double growthChance;
    
    @Override
    public Powerup createPowerup(Location location) {
        return new CropGrower(location, radius, seconds, growthChance);
    }
}
