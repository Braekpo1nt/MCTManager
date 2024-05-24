package org.braekpo1nt.mctmanager.hub.config;

import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.bukkit.util.Vector;

record HubConfig(String version, String world, LocationDTO spawn, LocationDTO podium, LocationDTO podiumObservation, Vector leaderBoard, double yLimit, Durations durations) {
    record Durations(int tpToHub) {
    }
}
