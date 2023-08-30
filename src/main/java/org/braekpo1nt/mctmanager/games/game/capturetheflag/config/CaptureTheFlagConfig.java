package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;


import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record CaptureTheFlagConfig(String world, Vector spawnObservatory, List<ArenaDTO> arenas, BoundingBoxDTO spectatorArea, Scores scores, Durations durations) {
    
    public BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    public record ArenaDTO(Vector northSpawn, Vector southSpawn, Vector northFlag, Vector southFlag, Vector northBarrier, Vector southBarrier, BoundingBoxDTO boundingBox) {
        public BoundingBox getBoundingBox() {
            return boundingBox.getBoundingBox();
        }
    }
    
    public record Scores(int kill, int win) {
    }
    
    public record Durations(int matchesStarting, int classSelection, int roundTimer) {
    }
}
