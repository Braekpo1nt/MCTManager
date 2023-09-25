package org.braekpo1nt.mctmanager.games.colossalcolosseum.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;

record ColossalColosseumConfig(String version, String world, LocationDTO firstPlaceSpawn, LocationDTO secondPlaceSpawn, LocationDTO spectatorSpawn, int requiredWins, JsonObject description) {
}
