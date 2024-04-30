package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
@RequiredArgsConstructor
class PuzzleDTO {
    /**
     * purely for assistance in editing the config file
     */
    private int index;
    /**
     * The bounding boxes which are collectively considered in-bounds (if you leave them, you're out of bounds). <br>
     * - all detectionAreas must be in at least one inBound box <br>
     * - all detection areas of the puzzle after this must also be in at least one inBound box (otherwise players will be teleported to the respawn point before they reach the next puzzle's checkpoint). <br>
     */
    private final List<BoundingBoxDTO> inBounds;
    /**
     * the list of checkpoints that players must reach to begin this puzzle. They contain the respawn point for if they go out of bounds, and the detectionArea which they must be inside to leave their previous puzzle and begin this one.
     */
    private final List<CheckPointDTO> checkPoints;
    
    static PuzzleDTO from(Puzzle puzzle) {
        return new PuzzleDTO(BoundingBoxDTO.from(puzzle.inBounds()), CheckPointDTO.from(puzzle.checkPoints()));
    }
    
    void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * @param box the box to check if it's contained in the inBounds boxes
     * @return true if the given box is contained in at least one of this PuzzleDTO's inBounds boxes, false otherwise
     */
    boolean isInBounds(BoundingBox box) {
        for (BoundingBoxDTO inBound : inBounds) {
            if (inBound.toBoundingBox().contains(box)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param v the vector to check if it is in bounds
     * @return true if the given vector is contained in at least one of this PuzzleDTO's inBounds boxes, false otherwise
     */
    boolean isInBounds(Vector v) {
        for (BoundingBoxDTO inBound : inBounds) {
            if (inBound.toBoundingBox().contains(v)) {
                return true;
            }
        }
        return false;
    }
    
    @Getter
    @AllArgsConstructor
    static class CheckPointDTO {
        /**
         * if a player reaches this area, they are considered to be in this puzzle (i.e. they completed the previous puzzle). This must contain the respawn location.
         */
        private final BoundingBoxDTO detectionArea;
        /**
         * the location at which a player should respawn if they go out of bounds of their current puzzle. Must be inside the detectionArea.
         */
        private final LocationDTO respawn;
        
        CheckPoint toCheckPoint(World world) {
            return new CheckPoint(detectionArea.toBoundingBox(), respawn.toLocation(world));
        }
        
        static CheckPointDTO from(CheckPoint checkPoint) {
            return new CheckPointDTO(BoundingBoxDTO.from(checkPoint.detectionArea()), LocationDTO.from(checkPoint.respawn()));
        }
        
        static List<CheckPointDTO> from(List<CheckPoint> checkPoints) {
            return checkPoints.stream().map(CheckPointDTO::from).toList();
        }
    }
    
    Puzzle toPuzzle(World world) {
        return new Puzzle(
                inBounds.stream().map(BoundingBoxDTO::toBoundingBox).toList(),
                checkPoints.stream().map(checkPointDTO -> checkPointDTO.toCheckPoint(world)).toList()
        );
    }
    
    static List<Puzzle> toPuzzles(World world, List<PuzzleDTO> puzzleDTOS) {
        return puzzleDTOS.stream().map(puzzleDTO -> puzzleDTO.toPuzzle(world)).toList();
    }
}
