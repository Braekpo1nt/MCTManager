package org.braekpo1nt.mctmanager.games.colossalcolosseum.config;

import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;

record ColossalColosseumConfig(String version, String world, LocationDTO firstPlaceSpawn, LocationDTO secondPlaceSpawn, LocationDTO spectatorSpawn) {
}
