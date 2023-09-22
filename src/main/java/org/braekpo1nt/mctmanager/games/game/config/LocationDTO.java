package org.braekpo1nt.mctmanager.games.game.config;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * An abstraction of {@link Location} for gson serialization/deserialization purposes.
 * <p>
 * Because we don't need to store the world for the location in the csv.
 */
public class LocationDTO {
    private double x;
    private double y;
    private double z;
    /**
     * The absolute rotation on the y-plane, in degrees
     */
    private float pitch;
    /**
     * The absolute rotation on the x-plane, in degrees
     */
    private float yaw;
    
    /**
     * Get the {@link Location} this DTO was storing. If this is the first request, creates the Location from the x, y, z, yaw, and pitch values stored in this DTO's fields, and the provided world
     * @param world the world this location should be in
     * @return The Location this DTO was storing. Modifying the return value will not modify the DTO.
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    @Override
    public String toString() {
        if (yaw == 0 && pitch == 0) {
            return "LocationDTO{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
        return "LocationDTO{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }
}
