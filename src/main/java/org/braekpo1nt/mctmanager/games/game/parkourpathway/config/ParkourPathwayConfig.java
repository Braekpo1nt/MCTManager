package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.Puzzle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 
 * @param version
 * @param world
 * @param startingLocation the location that players start the Parkour Pathway from
 * @param puzzles the list of puzzles for this parkour game
 * @param spectatorArea
 * @param scores
 * @param durations
 * @param description
 */
record ParkourPathwayConfig  (String version, String world, Vector startingLocation, List<PuzzleDTO> puzzles, BoundingBoxDTO spectatorArea, Scores scores, Durations durations, JsonElement description) {
    
    /**
     * 
     * @param checkpoint points for reaching puzzle checkpoints. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
     * @param win points for winning. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
     */
    record Scores(int[] checkpoint, int[] win) {
    }
    
    record Durations(int starting, int timeLimit, int checkpointCounter, int checkpointCounterAlert) {
    }
    
    /**
     * @param inBounds The bounding box that is considered in-bounds (if you leave it you're out of bounds). Must contain 
     * - all CheckPoint detectionAreas and respawn locations
     * - the checkpoint detection areas of the puzzle after this (otherwise players will be teleported to the respawn point before they reach the next puzzle's checkpoint). 
     * @param checkPoints the list of checkpoints that players must reach to begin this puzzle. They contain the respawn point for if they go out of bounds, and the detectionArea which they must be inside to leave their previous puzzle and begin this one.
     */
    record PuzzleDTO(BoundingBoxDTO inBounds, List<CheckPointDTO> checkPoints) {
        /**
         * @param detectionArea if a player reaches this area, they are considered to be in this puzzle (i.e. they completed the previous puzzle). This must contain the respawn location.
         * @param respawn the location at which a player should respawn if they go out of bounds of their current puzzle. Must be inside the detectionArea.
         */
        record CheckPointDTO(BoundingBoxDTO detectionArea, LocationDTO respawn) {
            Puzzle.CheckPoint toCheckPoint(World world) {
                return new Puzzle.CheckPoint(detectionArea.toBoundingBox(), respawn.toLocation(world));
            }
        }
        
        public Puzzle toPuzzle(World world) {
            return new Puzzle(
                    inBounds.toBoundingBox(),
                    checkPoints.stream().map(checkPointDTO -> checkPointDTO.toCheckPoint(world)).toList()
            );
        }
    }
}
