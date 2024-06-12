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
    /**
     * how many players should be shown (i.e. top 10, top 20, top 5, etc.)
     */
    private int topNumber;
    private double yLimit;
    private int tpToHubDuration;
    
}
