package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.config.ConfigStorageUtil;
import org.braekpo1nt.mctmanager.config.validation.Validation;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
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

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class SpleefStorageUtil extends ConfigStorageUtil<SpleefConfigDTO> {
    
    protected SpleefConfigDTO spleefConfig = null;
    private World world;
    private List<Location> startingLocations;
    private List<Structure> structures;
    private List<Location> structureOrigins;
    private List<BoundingBox> decayLayers;
    private List<DecayStage> decayStages;
    private Component description;
    private ItemStack tool;
    private Material stencilBlock;
    private @NotNull Material layerBlock = Material.DIRT;
    private @NotNull Material decayBlock = Material.COARSE_DIRT;
    private Map<Powerup.Source, @NotNull Double> chances;
    private Map<Powerup.Type, @NotNull Integer> initialLoadout;
    private long minTimeBetween;
    private int maxPowerups;
    /**
     * Maps a source to the weights of the types that can come from that source
     */
    private Map<Powerup.Source, Map<Powerup.Type, @NotNull Integer>> sourceToPowerupWeights;
    private Map<Powerup.Type, @Nullable Sound> userSounds;
    private Map<Powerup.Type, @Nullable Sound> affectedSounds;
    public SpleefStorageUtil(File configDirectory) {
        super(configDirectory, "spleefConfig.json", SpleefConfigDTO.class);
    }
    
    @Override
    protected SpleefConfigDTO getConfig() {
        return spleefConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfigDTO config) throws IllegalArgumentException {
        Validation.validate(config != null, "config can't be null");
        config.isValid();
        return true;
    }
    
    @Override
    protected void setConfig(SpleefConfigDTO config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        List<Location> newStartingLocations = new ArrayList<>(config.startingLocations().size());
        for (Vector startingLocation : config.startingLocations()) {
            newStartingLocations.add(startingLocation.toLocation(newWorld));
        }
        
        List<Structure> newStructures = new ArrayList<>(config.layers().size());
        List<Location> newStructureOrigins = new ArrayList<>(config.layers().size());
        Material newStencilBlock = config.stencilBlock();
        Material newLayerBlock = config.layerBlock() != null ? config.layerBlock() : Material.DIRT;
        Material newDecayBlock = config.decayBlock() != null ? config.decayBlock() : Material.COARSE_DIRT;
        List<BoundingBox> newDecayLayers = new ArrayList<>(config.layers().size());
        for (SpleefConfigDTO.Layer layer : config.layers()) {
            Preconditions.checkArgument(layer.structure() != null, "structure can't be null");
            Structure structure = Bukkit.getStructureManager().loadStructure(layer.structure().toNamespacedKey());
            Preconditions.checkArgument(structure != null, "can't find structure %s", layer.structure());
            newStructures.add(structure);
            newStructureOrigins.add(layer.structureOrigin().toLocation(newWorld));
            Preconditions.checkArgument(layer.decayArea() != null, "decayArea can't be null");
            newDecayLayers.add(layer.decayArea().toBoundingBox());
        }
        List<DecayStage> newDecayStages = DecayStageDTO.toDecayStages(config.decayStages());
        ItemStack newTool;
        if (config.tool() == null) {
            newTool = new ItemStack(Material.DIAMOND_SHOVEL);
            newTool.addEnchantment(Enchantment.DIG_SPEED, 5);
            newTool.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        } else {
            newTool = config.tool().toItemStack();
        }
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        // now it's confirmed everything works, so set the actual fields
        this.userSounds = new HashMap<>(Powerup.Type.values().length);
        this.affectedSounds = new HashMap<>(Powerup.Type.values().length);
        if (config.powerups() != null) {
            this.minTimeBetween = config.powerups().minTimeBetween();
            this.maxPowerups = config.powerups().maxPowerups();
            this.sourceToPowerupWeights = config.powerups().getSourcePowerups();
            if (config.powerups().powerups() != null) {
                for (Map.Entry<Powerup.Type, @Nullable PowerupDTO> entry : config.powerups().powerups().entrySet()) {
                    Powerup.Type type = entry.getKey();
                    PowerupDTO powerupDTO = entry.getValue();
                    if (powerupDTO != null) {
                        if (powerupDTO.getUserSound() != null) {
                            userSounds.put(type, powerupDTO.getUserSound().toSound());
                        }
                        if (powerupDTO.getAffectedSound() != null) {
                            affectedSounds.put(type, powerupDTO.getAffectedSound().toSound());
                        }
                    }
                }
            }
            this.chances = config.powerups().getChances();
            this.initialLoadout = config.powerups().getInitialLoadout();
        } else {
            this.minTimeBetween = 0L;
            this.maxPowerups = 0;
            this.sourceToPowerupWeights = getDefaultSourcePowerups();
            this.chances = getDefaultChances();
            this.initialLoadout = Collections.emptyMap();
        }
        this.world = newWorld;
        this.startingLocations = newStartingLocations;
        this.structures = newStructures;
        this.structureOrigins = newStructureOrigins;
        this.stencilBlock = newStencilBlock;
        this.layerBlock = newLayerBlock;
        this.decayBlock = newDecayBlock;
        this.decayLayers = newDecayLayers;
        this.decayStages = newDecayStages;
        this.tool = newTool;
        this.description = newDescription;
        this.spleefConfig = config;
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
    
    @Override
    protected InputStream getExampleResourceStream() {
        return SpleefStorageUtil.class.getResourceAsStream("exampleSpleefConfig.json");
    }
    
    public List<Location> getStartingLocations() {
        return startingLocations;
    }
    
    public World getWorld() {
        return world;
    }
    
    public int getRoundStartingDuration() {
        return spleefConfig.durations().roundStarting();
    }
    
    public int getSurviveScore() {
        return spleefConfig.scores().survive();
    }
    
    public List<Structure> getStructures() {
        return structures;
    }
    
    public List<Location> getStructureOrigins() {
        return structureOrigins;
    }
    
    public List<BoundingBox> getDecayLayers() {
        return decayLayers;
    }
    
    public int getRounds() {
        return spleefConfig.rounds();
    }
    
    public List<DecayStage> getStages() {
        return decayStages;
    }
    
    public Component getDescription() {
        return description;
    }
    
    public ItemStack getTool() {
        return tool;
    }
    
    public @Nullable Material getStencilBlock() {
        return stencilBlock;
    }
    
    /**
     * @return the solid block type from the config
     */
    public @NotNull Material getLayerBlock() {
        return layerBlock;
    }
    
    public @NotNull Material getDecayBlock() {
        return decayBlock;
    }
    
    public double getChance(@NotNull Powerup.Source source) {
        return chances.get(source);
    }
    
    public long getMinTimeBetween() {
        return minTimeBetween;
    }
    
    public int getMaxPowerups() {
        return maxPowerups;
    }
    
    /**
     * @param source the source that the powerups should come from
     * @return the weights for the powerups which come from the given source (all powerup weights if source is null). The key is the powerup which can come from the source, the value is the weight. 
     */
    public @NotNull Map<Powerup.Type, @NotNull Integer> getPowerupWeights(@Nullable Powerup.Source source) {
        return sourceToPowerupWeights.get(source);
    }
    
    public @Nullable Sound getUserSound(Powerup.Type type) {
        return userSounds.get(type);
    }
    
    public @Nullable Sound getAffectedSound(Powerup.Type type) {
        return affectedSounds.get(type);
    }
    
    public Map<Powerup.Type, Integer> getInitialLoadout() {
        return initialLoadout;
    }
}
