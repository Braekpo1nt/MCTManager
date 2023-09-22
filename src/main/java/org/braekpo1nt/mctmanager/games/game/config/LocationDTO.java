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
     * @param world the world the location should be in
     * @return A new {@link Location} with the values this DTO was storing and the given world
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public float getYaw() {
        return yaw;
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
