package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Powerup {
    enum Type {
        CROP_GROWER,
//        ANIMAL_GROWTH,
    }
    
    /**
     * the item used to place the powerup
     */
    @NotNull ItemStack getItem();
    @NotNull Type getType();
    /**
     * How far the effects reach
     */
    double getRadius();
    void setRadius(double radius);
}
