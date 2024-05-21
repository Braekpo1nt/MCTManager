package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
class ParkourPathwayConfig {
    
    private String version;
    private String world;
    /**
     * the larger glass barrier meant to close off all participants from the puzzles until it is time to race. If null, no glass barrier will be created.
     */
    private @Nullable BoundingBoxDTO glassBarrier;
    /**
     * The chat message sent to all participants when the glass barrier opens. Null means no message will be sent.
     */
    private @Nullable JsonElement glassBarrierOpenMessage;
    /**
     * the list of team spawn locations. If null, the team spawn phase will be skipped. Each {@link TeamSpawnDTO#getBarrierArea()} and {@link TeamSpawnDTO#getSpawn()} must be contained in the inBounds area of the first puzzle.
     */
    private @Nullable List<TeamSpawnDTO> teamSpawns;
    /**
     * The chat message sent to all participants when the team spawns open. Null means no message will be sent.
     */
    private @Nullable JsonElement teamSpawnsOpenMessage;
    /** the list of puzzles for this parkour game */
    private List<PuzzleDTO> puzzles;
    private BoundingBoxDTO spectatorArea;
    private Scores scores;
    private Durations durations;
    private JsonElement description;
    
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
    @NoArgsConstructor
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
    @NoArgsConstructor
    static class Durations {
        /**
         * how long the teams spend inside their individual spawns. Defaults to -1.
         */
        private int teamSpawn = -1;
        /**
         * how long (after the teamSpawn duration is over) until the final glass barrier is dropped
         */
        private int starting;
        private int timeLimit;
        private int checkpointCounter;
        private int checkpointCounterAlert;
    }
    
}
