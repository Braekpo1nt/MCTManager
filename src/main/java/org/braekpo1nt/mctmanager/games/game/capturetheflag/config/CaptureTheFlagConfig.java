package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;


import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record CaptureTheFlagConfig(String world, Vector spawnObservatory, List<ArenaDTO> arenas, BoundingBoxDTO spectatorArea, Scores scores, Durations durations) {
    
    public BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    public record ArenaDTO(Vector northSpawn, Vector southSpawn, Vector northFlag, Vector southFlag, Vector northBarrier, Vector southBarrier, Arena.BarrierSize barrierSize, BoundingBoxDTO boundingBox) {
        public BoundingBox getBoundingBox() {
            return boundingBox.getBoundingBox();
        }
    }
    
    /**
     * Holds the scores for the game
     * @param kill the number of points to award for getting a kill
     * @param win the number of points to award for winning a match
     */
    public record Scores(int kill, int win) {
    }
    
    /**
     * Holds durations for the game
     * @param matchesStarting the duration (in seconds) for the "matches starting" period (i.e. waiting in the lobby for the match to start)
     * @param classSelection the duration (in seconds) of the class selection period
     * @param roundTimer the duration (in seconds) of each round
     */
    public record Durations(int matchesStarting, int classSelection, int roundTimer) {
    }
}
