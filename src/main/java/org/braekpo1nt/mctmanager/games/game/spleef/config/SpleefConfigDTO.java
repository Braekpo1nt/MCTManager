package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.config.validation.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
record SpleefConfigDTO(String version, String world, List<Vector> startingLocations, BoundingBoxDTO spectatorArea, @Nullable Material stencilBlock, @Nullable Material layerBlock, @Nullable Material decayBlock, List<Layer> layers, List<DecayStageDTO> decayStages, @Nullable ItemStackDTO tool, int rounds, Powerups powerups, Scores scores, Durations durations, Component description) implements Validatable {
    
    @Override
    public void validate(Validator validator) throws ConfigInvalidException {
        validator.validate(this.version() != null, "version can't be null");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version()), "invalid config version (%s)", this.version());
        validator.validate(Bukkit.getWorld(this.world()) != null, "world: Could not find world \"%s\"", this.world());
        validator.validate(this.startingLocations() != null, "startingLocations can't be null");
        validator.validate(this.startingLocations.size() >= 1, "startingLocations must have at least one entry");
        validator.validate(!this.startingLocations.contains(null), "startingLocations can't contain any null elements");
        validator.validate(this.spectatorArea() != null, "spectatorArea can't be null");
        validator.validate(this.spectatorArea.toBoundingBox().getVolume() >= 1.0, "spectatorArea (%s) must have a volume (%s) of at least 1.0", this.spectatorArea(), this.spectatorArea.toBoundingBox().getVolume());
        validator.validate(this.layers != null, "layers can't be null");
        int numberOfLayers = this.layers.size();
        validator.validate(numberOfLayers >= 2, "there must be at least 2 layers");
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            validator.notNull(layer, "layers[%d]", i);
            layer.validate(validator.path("layers[%d]", i));
        }
        
        validator.notNull(this.decayStages, "decayStages");
        validator.validate(this.decayStages.size() > 0, "decayStages must have at least one entry");
        for (int i = 0; i < decayStages.size(); i++) {
            DecayStageDTO decayStageDTO = decayStages.get(i);
            validator.notNull(decayStageDTO, "decayStages[%d]", i);
            decayStageDTO.validate(validator.path("decayStages[%d]", i));
            decayStageDTO.validateIndexes(validator.path("decayStages[%d]", i), numberOfLayers);
        }
        
        if (this.tool() != null) {
            this.tool().validate(validator.path("tool"));
        }
        validator.validate(this.rounds() >= 1, "rounds must be greater than 0");
        if (this.powerups != null) {
            powerups.validate(validator.path("powerups"));
        }
        validator.validate(this.scores() != null, "scores can't be null");
        validator.validate(this.durations() != null, "durations can't be null");
        validator.validate(this.durations.roundStarting() >= 0, "durations.roundStarting (%s) can't be negative", this.durations.roundStarting());
        validator.validate(this.durations.roundEnding() >= 0, "duration.roundEnding (%s) can't be negative", this.durations.roundEnding());
        validator.notNull(this.description, "description");
    }
    
    /**
     * @param structure the NamespacedKey of the structure to place for this layer
     * @param structureOrigin the origin to place the structure at
     * @param decayArea the area in which to decay blocks for this layer. If this is null, the size of the structure and structureOrigin will be used as the area.
     */
    record Layer(@Nullable NamespacedKeyDTO structure, Vector structureOrigin, @Nullable BoundingBoxDTO decayArea) implements Validatable {
        @Override
        public void validate(Validator validator) throws ConfigInvalidException {
            validator.validate(this.structure() != null, "layer.structure can't be null");
            Preconditions.checkArgument(this.structure != null);
            this.structure.validate(validator);
            validator.validate(Bukkit.getStructureManager().loadStructure(this.structure.toNamespacedKey()) != null, "Can't find structure %s", this.structure());
            validator.validate(this.structureOrigin() != null, "layer.structureOrigin can't be null");
        }
    }
    
    /**
     * 
     * @param minTimeBetween the minimum time (in milliseconds) between getting powerups. Players should not get two powerups one after another immediately. 0 means no restriction. Defaults to 0
     * @param maxPowerups limit the number of powerups a player can have. If they are at max, they won't collect any more until they use some of them. 0 means players can't hold any powerups at all. Negative values indicate unlimited powerup collection. Defaults to 0
     * @param initialLoadout the initial loadout of powerups in the participant's inventories at the start of every round. Null means empty. The key is the type powerup, the value is how many of that powerup the players are given.
     * @param powerups configuration of powerup attributes, such as the sounds that come from them.
     * @param sources configuration of the sources, such as their likelihood of giving a powerup and what powerups come from it. 
     */
    record Powerups(long minTimeBetween, int maxPowerups, @Nullable Map<Powerup.Type, @Nullable Integer> initialLoadout, @Nullable Map<Powerup.Type, @Nullable PowerupDTO> powerups, @Nullable Map<Powerup.Source, @Nullable SourceDTO> sources) implements Validatable {
        
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
            
            public void validate(Validator validator) {
                validator.validate(chance <= 1.0, "chance can't be greater than 1.0");
                if (types != null) {
                    validator.validate(!types.containsKey(null), "types can't contain null keys");
                    validator.validate(!types.containsValue(null), "types can't contain null values");
                }
            }
        }
        
        public void validate(Validator validator) {
            validator.validate(minTimeBetween >= 0, "minTimeBetween must be greater than or equal to 0");
            if (initialLoadout != null) {
                validator.validate(!initialLoadout.containsKey(null), "initialLoadout can't have null keys");
                validator.validate(!initialLoadout.containsValue(null), "initialLoadout can't have null entries");
            }
            
            if (powerups != null) {
                for (Map.Entry<Powerup.Type, PowerupDTO> entry : powerups.entrySet()) {
                    entry.getValue().validate(validator.path("powerups[%s]", entry.getKey()));
                }
            }
            
            if (sources != null) {
                validator.validate(!sources.containsKey(null), "sources can't have null keys");
                validator.validate(!sources.containsValue(null), "sources can't have null entries");
                for (Map.Entry<Powerup.Source, SourceDTO> entry : sources.entrySet()) {
                    SourceDTO sourceDTO = entry.getValue();
                    if (sourceDTO != null) {
                        sourceDTO.validate(validator.path("sources[%s]", entry.getKey()));
                    }
                }
            }
        }
        
        /**
         * Each key is mapped to a type-to-weight map, where the keys are the types which can come from the respective source key, and the values are the weights of those types. The weights are used to randomly choose a powerup from the given source.
         * If the 
         * @return a map from every {@link Powerup.Source} to the {@link Powerup.Type}+weight pairs which come from the source. 
         */
        @NotNull Map<Powerup.Source, Map<Powerup.Type, @NotNull Integer>> getSourcePowerups() {
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
        
        /**
         * @return each {@link Powerup.Source} paired with the chance it has to give a powerup upon activation. 
         */
        @NotNull Map<Powerup.Source, @NotNull Double> getChances() {
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
    
    /**
     * @param survive the score given to every living player each time a single player dies. Players on the same team as the player who died will not receive points. w
     */
    record Scores(int survive) {
    }
    
    record Durations(int roundStarting, int roundEnding) {
    }
}
