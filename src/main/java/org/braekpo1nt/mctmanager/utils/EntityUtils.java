package org.braekpo1nt.mctmanager.utils;

import org.braekpo1nt.mctmanager.games.game.config.YawPitch;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class EntityUtils {
    
    /**
     * Method to calculate the direction (yaw and pitch) for a player to look at the target position from the source position
     * @param source The source position from which to look (specifically the player's Location as a vector, assumes eye height of 1.62 meters)
     * @param target The target position to look at
     * @return the yaw and pitch (in degrees) that an entity standing at source would need to have in order to look directly at target
     */
    public static YawPitch getPlayerLookAtYawPitch(Vector source, Vector target) {
        // x-axis distance from source to target
        double deltaX = target.getX() - source.getX();
        // vertical distance from source to target, adjusted for eye height
        double deltaY = target.getY() - (source.getY() + 1.62);
        // z-axis distance from source to target
        double deltaZ = target.getZ() - source.getZ();
        // yaw (horizontal rotation angle) in degrees
        float yaw = (float) -Math.toDegrees(Math.atan2(deltaX, deltaZ));
        // horizontal distance from source to target
        double d = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        // pitch (vertical rotation angle) in degrees
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, d));
        return new YawPitch(yaw, pitch);
    }
    
    /**
     * see {@link EntityUtils#getPlayerDirection(float, float)}
     * @param location a location
     */
    public static BlockFace getPlayerDirection(Location location) {
        return getPlayerDirection(location.getYaw(), location.getPitch());
    }
    
    /**
     * Converts the player's yaw and pitch to one of the 4 cardinal directions or up or down.
     * @param yaw the yaw in degrees of a player's looking direction
     * @param pitch the pitch in degrees of a player's looking direction
     * @return one of [{@link BlockFace#UP}, {@link BlockFace#DOWN}, {@link BlockFace#NORTH}, {@link BlockFace#SOUTH}, {@link BlockFace#EAST}, {@link BlockFace#WEST}] which the yaw and pitch are most aligned to
     */
    public static BlockFace getPlayerDirection(float yaw, float pitch) {
        double yawRadians = Math.toRadians((yaw + 360) % 360);
        double pitchRadians = Math.toRadians(pitch);
        // Calculate direction based on yaw and pitch
        double x = Math.sin(yawRadians) * Math.cos(pitchRadians);
        double y = Math.sin(pitchRadians);
        double z = -Math.cos(yawRadians) * Math.cos(pitchRadians);
        
        // Check which direction the player is facing
        if (y < -0.5)
            return BlockFace.UP;
        else if (y > 0.5)
            return BlockFace.DOWN;
        else if (Math.abs(x) > Math.abs(z)) {
            if (x > 0) {
                return BlockFace.WEST;
            } else {
                return BlockFace.EAST;
            }
        } else {
            if (z > 0){
                return BlockFace.NORTH;
            } else {
                return BlockFace.SOUTH;
            }
        }
    }
    
    private EntityUtils() {
        // do not instantiate
    }
    
}
