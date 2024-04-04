package org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle;

import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.bukkit.util.Vector;

/**
 * Represents a puzzle in a parkour pathway game.
 */
@AllArgsConstructor
public class Puzzle {
    /**
     * The bounding box that is considered in-bounds.
     * Players must stay inside this area to remain in the puzzle.
     * It must contain all checkpoints and respawn locations.
     */
    private final List<BoundingBox> inBounds;
    
    /**
     * The list of checkpoints that players must reach to begin this puzzle.
     * Each checkpoint contains a respawn point and detection area.
     */
    private final List<CheckPoint> checkPoints;
    
    public List<BoundingBox> inBounds() {
        return inBounds;
    }
    
    public List<CheckPoint> checkPoints() {
        return checkPoints;
    }
    
    /**
     * Creates a copy of this puzzle.
     *
     * @return A new puzzle with the same in-bounds area, checkpoints, and transition status.
     */
    public Puzzle copy() {
        List<CheckPoint> copiedCheckPoints = checkPoints.stream()
                .map(CheckPoint::copy)
                .collect(Collectors.toCollection(ArrayList::new));
        List<BoundingBox> copiedBoundingBoxes = inBounds.stream()
                .map(BoundingBox::clone)
                .collect(Collectors.toCollection(ArrayList::new));
        return new Puzzle(copiedBoundingBoxes, copiedCheckPoints);
    }
    
    /**
     * @param v the vector to check if it is contained in this puzzle's inBounds boxes
     * @return true if the given vector is contained in any of this puzzle's inBounds boxes, false otherwise
     */
    public boolean isInBounds(Vector v) {
        for (BoundingBox bound : inBounds) {
            if (bound.contains(v)) {
                return true;
            }
        }
        return false;
    }
}

