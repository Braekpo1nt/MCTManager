package org.braekpo1nt.mctmanager.hub.config;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class HubConfig {
    
    private World world;
    private Location spawn;
    private Location podium;
    private Location podiumObservation;
    /**
     * the location the leaderboard should appear
     */
    private @NotNull Location leaderboardLocation;
    /**
     * how many players should be shown (i.e. top 10, top 20, top 5, etc.)
     */
    private int topPlayers;
    private double yLimit;
    private int tpToHubDuration;
    
}
