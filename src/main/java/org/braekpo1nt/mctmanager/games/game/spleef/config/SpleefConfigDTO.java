package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.braekpo1nt.mctmanager.geometry.CompositeGeometry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
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
 * @param preventInteractions
 * @param safetyArea the area where players should be kept before the game starts, to stop them from falling off too early
 * @param scores the scores for spleef
 * @param durations the durations for spleef
 * @param description the description of spleef
 */
record SpleefConfigDTO(
        String version, 
        String world, 
        List<Vector> startingLocations, 
        BoundingBoxDTO spectatorArea, 
        @Nullable Material stencilBlock, 
        @Nullable Material layerBlock, 
        @Nullable Material decayBlock, 
        List<LayerDTO> layers, 
        List<DecayStageDTO> decayStages, 
        @Nullable ItemStackDTO tool, 
        int rounds, 
        PowerupsDTO powerups, 
        @Nullable List<Material> preventInteractions,
        @Nullable CompositeGeometry safetyArea,
        Scores scores, 
        Durations durations, 
        Component description) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) throws ConfigInvalidException {
        validator.validate(this.version() != null, "version can't be null");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version()), "invalid config version (%s)", this.version());
        validator.validate(Bukkit.getWorld(this.world()) != null, "world: Could not find world \"%s\"", this.world());
        validator.validate(this.startingLocations() != null, "startingLocations can't be null");
        validator.validate(!this.startingLocations.isEmpty(), "startingLocations must have at least one entry");
        validator.validate(!this.startingLocations.contains(null), "startingLocations can't contain any null elements");
        validator.validate(this.spectatorArea() != null, "spectatorArea can't be null");
        validator.validate(this.spectatorArea.toBoundingBox().getVolume() >= 1.0, "spectatorArea (%s) must have a volume (%s) of at least 1.0", this.spectatorArea(), this.spectatorArea.toBoundingBox().getVolume());
        validator.validate(this.layers != null, "layers can't be null");
        int numberOfLayers = this.layers.size();
        validator.validate(numberOfLayers >= 2, "there must be at least 2 layers");
        validator.validateList(this.layers, "layers");
        
        validator.notNull(this.decayStages, "decayStages");
        validator.validate(!this.decayStages.isEmpty(), "decayStages must have at least one entry");
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
        if (safetyArea != null) {
            validator.validate(safetyArea.isCohesive(), "safetyArea: each geometry must overlap with at least one other geometry");
        }
        validator.validate(this.scores() != null, "scores can't be null");
        validator.validate(this.durations() != null, "durations can't be null");
        validator.validate(this.durations.roundStarting() >= 0, "durations.roundStarting (%s) can't be negative", this.durations.roundStarting());
        validator.validate(this.durations.roundEnding() >= 0, "duration.roundEnding (%s) can't be negative", this.durations.roundEnding());
        validator.notNull(this.description, "description");
    }
    
    public SpleefConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        List<Structure> newStructures = new ArrayList<>(this.layers.size());
        List<Location> newStructureOrigins = new ArrayList<>(this.layers.size());
        List<BoundingBox> newDecayLayers = new ArrayList<>(this.layers.size());
        for (LayerDTO layer : this.layers) {
            Preconditions.checkArgument(layer.structure() != null, "structure can't be null");
            Structure structure = Bukkit.getStructureManager().loadStructure(layer.structure().toNamespacedKey());
            Preconditions.checkArgument(structure != null, "can't find structure %s", layer.structure());
            newStructures.add(structure);
            newStructureOrigins.add(layer.structureOrigin().toLocation(newWorld));
            Preconditions.checkArgument(layer.decayArea() != null, "decayArea can't be null");
            newDecayLayers.add(layer.decayArea().toBoundingBox());
        }
        
        return SpleefConfig.builder()
                .world(newWorld)
                .startingLocations(this.startingLocations.stream().map(loc -> loc.toLocation(newWorld)).toList())
                .structures(newStructures)
                .structureOrigins(newStructureOrigins)
                .decayLayers(newDecayLayers)
                .stencilBlock(this.stencilBlock)
                .layerBlock(this.getLayerBlock())
                .decayBlock(this.getDecayBlock())
                .chances(this.getChances())
                .tool(this.getTool())
                .minTimeBetween(this.getMinTimeBetween())
                .maxPowerups(this.getMaxPowerups())
                .initialLoadout(this.getInitialLoadout())
                .sourceToPowerupWeights(this.getSourcePowerups())
                .userSounds(this.getUserSounds())
                .affectedSounds(this.getAffectedSounds())
                .stages(DecayStageDTO.toDecayStages(this.decayStages))
                .rounds(this.rounds)
                .surviveScore(this.scores.survive)
                .roundStartingDuration(this.durations.roundStarting)
                .roundEndingDuration(this.durations.roundEnding)
                .descriptionDuration(this.durations.description)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .description(this.description)
                .build();
    }
    
    private Map<Powerup.Type, Sound> getUserSounds() {
        HashMap<Powerup.Type, Sound> userSounds = new HashMap<>(Powerup.Type.values().length);
        if (this.powerups != null) {
            if (this.powerups.powerups() != null) {
                for (Map.Entry<Powerup.Type, @Nullable PowerupDTO> entry : this.powerups.powerups().entrySet()) {
                    Powerup.Type type = entry.getKey();
                    PowerupDTO powerupDTO = entry.getValue();
                    if (powerupDTO != null) {
                        if (powerupDTO.getUserSound() != null) {
                            userSounds.put(type, powerupDTO.getUserSound().toSound());
                        }
                    }
                }
            }
        }
        return userSounds;
    }
    
    private Map<Powerup.Type, Sound> getAffectedSounds() {
        HashMap<Powerup.Type, Sound> affectedSounds = new HashMap<>(Powerup.Type.values().length);
        if (this.powerups != null) {
            if (this.powerups.powerups() != null) {
                for (Map.Entry<Powerup.Type, @Nullable PowerupDTO> entry : this.powerups.powerups().entrySet()) {
                    Powerup.Type type = entry.getKey();
                    PowerupDTO powerupDTO = entry.getValue();
                    if (powerupDTO != null) {
                        if (powerupDTO.getAffectedSound() != null) {
                            affectedSounds.put(type, powerupDTO.getAffectedSound().toSound());
                        }
                    }
                }
            }
        }
        return affectedSounds;
    }
    
    @NotNull ItemStack getTool() {
        if (this.tool() == null) {
            ItemStack newTool = new ItemStack(Material.DIAMOND_SHOVEL);
            newTool.addEnchantment(Enchantment.DIG_SPEED, 5);
            newTool.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
            return newTool;
        }
        return this.tool().toItemStack();
    }
    
    private Map<Powerup.Source, Double> getChances() {
        if (this.powerups != null) {
            return this.powerups.getChances();
        }
        Map<Powerup.Source, Double> result = new HashMap<>();
        for (Powerup.Source source : Powerup.Source.values()) {
            result.put(source, -1.0);
        }
        return result;
    }
    
    private long getMinTimeBetween() {
        if (this.powerups != null) {
            return this.powerups.minTimeBetween();
        }
        return 0L;
    }
    
    private int getMaxPowerups() {
        if (this.powerups != null) {
            return this.powerups.maxPowerups();
        }
        return 0;
    }
    
    private Map<Powerup.Type, Integer> getInitialLoadout() {
        if (this.powerups != null) {
            return this.powerups.getInitialLoadout();
        }
        return Collections.emptyMap();
    }
    
    @NotNull Material getLayerBlock() {
        return this.layerBlock == null ? Material.DIRT : this.layerBlock;
    }
    
    @NotNull Material getDecayBlock() {
        return this.decayBlock == null ? Material.COARSE_DIRT : this.decayBlock;
    }
    
    private @NotNull Map<Powerup.Source, Map<Powerup.Type, Integer>> getSourcePowerups() {
        if (this.powerups != null) {
            return this.powerups.getSourcePowerups();
        }
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
     * @param survive the score given to every living player each time a single player dies. Players on the same team as the player who died will not receive points. w
     */
    record Scores(int survive) {
    }
    
    record Durations(int roundStarting, int roundEnding, int description) {
    }
}
