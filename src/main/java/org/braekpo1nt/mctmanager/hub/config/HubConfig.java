package org.braekpo1nt.mctmanager.hub.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

record HubConfig(String world, Vector spawn, Vector podium, Vector podiumObservation, double yLimit, Vector leaderBoard, Durations durations) {
    record Durations(int tpToHub) {
    }
}
