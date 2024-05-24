package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;

record FootRaceConfig(String version, String world, LocationDTO startingLocation, BoundingBoxDTO finishLine, BoundingBoxDTO spectatorArea, BoundingBoxDTO glassBarrier, Scores scores, Durations durations, JsonElement description) {
    
    record Scores(int completeLap, int[] placementPoints, int detriment) {
    }
    
    record Durations(int startRace, int raceEndCountdown) {
    }
    
}
