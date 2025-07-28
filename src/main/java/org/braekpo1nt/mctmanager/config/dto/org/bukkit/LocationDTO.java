package org.braekpo1nt.mctmanager.config.dto.org.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An abstraction of {@link Location} for gson serialization/deserialization purposes.
 * <p>
 * Because we don't need to store the world for the location in the csv.
 */
public class LocationDTO {
    private final double x;
    private final double y;
    private final double z;
    /**
     * The absolute rotation on the x-plane, in degrees
     */
    private final float yaw;
    /**
     * The absolute rotation on the y-plane, in degrees
     */
    private final float pitch;
    
    public LocationDTO(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }
    
    public static LocationDTO from(Location respawn) {
        return new LocationDTO(respawn);
    }
    
    public static List<Location> toLocations(@NotNull List<LocationDTO> dtos, @NotNull World world) {
        return dtos.stream()
                .map(l -> l.toLocation(world))
                .toList();
    }
    
    /**
     * @param world the world the location should be in
     * @return A new {@link Location} with the values this DTO was storing and the given world
     */
    public @NotNull Location toLocation(@NotNull World world) {
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
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
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
    
    public Vector toVector() {
        return new Vector(x, y, z);
    }
}
