package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.Puzzle;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
@AllArgsConstructor
class ParkourPathwayConfig {
    
    private final String version;
    private final String world;
    /** the list of puzzles for this parkour game */
    private List<PuzzleDTO> puzzles;
    private final BoundingBoxDTO spectatorArea;
    private final Scores scores;
    private final Durations durations;
    private final JsonElement description;
    
    /**
     * Set this config's {@link ParkourPathwayConfig#puzzles} to be the given list of {@link Puzzle} objects
     * @param puzzles {@link Puzzle} list to be assigned to this config
     */
    public void setPuzzles(List<Puzzle> puzzles) {
        this.puzzles = puzzles.stream().map(PuzzleDTO::from).toList();
        for (int i = 0; i < this.puzzles.size(); i++) {
            this.puzzles.get(i).setIndex(i);
        }
    }
    
    @Getter
    @AllArgsConstructor
    static class Scores {
        /**
         * points for reaching puzzle checkpoints. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
         */
        private final int[] checkpoint;
        /**
         * points for winning. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
         */
        private final int[] win;
    }
    
    @Getter
    @AllArgsConstructor
    static class Durations {
        private final int starting;
        private final int timeLimit;
        private final int checkpointCounter;
        private final int checkpointCounterAlert;
    }
    
}
