package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import org.braekpo1nt.mctmanager.games.game.parkourpathway.CheckPoint;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

public record ParkourPathwayConfig  (String world, Durations durations, List<CheckPointDTO> checkpoints) {
    public record Durations(int timeLimit, int checkpointCounter, int checkpointCounterAlert) {
    }
    
    /**
     * The Data Transfer Object holding the necessary information to create a {@link CheckPoint}. 
     * For more info, look up the DAO (Data Access Object) and DTO (Data Transfer Object) pattern
     * @param yValue the y-value that a participant can't fall below without being teleported to respawn
     * @param detectionBox the box that is checked to see if the player entered the checkpoint
     * @param respawn the position to teleport back to if the player falls below the yValue
     */
    public record CheckPointDTO(double yValue, BoundingBox detectionBox, Vector respawn) {
    }
}
