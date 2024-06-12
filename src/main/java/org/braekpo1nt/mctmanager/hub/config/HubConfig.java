package org.braekpo1nt.mctmanager.hub.config;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

@Data
@Builder
public class HubConfig {
    
    private World world;
    private Location spawn;
    private Location podium;
    private Location podiumObservation;
    private Location leaderboardLocation;
    private double yLimit;
    private int tpToHubDuration;
    
}
