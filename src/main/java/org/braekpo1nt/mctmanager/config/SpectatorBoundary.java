package org.braekpo1nt.mctmanager.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the boundary that spectators must stay in during a game
 */
@AllArgsConstructor
public class SpectatorBoundary {
    
    /**
     * The bounding box that the spectators should be kept inside
     */
    @Getter
    private final @NotNull BoundingBox area;
    
    /**
     * The spawn location that spectators should be sent to if they stray 
     * too far from their designated area
     */
    private final @NotNull Location defaultSpawn;
    
    /**
     * @param vector the coordinates to check
     * @return true if this boundary contains the given location, false otherwise
     */
    public boolean contains(@NotNull Vector vector) {
        return area.contains(vector);
    }
    
    /**
     * Teleport the given participant to the {@link #defaultSpawn} location
     * @param participant the participant to teleport
     */
    public void teleportToSpawn(Participant participant) {
        teleportToSpawn(participant.getPlayer());
    }
    
    /**
     * Teleport the given entity to the {@link #defaultSpawn} location
     * @param entity the entity to teleport
     */
    public void teleportToSpawn(Entity entity) {
        entity.teleport(defaultSpawn);
    }
    
}
