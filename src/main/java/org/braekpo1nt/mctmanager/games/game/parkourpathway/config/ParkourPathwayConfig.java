package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.CheckPoint;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record ParkourPathwayConfig  (String world, Vector startingLocation, BoundingBoxDTO spectatorArea, Scores scores, Durations durations, List<CheckPointDTO> checkpoints, JsonObject description) {
    
    BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    /**
     * 
     * @param checkpoint points for reaching checkpoints. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
     * @param win points for winning. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
     */
    record Scores(int[] checkpoint, int[] win) {
    }
    
    record Durations(int starting, int timeLimit, int checkpointCounter, int checkpointCounterAlert) {
    }
    
    /**
     * The Data Transfer Object holding the necessary information to create a {@link CheckPoint}. 
     * For more info, look up the DAO (Data Access Object) and DTO (Data Transfer Object) pattern
     * @param yValue the y-value that a participant can't fall below without being teleported to respawn
     * @param detectionBox the box that is checked to see if the player entered this checkpoint
     * @param respawn the position to teleport back to if the player falls below the yValue
     */
    record CheckPointDTO(double yValue, BoundingBoxDTO detectionBox, Vector respawn) {
        public BoundingBox getDetectionBox() {
            return detectionBox.getBoundingBox();
        }
    }
}
