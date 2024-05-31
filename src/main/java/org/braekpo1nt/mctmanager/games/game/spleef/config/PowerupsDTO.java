package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param minTimeBetween the minimum time (in milliseconds) between getting powerups. Players should not get two powerups one after another immediately. 0 means no restriction. Defaults to 0
 * @param maxPowerups    limit the number of powerups a player can have. If they are at max, they won't collect any more until they use some of them. 0 means players can't hold any powerups at all. Negative values indicate unlimited powerup collection. Defaults to 0
 * @param initialLoadout the initial loadout of powerups in the participant's inventories at the start of every round. Null means empty. The key is the type powerup, the value is how many of that powerup the players are given.
 * @param powerups       configuration of powerup attributes, such as the sounds that come from them.
 * @param sources        configuration of the sources, such as their likelihood of giving a powerup and what powerups come from it.
 */
record PowerupsDTO(long minTimeBetween, int maxPowerups, @Nullable Map<Powerup.Type, @Nullable Integer> initialLoadout,
                   @Nullable Map<Powerup.Type, @Nullable PowerupDTO> powerups,
                   @Nullable Map<Powerup.Source, @Nullable SourceDTO> sources) implements Validatable {
    
    @NotNull Map<Powerup.Type, @NotNull Integer> getInitialLoadout() {
        if (initialLoadout == null) {
            return Collections.emptyMap();
        }
        return initialLoadout.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Configuration of each source, namely the chance of it giving a powerup upon activation, the types that can come from it, and their weights.
     */
    @Data
    static class SourceDTO implements Validatable {
        /**
         * the percent chance of this source giving a powerup every time it is used. 0 or fewer means no powerups will be given from this source. Defaults to -1.
         */
        private double chance = -1;
        /**
         * the types which can come from this source paired with their weights from this source. If null, all types can come from this source. If empty, no types can come from this source. Must not contain null keys or values.
         */
        private @Nullable Map<Powerup.@Nullable Type, @Nullable Integer> types;
        
        public void validate(@NotNull Validator validator) {
            validator.validate(chance <= 1.0, "chance can't be greater than 1.0");
            if (types != null) {
                validator.validate(!types.containsKey(null), "types can't contain null keys");
                validator.validate(!types.containsValue(null), "types can't contain null values");
            }
        }
    }
    
    public void validate(@NotNull Validator validator) {
        validator.validate(minTimeBetween >= 0, "minTimeBetween must be greater than or equal to 0");
        if (initialLoadout != null) {
            validator.validate(!initialLoadout.containsKey(null), "initialLoadout can't have null keys");
            validator.validate(!initialLoadout.containsValue(null), "initialLoadout can't have null entries");
        }
        
        if (powerups != null) {
            validator.validateMap(this.powerups, "powerups");
        }
        
        if (sources != null) {
            validator.validateMap(this.sources, "sources");
        }
    }
    
    /**
     * Each key is mapped to a type-to-weight map, where the keys are the types which can come from the respective source key, and the values are the weights of those types. The weights are used to randomly choose a powerup from the given source.
     * If the
     *
     * @return a map from every {@link Powerup.Source} to the {@link Powerup.Type}+weight pairs which come from the source.
     */
    @NotNull Map<Powerup.Source, Map<Powerup.Type, @NotNull Integer>> getSourcePowerups() {
        Map<Powerup.Source, Map<Powerup.Type, Integer>> result = PowerupsDTO.getDefaultSourcePowerups();
        if (sources == null) {
            return result;
        }
        for (Map.Entry<Powerup.Source, @Nullable SourceDTO> entry : sources.entrySet()) {
            Powerup.Source source = entry.getKey();
            SourceDTO sourceDTO = entry.getValue();
            if (sourceDTO != null) {
                result.put(source, sourceDTO.getTypes());
            }
        }
        return result;
    }
    
    /**
     * @return a map from every {@link Powerup.Source} to a map of every {@link Powerup.Type} value to a weight of 1
     */
    static @NotNull Map<Powerup.Source, Map<Powerup.Type, Integer>> getDefaultSourcePowerups() {
        Map<Powerup.Type, Integer> weights = new HashMap<>();
        for (Powerup.Type value : Powerup.Type.values()) {
            weights.put(value, 1);
        }
        Map<Powerup.Source, Map<Powerup.Type, Integer>> result = new HashMap<>();
        for (Powerup.Source source : Powerup.Source.values()) {
            result.put(source, weights);
        }
        return result;
    }
    
    /**
     * @return each {@link Powerup.Source} paired with the chance it has to give a powerup upon activation.
     */
    @NotNull Map<Powerup.Source, @NotNull Double> getChances() {
        if (sources == null) {
            return PowerupsDTO.getDefaultChances();
        }
        Map<Powerup.Source, Double> result = new HashMap<>();
        for (Map.Entry<Powerup.Source, SourceDTO> entry : sources.entrySet()) {
            Powerup.Source source = entry.getKey();
            SourceDTO sourceDTO = entry.getValue();
            if (sourceDTO != null) {
                result.put(source, sourceDTO.chance);
            } else {
                result.put(source, -1.0);
            }
        }
        return result;
    }
    
    /**
     * @return a map of every source to a chance of -1 (i.e. no chance)
     */
    static Map<Powerup.Source, Double> getDefaultChances() {
        Map<Powerup.Source, Double> result = new HashMap<>();
        for (Powerup.Source source : Powerup.Source.values()) {
            result.put(source, -1.0);
        }
        return result;
    }
}
