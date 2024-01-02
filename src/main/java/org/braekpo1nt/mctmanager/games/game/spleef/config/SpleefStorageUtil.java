package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpleefStorageUtil extends GameConfigStorageUtil<SpleefConfig> {
    
    protected SpleefConfig spleefConfig = null;
    private World world;
    private List<Location> startingLocations;
    private List<Structure> structures;
    private List<Location> structureOrigins;
    private List<BoundingBox> decayLayers;
    
    public SpleefStorageUtil(File configDirectory) {
        super(configDirectory, "spleefConfig.json", SpleefConfig.class);
    }
    
    @Override
    protected SpleefConfig getConfig() {
        return spleefConfig;
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
        List<BoundingBox> newDecayLayers = new ArrayList<>(config.layers().size());
        for (SpleefConfig.Layer layer : config.layers()) {
            Structure structure = Bukkit.getStructureManager().loadStructure(layer.structure());
            Preconditions.checkArgument(structure != null, "can't find structure %s", layer.structure());
            newStructures.add(structure);
            newStructureOrigins.add(layer.structureOrigin().toLocation(newWorld));
            newDecayLayers.add(layer.decayArea().toBoundingBox());
        }
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocations = newStartingLocations;
        this.structures = newStructures;
        this.structureOrigins = newStructureOrigins;
        this.decayLayers = newDecayLayers;
        this.spleefConfig = config;
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
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
            Preconditions.checkArgument(Bukkit.getStructureManager().loadStructure(layer.structure()) != null, "Can't find structure %s", layer.structure());
            Preconditions.checkArgument(layer.structureOrigin() != null, "layer.structureOrigin can't be null");
            Preconditions.checkArgument(layer.decayArea() != null, "layer.decayArea can't be null");
        }
        Preconditions.checkArgument(config.decayStages() != null, "decayStages can't be null");
        Preconditions.checkArgument(config.decayStages().size() > 0, "decayStages must have at least one entry");
        for (DecayStage decayStage : config.decayStages()) {
            Preconditions.checkArgument(decayStage.layerInfos() != null, "decayStages.layers can't be null");
            // make sure index is between 0 and the max index for decayLayers 
            // also make sure there are no duplicate indexes
            Set<Integer> usedIndexes = new HashSet<>(decayStage.layerInfos().size());
            for (DecayStage.LayerInfo layerInfo : decayStage.layerInfos()) {
                Preconditions.checkArgument(0 <= layerInfo.index() && layerInfo.index() < numberOfLayers, "layerInfo.index must be at least 0, and at most 1 less than the number of elements in layers list");
                Preconditions.checkArgument(layerInfo.blocksPerSecond() >= 0, "layerInfo.blocksPerSecond must be at least 0");
                Preconditions.checkArgument(!usedIndexes.contains(layerInfo.index()), "decayStage.layerInfos entries can't have duplicate index values (%s)", layerInfo.index());
                usedIndexes.add(layerInfo.index());
            }
            Preconditions.checkArgument(decayStage.duration() > 0, "decayStage.duration must be at least 1");
        }
        Preconditions.checkArgument(config.rounds() >= 1, "rounds must be greater than 0");
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
        return spleefConfig.decayStages();
    }
}
