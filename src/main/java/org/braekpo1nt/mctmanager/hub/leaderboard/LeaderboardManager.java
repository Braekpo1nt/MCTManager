package org.braekpo1nt.mctmanager.hub.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the leaderboard (creating, updating, showing/hiding)
 */
public class LeaderboardManager {
    
    private final Map<UUID, Hologram> holograms = new HashMap<>();
    private static final String PREFIX = "leaderboard_";
    private @NotNull Location location;
    
    /**
     * @param location the location that the leaderboard should appear. Must not be null. 
     */
    public LeaderboardManager(@NotNull Location location) {
        this.location = location;
    }
    
    /**
     * Set the location of the leaderboard. Updates any existing holograms. 
     * @param location the location of the leaderboard 
     */
    public void setLocation(@NotNull Location location) {
        this.location = location;
        for (Hologram hologram : holograms.values()) {
            DHAPI.moveHologram(hologram, location);
        }
    }
    
    public void onParticipantJoin(@NotNull Player participant) {
        if (holograms.containsKey(participant.getUniqueId())) {
            return;
        }
        Hologram hologram = createHologram(PREFIX + participant.getName());
        hologram.setShowPlayer(participant);
        holograms.put(participant.getUniqueId(), hologram);
    }
    
    public void onParticipantQuit(@NotNull Player participant) {
        Hologram hologram = holograms.remove(participant.getUniqueId());
        if (hologram != null) {
            DHAPI.removeHologram(hologram.getName());
        }
    }
    
    /**
     * Creates a new hologram or retrieves the one already created if one with the same name exists
     * @param name the name of the hologram (an ID)
     * @return the created hologram with the given name and location
     */
    private Hologram createHologram(String name) {
        Hologram hologram = DHAPI.getHologram(name);
        if (hologram == null) {
            hologram = DHAPI.createHologram(name, location);
        } else {
            DHAPI.moveHologram(hologram, location);
        }
        DHAPI.setHologramLines(hologram, List.of("Test"));
        hologram.setDefaultVisibleState(false);
        return hologram;
    }
    
}
