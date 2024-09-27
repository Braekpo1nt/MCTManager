package org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs;

import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.Powerup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PowerupSpec {
    
    /**
     * the item used to place the powerup
     */
    @NotNull ItemStack getItem();
    @NotNull
    Powerup.Type getType();
    /**
     * How far the effects reach
     */
    double getRadius();
    void setRadius(double radius);
    
    /**
     * @return a powerup using this as its spec
     */
    Powerup createPowerup(Location location);
}
