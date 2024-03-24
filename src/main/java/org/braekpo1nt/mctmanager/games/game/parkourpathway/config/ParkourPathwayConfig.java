package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.Puzzle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParkourPathwayConfig {
    
    private String version;
    private String world;
    /** the location that players start the Parkour Pathway from */
    private Vector startingLocation;
    /** the list of puzzles for this parkour game */
    private List<PuzzleDTO> puzzles;
    private BoundingBoxDTO spectatorArea;
    private Scores scores;
    private Durations durations;
    private JsonElement description;
    
    @Getter
    @AllArgsConstructor
    static class Scores {
        /**
         * points for reaching puzzle checkpoints. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
         */
        private int[] checkpoint;
        /**
         * points for winning. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
         */
        private int[] win;
    }
    
    @Getter
    @AllArgsConstructor
    static class Durations {
        private int starting;
        private int timeLimit;
        private int checkpointCounter;
        private int checkpointCounterAlert;
    }
    
    @Getter
    @AllArgsConstructor
    class PuzzleDTO {
        /**
         * The bounding box that is considered in-bounds (if you leave it you're out of bounds). Must contain 
         * - all CheckPoint detectionAreas and respawn locations
         * - the checkpoint detection areas of the puzzle after this (otherwise players will be teleported to the respawn point before they reach the next puzzle's checkpoint).
         */
        private BoundingBoxDTO inBounds;
        /**
         * the list of checkpoints that players must reach to begin this puzzle. They contain the respawn point for if they go out of bounds, and the detectionArea which they must be inside to leave their previous puzzle and begin this one.
         */
        private List<CheckPointDTO> checkPoints;
        @Getter
        @AllArgsConstructor
        class CheckPointDTO {
            /**
             * if a player reaches this area, they are considered to be in this puzzle (i.e. they completed the previous puzzle). This must contain the respawn location.
             */
            private BoundingBoxDTO detectionArea;
            /**
             * the location at which a player should respawn if they go out of bounds of their current puzzle. Must be inside the detectionArea.
             */
            private LocationDTO respawn;
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
