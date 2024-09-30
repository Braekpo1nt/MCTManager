package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.PowerupSpec;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * One of these is instantiated for each powerup
 * in the world. It is unique to its position and
 * features, stores and modifies world data,
 * and is kept track of in the {@link PowerupManager}.
 * You create these using {@link PowerupSpec}.
 */
@Data
public abstract class Powerup {
    public enum Type {
        CROP_GROWER,
        ANIMAL_GROWER,
    }
    
    /**
     * the world the powerup is in
     */
    protected final World world;
    /**
     * The location of the powerup in the world
     */
    protected final Location location;
    protected final double radius;
    
    public abstract void performAction();
    public abstract Type getType();
    
}
