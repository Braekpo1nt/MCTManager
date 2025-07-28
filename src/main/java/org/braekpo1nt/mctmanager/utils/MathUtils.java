package org.braekpo1nt.mctmanager.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

public class MathUtils {
    
    private static final Random random = new Random();
    
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
    
    /**
     * Uses the weights paired with each index to select a random index using a weighted randomization algorithm. 
     * If weights is {1: w1, 2: w2, ...n: wn}, and the sum of all n weights is W,
     * then index x has a wx/W chance of being chosen.
     * @param weights a list where each index is paired with a weight.
     * @return a random index from 0 to the length of weights, or -1 if weights is empty.
     */
    public static int getWeightedRandomIndex(int[] weights) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        // random number in range [0, totalWeight)
        int randomNumber = random.nextInt(totalWeight);
        // iterate through the weights to find the corresponding index
        int weightSum = 0;
        for (int i = 0; i < weights.length; i++) {
            weightSum += weights[i];
            if (randomNumber < weightSum) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Uses the key-value pair as the value-to-weight pair for a weighted random selection.
     * If weightedMap is {v1: w1, v2: w2, ...vn: wn}, and the sum of all n weights is W,
     * then vx has a wx/W chance of being chosen.
     * @param weightedMap a map of the objects to choose from and their weights. Can't be empty. No weight can be less than 1. 
     * @return a random key from the map
     * @param <K> the type of the key object in the given map
     * @throws IllegalArgumentException if the weightedMap is empty or null
     */
    public static @NotNull <K> K getWeightedRandomValue(@NotNull Map<@NotNull K, @NotNull Integer> weightedMap) {
        Preconditions.checkArgument(!weightedMap.isEmpty(), "weightedMap can't be empty");
        int totalWeight = 0;
        Collection<Integer> weights = weightedMap.values();
        for (int weight : weights) {
            totalWeight += weight;
        }
        int randomIndex = (int) (Math.random() * totalWeight);
        int weightSum = 0;
        for (Map.Entry<K, Integer> entry : weightedMap.entrySet()) {
            int weight = entry.getValue();
            weightSum += weight;
            if (randomIndex < weightSum) {
                return entry.getKey();
            }
        }
        // this will not happen
        throw new IllegalArgumentException("weightedMap can't be empty");
    }
    
    /**
     * @param index the index to wrap
     * @param size the size to wrap around
     * @return the wrapped version of the index. e.g. if index is 1, and size is 4, returns 1; if index is 6, and size is 4, returns 1;
     */
    public static int wrapIndex(int index, int size) {
        Preconditions.checkArgument(size > 0, "size must be greater than 0");
        return (index % size + size) % size;
    }
    
    public static double getMinimumDistance(BoundingBox box, Vector point) {
        // Get the closest point on the bounding box to the given point
        double closestX = Math.max(box.getMinX(), Math.min(point.getX(), box.getMaxX()));
        double closestY = Math.max(box.getMinY(), Math.min(point.getY(), box.getMaxY()));
        double closestZ = Math.max(box.getMinZ(), Math.min(point.getZ(), box.getMaxZ()));
        
        // Create a vector for the closest point
        Vector closestPoint = new Vector(closestX, closestY, closestZ);
        
        // Calculate the distance between the point and the closest point on the bounding box
        return closestPoint.distance(point);
    }
    
    public static Point2D closestPointOnRectangle(double minX, double minZ,
                                                  double maxX, double maxZ,
                                                  double tx, double tz) {
        double distLeft   = tx - minX;
        double distRight  = maxX - tx;
        double distTop    = tz - minZ;
        double distBottom = maxZ - tz;
        
        double minDist = Math.min(Math.min(distLeft, distRight),
                Math.min(distTop, distBottom));
        
        if (minDist == distLeft) {
            return new Point2D(minX, tz);
        } else if (minDist == distRight) {
            return new Point2D(maxX, tz);
        } else if (minDist == distTop) {
            return new Point2D(tx, minZ);
        } else {
            return new Point2D(tx, maxZ);
        }
    }
    
    public record Point2D(double x, double z) {
        public Vector toVector(double y) {
            return new Vector(x, y, z);
        }
    }
    
    /**
     * 
     * @param centerX the x coord of the center of the square
     * @param centerZ the z coord of the center of the square
     * @param size the size of one side of the square (not the distance from the center)
     * @param pointX the x coord of the point to check
     * @param pointZ the z coord of the point to check
     * @return true if the given point is inside the square defined by the given center and side length
     */
    public static boolean pointIsInsideSquare(double centerX, double centerZ, double size, double pointX, double pointZ) {
        double halfSize = size / 2;
        double minX = centerX - halfSize;
        double maxX = centerX + halfSize;
        double minZ = centerZ - halfSize;
        double maxZ = centerZ + halfSize;
        return (minX <= pointX && pointX <= maxX) && (minZ <= pointZ && pointZ <= maxZ);
    }
}
