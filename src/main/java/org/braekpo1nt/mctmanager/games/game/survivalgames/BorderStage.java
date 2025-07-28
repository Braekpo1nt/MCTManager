package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Location;

import java.util.List;

@Data
@Builder
public class BorderStage {
    /** The size (in blocks) the border will be at this stage. 
     * The border will shrink from the previous stage's size to this stage's size over this stage's duration.
     * Note: size is the full side length of the border, not the distance from the center to the edge
     */
    private int size;
    /** the border will stay at the previous stage's size for this many seconds */
    private int delay;
    /** the border will take this many seconds to transition from the previous stage's size to this stage's size */
    private int duration;
    
    public List<Location> getLocationsInside(double centerX, double centerZ, List<Location> respawnLocations) {
        return respawnLocations.stream()
                .filter(r -> MathUtils.pointIsInsideSquare(centerX, centerZ, size, r.x(), r.z()))
                .toList();
    }
    
    /**
     * @return The total time this border stage will take (delay plus duration)
     */
    public int getTotalTime() {
        return delay + duration;
    }
}
