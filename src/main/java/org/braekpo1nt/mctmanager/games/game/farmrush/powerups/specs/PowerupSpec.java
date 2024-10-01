package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PowerupSpec {
    
    @NotNull
    PowerupType getType();
    /**
     * How far the effects reach
     */
    double getRadius();
    void setRadius(double radius);
    
    /**
     * @param itemStack the item to check
     * @return true if this item matches this {@link PowerupSpec}'s item
     */
    boolean isItem(@Nullable ItemStack itemStack);
}
