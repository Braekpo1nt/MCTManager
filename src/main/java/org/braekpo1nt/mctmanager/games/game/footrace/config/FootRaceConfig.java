package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

record FootRaceConfig(String world, Vector startingLocation, BoundingBoxDTO finishLine, BoundingBoxDTO spectatorArea, Scores scores, Durations durations, JsonObject description) {
    BoundingBox getFinishLine() {
        return finishLine.getBoundingBox();
    }
    
    BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    record Scores(int completeLap, int[] placementPoints, int detriment) {
    }
    
    record Durations(int startRace, int raceEndCountdown) {
    }
    
}
