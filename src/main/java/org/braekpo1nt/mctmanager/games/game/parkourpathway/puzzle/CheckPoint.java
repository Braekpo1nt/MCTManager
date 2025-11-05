package org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a checkpoint within a puzzle.
 */
@Getter
@Setter
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
    private @NotNull Location respawn;
    
    /**
     * Creates a copy of this checkpoint.
     * @return A new checkpoint with the same detection area and respawn location.
     */
    public CheckPoint copy() {
        return new CheckPoint(detectionArea.clone(), respawn.clone());
    }
}
