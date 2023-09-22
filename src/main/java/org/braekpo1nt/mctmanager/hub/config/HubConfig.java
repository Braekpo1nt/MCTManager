package org.braekpo1nt.mctmanager.hub.config;

import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.bukkit.util.Vector;

record HubConfig(String world, LocationDTO spawn, LocationDTO podium, LocationDTO podiumObservation, double yLimit, Vector leaderBoard, Durations durations) {
    record Durations(int tpToHub) {
    }
}
