package org.braekpo1nt.mctmanager.games.colossalcolosseum.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;

record ColossalColosseumConfig(String version, String world, BoundingBoxDTO spectatorArea, LocationDTO firstPlaceSpawn, LocationDTO secondPlaceSpawn, LocationDTO spectatorSpawn, int requiredWins, Gate firstPlaceGate, Gate secondPlaceGate, Durations durations, JsonObject description) {
    record Gate(BoundingBoxDTO clearArea, BoundingBoxDTO placeArea, BoundingBoxDTO stone) {
    }
    
    record Durations(int roundStarting) {
    }
}
