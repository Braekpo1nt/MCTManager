package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 
 * @param world the world the game is in
 * @param startingLocations a set of starting locations that the players will be sent to a random one of
 * @param spectatorArea the area the spectators shouldn't be able to leave
 * @param stencilBlock the stencil block which the structures should have inside them to be replaced with the layerBlock upon start. If this is null, no replacement will be made (this is useful if your layer structures are already made of the stencil).
 * @param layerBlock the block that the stencil will be replaced with upon start which the floors are to be made of. The area(s) of replacement will be the BoundingBox formed by the layers decayAreas. If this is null, dirt will be used.
 * @param decayBlock the block type that blocks decay to before disappearing. If this is null, coarse dirt will be used.
 * @param layers the layers of spleef
 * @param decayStages the stages of decay (must have at least 1). The last stage will go on forever, regardless of the duration or minParticipants values
 * @param tool the tool players receive to break the blocks (if null, a diamond shovel will be used).
 * @param rounds the number of rounds
 * @param scores the scores for spleef
 * @param durations the durations for spleef
 * @param description the description of spleef
 */
record SpleefConfig(String version, String world, List<Vector> startingLocations, BoundingBoxDTO spectatorArea, @Nullable Material stencilBlock, @Nullable Material layerBlock, @Nullable Material decayBlock, List<Layer> layers, List<DecayStage> decayStages, @Nullable ItemStackDTO tool, int rounds, Powerups powerups, Scores scores, Durations durations, JsonElement description) {
    
    /**
     * @param structure the NamespacedKey of the structure to place for this layer
     * @param structureOrigin the origin to place the structure at
     * @param decayArea the area in which to decay blocks for this layer. If this is null, the size of the structure and structureOrigin will be used as the area.
     */
    record Layer(@Nullable NamespacedKeyDTO structure, Vector structureOrigin, @Nullable BoundingBoxDTO decayArea) {
    }
    
    /**
     * 
     * @param minTimeBetween the minimum time (in milliseconds) between getting powerups. Players should not get two powerups one after another immediately. 0 means no restriction. Defaults to 0
     * @param maxPowerups limit the number of powerups a player can have. If they are at max, they won't collect any more until they use some of them. 0 means players can't hold any powerups at all. Negative values indicate unlimited powerup collection. Defaults to 0
     */
    record Powerups(long minTimeBetween, int maxPowerups, @Nullable Map<Powerup.Type, @Nullable PowerupDTO> powerups, @Nullable Map<Powerup.Source, @Nullable SourceDTO> sources) {
        
        @Getter
        static class SourceDTO {
            /**
             * the percent chance of this source giving a powerup every time it is used. 0 or fewer means no powerups will be given from this source. Defaults to -1.
             */
            private double chance;
            /**
             * the types which can come from this source paired with their weights from this source. If null, all types can come from this source. If empty, no types can come from this source. Must not contain null keys or values.
             */
            private @Nullable Map<Powerup.@Nullable Type, @Nullable Integer> types;
            
            void isValid() {
                Preconditions.checkArgument(chance <= 1.0, "chance can't be greater than 1.0");
                if (types != null) {
                    Preconditions.checkArgument(!types.containsKey(null), "types can't contain null keys");
                    Preconditions.checkArgument(!types.containsValue(null), "types can't contain null values");
                }
            }
        }
        
        void isValid() {
            Preconditions.checkArgument(minTimeBetween >= 0, "minTimeBetween must be greater than or equal to 0");
            if (powerups != null) {
                for (PowerupDTO powerupDTO : powerups.values()) {
                    if (powerupDTO != null) {
                        powerupDTO.isValid();
                    }
                }
            }
            
            if (sources != null) {
                Preconditions.checkArgument(!sources.containsKey(null), "sources can't have null keys");
                Preconditions.checkArgument(!sources.containsValue(null), "sources can't have null entries");
                for (SourceDTO sourceDTO : sources.values()) {
                    if (sourceDTO != null) {
                        sourceDTO.isValid();
                    }
                }
            }
        }
        
        /**
         * Each key is mapped to a type-to-weight map, where the keys are the types which can come from the respective source key, and the values are the weights of those types. The weights are used to randomly choose a powerup from the given source.
         * If the 
         * @return a map from every {@link Powerup.Source} to the {@link Powerup.Type}+weight pairs which come from the source. 
         */
        @NotNull Map<Powerup.Source, Map<Powerup.Type, Integer>> getSourcePowerups() {
            Map<Powerup.Source, Map<Powerup.Type, Integer>> result = SpleefStorageUtil.getDefaultSourcePowerups();
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
        
        Map<Powerup.Source, Double> getChances() {
            if (sources == null) {
                return SpleefStorageUtil.getDefaultChances();
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
    }
    
    record Scores(int survive) {
    }
    
    record Durations(int roundStarting, int roundEnding) {
    }
}
