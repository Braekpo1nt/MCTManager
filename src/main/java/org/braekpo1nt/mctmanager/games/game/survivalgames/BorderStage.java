package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class BorderStage {
    /**
     * The size (in blocks) the border will be at this stage.
     * The border will shrink from the previous stage's size to this stage's size over this stage's duration.
     * Note: size is the full side length of the border, not the distance from the center to the edge
     */
    private int size;
    /**
     * the border will stay at the previous stage's size for this many seconds
     */
    private int delay;
    /**
     * the border will take this many seconds to transition from the previous stage's size to this stage's size
     */
    private int duration;
    
    /**
     * You need to pass the center of the border to this method because BorderStage objects
     * don't store the center of the border in them.
     * @param centerX the center x coord of the border
     * @param centerZ the center z coord of the boarder
     * @param respawnLocations the locations to check if they're inside the border stage
     * @return a list containing all the locations inside the border stage at the given center
     */
    public List<Location> getLocationsInside(double centerX, double centerZ, List<Location> respawnLocations) {
        return respawnLocations.stream()
                .filter(r -> MathUtils.pointIsInsideSquare(centerX, centerZ, size, r.x(), r.z()))
                .toList();
    }
    
    /**
     * You need to pass the center of the border to this method because BorderStage objects
     * don't store the center of the border in them.
     * @param centerX the center x coord of the border
     * @param centerZ the center z coord of the boarder
     * @param respawnLocations the locations to check if they're inside the border stage
     * @return the indexes of the locations from the given respawnLocations list which are inside
     * this border
     */
    public @NotNull List<Integer> getLocationIndexesInside(double centerX, double centerZ, List<Location> respawnLocations) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < respawnLocations.size(); i++) {
            if (contains(centerX, centerZ, respawnLocations.get(i))) {
                result.add(i);
            }
        }
        return result;
    }
    
    /**
     * You need to pass the center of the border to this method because BorderStage objects
     * don't store the center of the border in them.
     * @param centerX the center x coord of the border
     * @param centerZ the center z coord of the boarder
     * @param location the location to check if it's inside the border stage
     * @return true if this border stage (with the given center) contains the given location
     */
    public boolean contains(double centerX, double centerZ, Location location) {
        return MathUtils.pointIsInsideSquare(centerX, centerZ, size, location.x(), location.z());
    }
    
    /**
     * @return The total time this border stage will take (delay plus duration)
     */
    public int getTotalTime() {
        return delay + duration;
    }
}
