package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Location;

public class MathUtils {
    
    private MathUtils() {
        // do not instantiate
    }
    
    
    /**
     * Rounds a given float value to the closest multiple of the specified increment
     * @param value The float value to be rounded
     * @param increment The increment to which the value should be rounded
     * @return The closest number to the input value that is a multiple of the increment
     */
    public static float specialRound(float value, float increment) {
        float multiple = Math.round(value / increment);
        return multiple * increment;
    }
    
    /**
     * Rounds a given double value to the closest multiple of the specified increment
     * @param value The double value to be rounded
     * @param increment The increment to which the value should be rounded
     * @return The closest number to the input value that is a multiple of the increment
     */
    public static double specialRound(double value, double increment) {
        float multiple = Math.round(value / increment);
        return multiple * increment;
    }
    
    /**
     * 
     * @param value the value to return the rounded version of
     * @param positionIncrement the increment to which the position values (x, y, z) should be rounded
     * @param rotationIncrement the increment (in degrees) to which the rotation values (yaw and pitch) should be rounded
     * @return A {@link Location} with values set to the closest number to the input value which is a multiple of the appropriate increment. 
     */
    public static Location specialRound(Location value, double positionIncrement, float rotationIncrement) {
        return new Location(
                value.getWorld(),
                MathUtils.specialRound(value.getX(), positionIncrement),
                MathUtils.specialRound(value.getY(), positionIncrement),
                MathUtils.specialRound(value.getZ(), positionIncrement),
                MathUtils.specialRound(value.getYaw(), rotationIncrement),
                MathUtils.specialRound(value.getPitch(), rotationIncrement)
        );
    }
    
}
