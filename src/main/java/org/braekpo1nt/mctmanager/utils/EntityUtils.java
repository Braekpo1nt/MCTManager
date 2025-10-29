package org.braekpo1nt.mctmanager.utils;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.dto.YawPitch;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class EntityUtils {
    
    /**
     * Method to calculate the direction (yaw and pitch) for a player to look at the target position from the source
     * position
     * @param source The source position from which to look (specifically the player's Location as a vector, assumes eye
     * height of 1.62 meters)
     * @param target The target position to look at
     * @return the yaw and pitch (in degrees) that an entity standing at source would need to have in order to look
     * directly at target
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
        double d = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
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
     * @return one of [{@link BlockFace#UP}, {@link BlockFace#DOWN}, {@link BlockFace#NORTH}, {@link BlockFace#SOUTH},
     * {@link BlockFace#EAST}, {@link BlockFace#WEST}] which the yaw and pitch are most aligned to
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
            if (z > 0) {
                return BlockFace.NORTH;
            } else {
                return BlockFace.SOUTH;
            }
        }
    }
    
    /**
     * @param boundingBox the bounding box to expand
     * @param player the player whose direction should determine the expansion block face
     * @param increment the amount to expand by
     * @return a clone of the given bounding box, expanded in the direction the given player is looking by the given increment
     */
    public static @NotNull BoundingBox expandBoundingBox(@NotNull BoundingBox boundingBox, Player player, double increment) {
        BlockFace direction = EntityUtils.getPlayerDirection(player.getLocation());
        return expandBoundingBox(boundingBox, direction, increment);
    }
    
    /**
     * @param boundingBox the bounding box to expand
     * @param direction the player whose direction should determine the expansion block face
     * @param increment the amount to expand by
     * @return a clone of the given bounding box, expanded in the direction the given player is looking by the given increment
     */
    public static @NotNull BoundingBox expandBoundingBox(@NotNull BoundingBox boundingBox, BlockFace direction, double increment) {
        return boundingBox.clone().expand(direction, increment);
    }
    
    /**
     * @param location the location to get the direction vector from
     * @return a normalized direction vector in the direction of the location's pitch and yaw
     */
    public static @NotNull Vector getDirection(@NotNull Location location) {
        return getDirection(location.getYaw(), location.getPitch());
    }
    
    /**
     * @param yaw the yaw for the direction vector
     * @param pitch the pitch for the direction vector
     * @return a normalized direction vector in the direction of the given pitch and yaw
     */
    public static @NotNull Vector getDirection(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        
        double x = -Math.cos(pitchRad) * Math.sin(yawRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(pitchRad) * Math.cos(yawRad);
        
        return new Vector(x, y, z).normalize();
    }
    
    /**
     * If there is at least one solid block above the given entity's position,
     * teleports the entity to the position one block above the highest solid block.
     * @param entity the entity to teleport.
     */
    public static @NotNull CommandResult top(Entity entity) {
        Location topBlock = BlockPlacementUtils.getTopBlock(entity.getLocation());
        if (topBlock == null) {
            return CommandResult.failure("You are at the top");
        }
        Location topLoc = topBlock.add(0, 1, 0);
        entity.teleport(topLoc);
        return CommandResult.success(Component.empty()
                .append(Component.text("Teleported to top ")));
    }
    
    private EntityUtils() {
        // do not instantiate
    }
    
    /**
     * @param location the location to check if it is on the ground
     * @param tolerance the tolerance (in blocks) of detection. How far down a solid block must be to assume they are on
     * the ground.
     * @return true if the nearest solid block below the given location is within the given tolerance
     */
    public static boolean isOnGround(@NotNull Location location, double tolerance) {
        Location solidBlockBelow = BlockPlacementUtils.getSolidBlockBelow(location);
        if (solidBlockBelow.equals(location)) {
            return false;
        }
        return !(solidBlockBelow.distance(location) > tolerance);
    }
}
