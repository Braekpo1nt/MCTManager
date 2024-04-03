package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @param inBounds The bounding box that is considered in-bounds (if you leave it you're out of bounds). Can't be null, must have a volume of at least 1.<br>
 *                 - all CheckPoint detectionAreas and respawns must be inside inBounds (otherwise players will constantly respawn to out-of-bounds areas)<br>
 *                 - the next puzzle's (or finish line's) CheckPoint detectionAreas and respawns must be inside inBounds (otherwise players will be teleported to the respawn point before they reach the next puzzle's checkpoint).<br> 
 *                 - Similarly, CheckPoint detectionAreas and respawns must be contained in the previous puzzle's inBounds area (or players won't reach this puzzle) <br>
 *                 - only the finish line (the last puzzle) doesn't need to contain the next puzzle's CheckPoint detectionAreas and respawns (because there isn't one)<br>
 *                 tl;dr: Must contain: all this Puzzle's checkpoints, all next Puzzle's checkpoints
 * @param checkPoints the list of checkpoints that players must reach to begin this puzzle. They contain the respawn point for if they go out of bounds, and the detectionArea which they must be inside to leave their previous puzzle and begin this one. Must have at least 1 element. Elements can't be null. detectionArea and respawn must be contained in the inBounds area. <br>
 *                    - CheckPoints can't have conflicting detectionAreas (meaning they can't overlap and can't have their respawns within another CheckPoint's detectionArea)
 * @param transition if a puzzle is a transition (true) then it awards no points and does not announce completion, and doesn't count toward the total puzzles
 */
public record Puzzle(BoundingBox inBounds, List<CheckPoint> checkPoints, boolean transition) {
    /**
     * 
     * @param detectionArea if a player reaches this area, they are considered to be in this puzzle (i.e. they completed the previous puzzle). This must contain the respawn location. Must have a volume of at least 1.
     * @param respawn the location at which a player should respawn if they go out of bounds of their current puzzle. Must be inside the detectionArea.
     */
    public record CheckPoint(BoundingBox detectionArea, Location respawn) {
        public CheckPoint copy() {
            return new CheckPoint(detectionArea.clone(), respawn.clone());
        }
    }
    
    public Puzzle copy() {
        return new Puzzle(inBounds.clone(), checkPoints.stream().map(CheckPoint::copy).collect(Collectors.toCollection(ArrayList::new)), transition);
    }
}
