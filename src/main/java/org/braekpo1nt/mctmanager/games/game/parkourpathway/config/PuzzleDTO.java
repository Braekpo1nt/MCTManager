package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
class PuzzleDTO implements Validatable {
    /**
     * purely for assistance in editing the config file
     */
    private int index;
    /**
     * The bounding boxes which are collectively considered in-bounds (if you leave them, you're out of bounds). <br>
     * - all detectionAreas must be in at least one inBound box <br>
     * - all detection areas of the puzzle after this must also be in at least one inBound box (otherwise players will be teleported to the respawn point before they reach the next puzzle's checkpoint). <br>
     */
    private final List<BoundingBox> inBounds;
    /**
     * the list of checkpoints that players must reach to begin this puzzle. They contain the respawn point for if they go out of bounds, and the detectionArea which they must be inside to leave their previous puzzle and begin this one.
     */
    private final List<CheckPointDTO> checkPoints;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validateInBounds(validator);
        validator.notNull(this.getCheckPoints(), "checkPoints");
        validateCheckPoint(validator);
    }
    
    private void validateInBounds(@NotNull Validator validator) {
        validator.notNull(this.getInBounds(), "inBounds");
        validator.validate(!this.getInBounds().isEmpty(), "inBounds can't be empty");
        validator.validate(!this.inBounds.contains(null), "inBounds can't contain null");
        List<BoundingBox> inBounds = this.inBounds.stream().map(BoundingBox::toBoundingBox).toList();
        for (int i = 0; i < inBounds.size(); i++) {
            BoundingBox inBound = inBounds.get(i);
            validator.validate(inBound.getVolume() >= 1, "inBounds[%d]'s volume (%s) can't be less than 1 (%s)", i, inBound.getVolume(), inBound);
            if (inBounds.size() == 1) {
                return; // no need to check for overlapping
            }
            validator.validate(overlapsOneOtherBox(i, inBounds), "inBounds[%d] must overlap at least one other inBounds box in the list", i);
        }
    }
    
    private void validateCheckPoint(@NotNull Validator validator) {
        validator.validate(!this.getCheckPoints().isEmpty(), "checkPoints must have at least 1 element");
        for (int i = 0; i < this.checkPoints.size(); i++) {
            CheckPointDTO checkPoint = this.checkPoints.get(i);
            validator.validate(checkPoint != null, "checkPoints can't have null elements");
            checkPoint.validate(validator.path("checkPoints[%d]", i));
            BoundingBox detectionArea = checkPoint.getDetectionArea();
            validator.validate(this.isInBounds(detectionArea), "checkPoints[%d].inBounds must contain all checkPoints[%d].detectionAreas", i, i);
            Vector respawn = checkPoint.getRespawn().toVector();
            validator.validate(detectionArea.contains(respawn), "checkPoints[%s].detectionArea must contain checkPoints[%s].respawn", i, i);
            for (int j = 0; j < i; j++) {
                PuzzleDTO.CheckPointDTO earlierCheckpoint = this.getCheckPoints().get(j);
                BoundingBox earlierDetectionArea = earlierCheckpoint.getDetectionArea();
                validator.validate(!earlierDetectionArea.overlaps(detectionArea), "checkPoints[%s].detectionArea (%s) and checkPoints[%s].detectionArea (%s) can't overlap", i-1, earlierDetectionArea, i, detectionArea);
            }
        }
    }
    
    /**
     * @param index the index of the bounding box to check if it overlaps at least one other box in the list
     * @param boxes a list of bounding boxes
     * @return true if the box with the given index overlaps at least one other box in the list, false otherwise
     */
    static boolean overlapsOneOtherBox(int index, @NotNull List<@NotNull BoundingBox> boxes) {
        BoundingBox box = boxes.get(index);
        for (int i = 0; i < boxes.size(); i++) {
            if (i != index) {
                BoundingBox otherBox = boxes.get(i);
                if (box.overlaps(otherBox)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    static @NotNull PuzzleDTO fromPuzzle(Puzzle puzzle) {
        return new PuzzleDTO(BoundingBox.from(puzzle.inBounds()), CheckPointDTO.from(puzzle.checkPoints()));
    }
    
    /**
     * Also sets the index of the puzzles in order
     * @param puzzles the puzzles to convert to PuzzleDTOs
     * @return the puzzles as PuzzleDTOs with their index fields assigned
     */
    static @NotNull List<PuzzleDTO> fromPuzzles(@NotNull List<Puzzle> puzzles) {
        List<PuzzleDTO> puzzleDTOS = puzzles.stream().map(PuzzleDTO::fromPuzzle).toList();
        for (int i = 0; i < puzzleDTOS.size(); i++) {
            puzzleDTOS.get(i).setIndex(i);
        }
        return puzzleDTOS;
    }
    
    void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * @param box the box to check if it's contained in the inBounds boxes
     * @return true if the given box is contained in at least one of this PuzzleDTO's inBounds boxes, false otherwise
     */
    boolean isInBounds(BoundingBox box) {
        for (BoundingBox inBound : inBounds) {
            if (inBound.contains(box)) {
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
        for (BoundingBox inBound : inBounds) {
            if (inBound.contains(v)) {
                return true;
            }
        }
        return false;
    }
    
    @Data
    @AllArgsConstructor
    static class CheckPointDTO implements Validatable {
        /**
         * if a player reaches this area, they are considered to be in this puzzle (i.e. they completed the previous puzzle). This must contain the respawn location.
         */
        private BoundingBox detectionArea;
        /**
         * the location at which a player should respawn if they go out of bounds of their current puzzle. Must be inside the detectionArea.
         */
        private LocationDTO respawn;
        
        CheckPoint toCheckPoint(World world) {
            return new CheckPoint(detectionArea, respawn.toLocation(world));
        }
        
        static CheckPointDTO from(CheckPoint checkPoint) {
            return new CheckPointDTO(BoundingBox.from(checkPoint.detectionArea()), LocationDTO.from(checkPoint.respawn()));
        }
        
        static List<CheckPointDTO> from(List<CheckPoint> checkPoints) {
            return checkPoints.stream().map(CheckPointDTO::from).toList();
        }
    
        @Override
        public void validate(@NotNull Validator validator) {
            BoundingBox detectionArea = this.getDetectionArea();
            validator.validate(detectionArea.getVolume() >= 1, "detectionArea's volume (%s) can't be less than 1 (%s)", detectionArea.getVolume(), detectionArea);
            Vector respawn = this.getRespawn().toVector();
            validator.validate(detectionArea.contains(respawn), "detectionArea (%s) must contain respawn (%s)", detectionArea, respawn);
        }
    }
    
    Puzzle toPuzzle(World world) {
        return new Puzzle(
                inBounds.stream().map(BoundingBox::toBoundingBox).toList(),
                checkPoints.stream().map(checkPointDTO -> checkPointDTO.toCheckPoint(world)).toList()
        );
    }
    
    static List<Puzzle> toPuzzles(World world, List<PuzzleDTO> puzzleDTOS) {
        return puzzleDTOS.stream().map(puzzleDTO -> puzzleDTO.toPuzzle(world)).collect(Collectors.toCollection(ArrayList::new));
    }
}
