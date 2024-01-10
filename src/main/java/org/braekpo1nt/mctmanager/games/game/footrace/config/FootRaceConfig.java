package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

record FootRaceConfig(String version, String world, Vector startingLocation, BoundingBoxDTO finishLine, BoundingBoxDTO spectatorArea, BoundingBoxDTO glassBarrier, Scores scores, Durations durations, JsonElement description) {
    
    record Scores(int completeLap, int[] placementPoints, int detriment) {
    }
    
    record Durations(int startRace, int raceEndCountdown) {
    }
    
}
