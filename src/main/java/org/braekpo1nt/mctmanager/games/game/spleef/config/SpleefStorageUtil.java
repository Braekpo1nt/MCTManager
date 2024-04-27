package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
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

public class SpleefStorageUtil extends GameConfigStorageUtil<SpleefConfig> {
    
    protected SpleefConfig spleefConfig = null;
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
        super(configDirectory, "spleefConfig.json", SpleefConfig.class);
    }
    
    @Override
    protected SpleefConfig getConfig() {
        return spleefConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(Main.VALID_CONFIG_VERSIONS.contains(config.version()), "invalid config version (%s)", config.version());
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocations() != null, "startingLocations can't be null");
        Preconditions.checkArgument(config.startingLocations().size() >= 1, "startingLocations must have at least one entry");
        Preconditions.checkArgument(!config.startingLocations().contains(null), "startingLocations can't contain any null elements");
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.spectatorArea().toBoundingBox().getVolume() >= 1.0, "spectatorArea (%s) must have a volume (%s) of at least 1.0", config.spectatorArea(), config.spectatorArea().toBoundingBox().getVolume());
        Preconditions.checkArgument(config.layers() != null, "layers can't be null");
        int numberOfLayers = config.layers().size();
        Preconditions.checkArgument(numberOfLayers >= 2, "there must be at least 2 layers");
        for (SpleefConfig.Layer layer : config.layers()) {
            Preconditions.checkArgument(layer != null, "layers can't contain any null elements");
            Preconditions.checkArgument(layer.structure() != null, "layer.structure can't be null");
            layer.structure().isValid();
            Preconditions.checkArgument(Bukkit.getStructureManager().loadStructure(layer.structure().toNamespacedKey()) != null, "Can't find structure %s", layer.structure());
            Preconditions.checkArgument(layer.structureOrigin() != null, "layer.structureOrigin can't be null");
        }
        Preconditions.checkArgument(config.decayStages() != null, "decayStages can't be null");
        Preconditions.checkArgument(config.decayStages().size() > 0, "decayStages must have at least one entry");
        for (DecayStageDTO decayStageDTO : config.decayStages()) {
            Preconditions.checkArgument(decayStageDTO.getLayerInfos() != null, "decayStages.layers can't be null");
            // make sure index is between 0 and the max index for decayLayers 
            // also make sure there are no duplicate indexes
            Set<Integer> usedIndexes = new HashSet<>(decayStageDTO.getLayerInfos().size());
            for (DecayStageDTO.LayerInfoDTO layerInfo : decayStageDTO.getLayerInfos()) {
                Preconditions.checkArgument(0 <= layerInfo.index() && layerInfo.index() < numberOfLayers, "layerInfo.index must be at least 0, and at most 1 less than the number of elements in layers list");
                Preconditions.checkArgument(layerInfo.blocksPerSecond() >= 0, "layerInfo.blocksPerSecond must be at least 0");
                Preconditions.checkArgument(!usedIndexes.contains(layerInfo.index()), "decayStage.layerInfos entries can't have duplicate index values (%s)", layerInfo.index());
                usedIndexes.add(layerInfo.index());
            }
            Preconditions.checkArgument(decayStageDTO.getDuration() > 0, "decayStage.duration must be at least 1");
        }
        if (config.tool() != null) {
            config.tool().isValid();
        }
        Preconditions.checkArgument(config.rounds() >= 1, "rounds must be greater than 0");
        if (config.powerups() != null) {
            config.powerups().isValid();
        }
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().roundStarting() >= 0, "durations.roundStarting (%s) can't be negative", config.durations().roundStarting());
        Preconditions.checkArgument(config.durations().roundEnding() >= 0, "duration.roundEnding (%s) can't be negative", config.durations().roundEnding());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    @Override
    protected void setConfig(SpleefConfig config) {
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
        for (SpleefConfig.Layer layer : config.layers()) {
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
