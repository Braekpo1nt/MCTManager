package org.braekpo1nt.mctmanager.games.colossalcolosseum.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;

record ColossalColosseumConfig(String version, String world, LocationDTO firstPlaceSpawn, LocationDTO secondPlaceSpawn, LocationDTO spectatorSpawn, int requiredWins, Gate firstPlaceGate, Gate secondPlaceGate, JsonObject description) {
    record Gate(BoundingBoxDTO clearArea, BoundingBoxDTO placeArea, BoundingBoxDTO stone) {
    }
}
