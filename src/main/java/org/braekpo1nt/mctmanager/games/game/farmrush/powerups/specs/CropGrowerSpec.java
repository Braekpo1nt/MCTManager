package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.CropGrower;
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
 * Changes the speed of crop growth in a range
 */
@Data
@Builder
public class CropGrowerSpec implements PowerupSpec {
    private final @NotNull PowerupType type = PowerupType.CROP_GROWER;
    private final @NotNull Recipe recipe;
    private double radius;
    private NamespacedKey recipeKey;
    
    /**
     * how many cycles between each grow attempt. Each cycle runs a probability check
     * on the crops in its radius to see if it should grow.
     * Defaults to 5.
     */
    @Builder.Default
    private int growCycles = 5;
    private double growthChance;
    
    public CropGrower createPowerup(Location location) {
        return new CropGrower(location, radius, growCycles, growthChance);
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
        return PowerupManager.cropGrowerItem.getItemMeta().equals(itemMeta);
    }
}
