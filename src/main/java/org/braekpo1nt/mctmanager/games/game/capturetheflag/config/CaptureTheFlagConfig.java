package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;


import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record CaptureTheFlagConfig(String world, Vector spawnObservatory, List<ArenaDTO> arenas, BoundingBox spectatorArea, Points scores, Durations durations) {
    public record ArenaDTO(Vector northSpawn, Vector southSpawn, Vector northFlag, Vector southFlag, Vector northBarrier, Vector southBarrier, BoundingBox boundingBox) {
    }
    
    public record Points(int kill, int win) {
    }
    
    public record Durations() {
    }
}
