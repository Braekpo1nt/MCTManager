package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Changes the speed of crop growth in a range
 */
@Data
public class CropGrower implements Powerup {
    /**
     * the item used to place the powerup
     */
    private final @NotNull ItemStack item;
    private final @NotNull Powerup.Type type;
    /**
     * How far the effects reach
     */
    private double radius;
}
