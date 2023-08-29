package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;


import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record CaptureTheFlagConfig(String world, Vector spawnObservatory, List<ArenaDTO> arenas, BoundingBox spectatorArea, Scores scores, Durations durations) {
    public record ArenaDTO(Vector northSpawn, Vector southSpawn, Vector northFlag, Vector southFlag, Vector northBarrier, Vector southBarrier, BoundingBox boundingBox) {
    }
    
    public record Scores(int kill, int win) {
    }
    
    public record Durations(int matchesStarting, int classSelection, int roundTimer) {
    }
}
