package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.Puzzle;
import org.bukkit.World;

import java.util.List;

@Getter
@RequiredArgsConstructor
class PuzzleDTO {
    /**
     * purely for assistance in editing the config file
     */
    private int index;
    /**
     * The bounding box that is considered in-bounds (if you leave it you're out of bounds). Must contain
     * - all CheckPoint detectionAreas and respawn locations
     * - the checkpoint detection areas of the puzzle after this (otherwise players will be teleported to the respawn point before they reach the next puzzle's checkpoint).
     */
    private final BoundingBoxDTO inBounds;
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
        
        Puzzle.CheckPoint toCheckPoint(World world) {
            return new Puzzle.CheckPoint(detectionArea.toBoundingBox(), respawn.toLocation(world));
        }
        
        static CheckPointDTO from(Puzzle.CheckPoint checkPoint) {
            return new CheckPointDTO(BoundingBoxDTO.from(checkPoint.detectionArea()), LocationDTO.from(checkPoint.respawn()));
        }
        
        static List<CheckPointDTO> from(List<Puzzle.CheckPoint> checkPoints) {
            return checkPoints.stream().map(CheckPointDTO::from).toList();
        }
    }
    
    Puzzle toPuzzle(World world) {
        return new Puzzle(
                inBounds.toBoundingBox(),
                checkPoints.stream().map(checkPointDTO -> checkPointDTO.toCheckPoint(world)).toList()
        );
    }
}
