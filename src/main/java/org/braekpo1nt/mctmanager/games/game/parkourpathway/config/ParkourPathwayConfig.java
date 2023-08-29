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
     * @param detectionBox the box that is checked to see if the player entered this checkpoint
     * @param respawn the position to teleport back to if the player falls below the yValue
     */
    public record CheckPointDTO(double yValue, DetectionBox detectionBox, Vector respawn) {
        /**
         * A necessary encapsulation of a BoundingBox. Users may enter garbage min and max coordinates,
         * so a true BoundingBox could return negative volume, or incorrect maxY or other dimensions.
         * With this, a user can enter two points, and we will form a bounding box from it.
         * @param x1
         * @param y1
         * @param z1
         * @param x2
         * @param y2
         * @param z2
         */
        public record DetectionBox(double x1, double y1, double z1, double x2, double y2, double z2) {
            public BoundingBox boundingBox() {
                return new BoundingBox(x1, y1, z1, x2, y2, z2);
            }
        }
    }
}
