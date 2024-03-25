package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
@AllArgsConstructor
class ParkourPathwayConfig {
    
    private final String version;
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
    
}
