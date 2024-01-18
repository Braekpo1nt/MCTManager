package org.braekpo1nt.mctmanager.utils;

import org.braekpo1nt.mctmanager.games.game.config.YawPitch;
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
    
    private EntityUtils() {
        // do not instantiate
    }
    
}
