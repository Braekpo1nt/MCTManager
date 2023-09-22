package org.braekpo1nt.mctmanager.games.game.config;

import org.bukkit.Location;
import org.bukkit.World;

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
    transient private Location location;

    public Location toLocation(World world) {
        if (location == null) {
            location = new Location(world, x, y, z, yaw, pitch);
        }
        return location;
    }

    @Override
    public String toString() {
        return "LocationDTO{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                ", location=" + location +
                '}';
    }
}
