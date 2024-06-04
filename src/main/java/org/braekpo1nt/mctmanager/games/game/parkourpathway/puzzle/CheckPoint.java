package org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

/**
 * Represents a checkpoint within a puzzle.
 */
@AllArgsConstructor
public class CheckPoint {
    /**
     * The detection area for this checkpoint.
     * When a player enters this area, they are considered to have completed the previous puzzle.
     * This area must contain the respawn location.
     */
    private final BoundingBox detectionArea;
    
    /**
     * The respawn location for this checkpoint.
     * Players respawn at this location if they go out of bounds of their current puzzle.
     */
    private final Location respawn;
    
    /**
     * Creates a copy of this checkpoint.
     *
     * @return A new checkpoint with the same detection area and respawn location.
     */
    public CheckPoint copy() {
        return new CheckPoint(detectionArea.clone(), respawn.clone());
    }
    
    public BoundingBox detectionArea() {
        return detectionArea;
    }
    
    public Location respawn() {
        return respawn;
    }
}
