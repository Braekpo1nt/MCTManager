package org.braekpo1nt.mctmanager.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    
}
