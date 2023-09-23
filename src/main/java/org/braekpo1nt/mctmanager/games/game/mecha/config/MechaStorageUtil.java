package org.braekpo1nt.mctmanager.games.game.mecha.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.loot.LootTable;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MechaStorageUtil extends GameConfigStorageUtil<MechaConfig> {
    
    protected MechaConfig mechaConfig = getExampleConfig();
    private Map<LootTable, Integer> weightedMechaLootTables;
    private World world;
    private Location platformsOrigin;
    private Structure platformsStructure;
    private Structure platformsRemovedStructure;
    private Component description;

    public MechaStorageUtil(File configDirectory) {
        super(configDirectory, "mechaConfig.json", MechaConfig.class);
    }
    
    @Override
    protected MechaConfig getConfig() {
        return mechaConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable MechaConfig config) {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, 
                "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), 
                "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, 
                "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.spectatorArea() != null, 
                "spectatorArea can't be null");
        Preconditions.checkArgument(config.spectatorArea().toBoundingBox().getVolume() >= 1.0, 
                "getSpectatorArea's volume (%s) can't be less than 1. %s", config.spectatorArea().toBoundingBox().getVolume(), config.spectatorArea().toBoundingBox());
        Preconditions.checkArgument(config.removeArea() != null, 
                "removeArea can't be null");
        Preconditions.checkArgument(config.removeArea().toBoundingBox().getVolume() >= 1.0, 
                "removeArea (%s) volume (%s) can't be less than 1.0", config.removeArea().toBoundingBox(), config.removeArea().toBoundingBox().getVolume());
        Preconditions.checkArgument(config.initialBorderSize() >= 1.0, 
                "initialBorderSize can't be less than 1.0: %s", config.initialBorderSize());
        Preconditions.checkArgument(config.borderStages() != null, 
                "borderStages can't be null");
        Preconditions.checkArgument(config.borderStages().size() >= 1, 
                "borderStages must have at least one stage");
        for (MechaConfig.BorderStage borderStage : config.borderStages()) {
            Preconditions.checkArgument(borderStage.size() >= 1.0, 
                    "border stage size (%s) can't be less than 1.0", borderStage.size());
            Preconditions.checkArgument(borderStage.delay() >= 0, 
                    "borderStage.delay (%S) can't be negative", borderStage.delay());
            Preconditions.checkArgument(borderStage.duration() >= 0, 
                    "borderStage.duration (%S) can't be negative", borderStage.duration());
        }
        Preconditions.checkArgument(lootTableExists(config.spawnLootTable()), 
                "Could not find spawn loot table \"%s\"", config.spawnLootTable());
        Preconditions.checkArgument(config.weightedMechaLootTables() != null, 
                "weightedMechaLootTables can't be null");
        Preconditions.checkArgument(config.weightedMechaLootTables().size() >= 1, 
                "weightedMechaLootTables must have at least 1 entry");
        for (MechaConfig.WeightedNamespacedKey weightedNamespacedKey : config.weightedMechaLootTables()) {
            Preconditions.checkArgument(weightedNamespacedKey.namespace() != null, 
                    "weightedNamespacedKey.namespace can't be null");
            Preconditions.checkArgument(weightedNamespacedKey.key() != null, 
                    "weightedNamespacedKey.key can't be null");
            NamespacedKey namespacedKey = new NamespacedKey(weightedNamespacedKey.namespace(), weightedNamespacedKey.key());
            Preconditions.checkArgument(lootTableExists(namespacedKey), 
                    "Could not find loot table \"%s\"", namespacedKey);
            Preconditions.checkArgument(weightedNamespacedKey.weight() >= 1, 
                    "weightedNamespacedKey (%s) can't have a weight (%s) less than 1", namespacedKey, weightedNamespacedKey.weight());
        }
        Preconditions.checkArgument(config.spawnChestCoords() != null, 
                "spawnChestCoords can't be null");
        Preconditions.checkArgument(!config.spawnChestCoords().contains(null), 
                "spawnChestCoords can't contain a null position");
        for (Vector pos : config.spawnChestCoords()) {
            Preconditions.checkArgument(config.removeArea().toBoundingBox().contains(pos), 
                    "spawnChestCoord (%s) is not inside removeArea (%s)", pos, config.removeArea().toBoundingBox());
        }
        Preconditions.checkArgument(config.mapChestCoords() != null, 
                "mapChestCoords can't be null");
        Preconditions.checkArgument(!config.mapChestCoords().contains(null), 
                "mapChestCoords can't contain a null position");
        for (Vector pos : config.mapChestCoords()) {
            Preconditions.checkArgument(config.removeArea().toBoundingBox().contains(pos), 
                    "mapChestCoord (%s) is not inside removeArea (%s)", pos, config.removeArea().toBoundingBox());
        }
        Preconditions.checkArgument(config.platformsStructure() != null, 
                "platformsStructure can't be null");
        Preconditions.checkArgument(Bukkit.getStructureManager().loadStructure(config.platformsStructure()) != null, 
                "Can't find platformsStructure %s", config.platformsStructure());
        Preconditions.checkArgument(config.platformsRemovedStructure() != null, 
                "platformsRemovedStructure can't be null");
        Preconditions.checkArgument(Bukkit.getStructureManager().loadStructure(config.platformsRemovedStructure()) != null, 
                "Can't find platformsRemovedStructure %s", config.platformsRemovedStructure());
        Preconditions.checkArgument(config.platformsOrigin() != null, 
                "platformsOrigin can't be null");
        Preconditions.checkArgument(config.scores() != null, 
                "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, 
                "durations can't be null");
        Preconditions.checkArgument(config.durations().start() >= 0, 
                "durations.start (%s) can't be negative", config.durations().start());
        Preconditions.checkArgument(config.durations().invulnerability() >= 0, 
                "durations.invulnerability (%s) can't be negative", config.durations().invulnerability());
        Preconditions.checkArgument(config.durations().end() >= 0, 
                "durations.end (%s) can't be negative", config.durations().end());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    @Override
    protected void setConfig(MechaConfig config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        List<MechaConfig.WeightedNamespacedKey> weightedNamespacedKeys = config.weightedMechaLootTables();
        HashMap<LootTable, Integer> newWeightedMechaLootTables  = new HashMap<>(weightedNamespacedKeys.size());
        for (MechaConfig.WeightedNamespacedKey weightedNamespacedKey : weightedNamespacedKeys) {
            String namespace = weightedNamespacedKey.namespace();
            String key = weightedNamespacedKey.key();
            int weight = weightedNamespacedKey.weight();
            LootTable lootTable = Bukkit.getLootTable(new NamespacedKey(namespace, key));
            newWeightedMechaLootTables.put(lootTable, weight);
        }
        Structure newPlatformsStructure = Bukkit.getStructureManager().loadStructure(config.platformsStructure());
        Preconditions.checkArgument(newPlatformsStructure != null, "Can't find platformsStructure %s", config.platformsStructure());
        Structure newPlatformsRemovedStructure = Bukkit.getStructureManager().loadStructure(mechaConfig.platformsRemovedStructure());
        Preconditions.checkArgument(newPlatformsRemovedStructure != null, "Can't find platformsRemovedStructure %s", config.platformsRemovedStructure());
        Location newPlatformsOrigin = config.platformsOrigin().toLocation(newWorld);
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.weightedMechaLootTables = newWeightedMechaLootTables;
        this.platformsStructure = newPlatformsStructure;
        this.platformsRemovedStructure = newPlatformsRemovedStructure;
        this.platformsOrigin = newPlatformsOrigin;
        this.description = newDescription;
        this.mechaConfig = config;
    }
    
    private boolean lootTableExists(@Nullable NamespacedKey lootTable) {
        return lootTable == null || Bukkit.getLootTable(lootTable) != null;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return MechaStorageUtil.class.getResourceAsStream("exampleMechaConfig.json");
    }
    
    /**
     * The coordinates of all the spawn chests
     */
    public List<Vector> getSpawnChestCoords() {
        return mechaConfig.spawnChestCoords();
    }
    
    /**
     * The coordinates of all the chests in the open world, not including spawn chests
     */
    public List<Vector> getMapChestCoords() {
        return mechaConfig.mapChestCoords();
    }
    
    /**
     * The mecha spawn loot table from the mctdatapack
     */
    public LootTable getSpawnLootTable() {
        return Bukkit.getLootTable(mechaConfig.spawnLootTable());
    }
    
    /**
     * The mecha loot tables from the mctdatapack, not including the spawn loot.
     * Each loot table is paired with a weight for random selection. 
     */
    public Map<LootTable, Integer> getWeightedMechaLootTables() {
        return this.weightedMechaLootTables;
    }
    
    public int[] getSizes() {
        List<MechaConfig.BorderStage> borderStages = mechaConfig.borderStages();
        int[] sizes = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            sizes[i] = borderStages.get(i).size();
        }
        return sizes;
    }
    
    public int[] getDelays() {
        List<MechaConfig.BorderStage> borderStages = mechaConfig.borderStages();
        int[] delays = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            delays[i] = borderStages.get(i).delay();
        }
        return delays;
    }
    
    public int[] getDurations() {
        List<MechaConfig.BorderStage> borderStages = mechaConfig.borderStages();
        int[] durations = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            durations[i] = borderStages.get(i).duration();
        }
        return durations;
    }
    
    public World getWorld() {
        return world;
    }
    
    public double getInitialBorderSize() {
        return mechaConfig.initialBorderSize();
    }

    public BoundingBox getRemoveArea() {
        return mechaConfig.removeArea().toBoundingBox();
    }
    
    public Structure getPlatformStructure() {
        return platformsStructure;
    }
    
    public Structure getPlatformRemovedStructure() {
        return platformsRemovedStructure;
    }
    
    public Location getPlatformsOrigin() {
        return platformsOrigin;
    }
    
    public Component getDescription() {
        return description;
    }
    
    public int getStartDuration() {
        return mechaConfig.durations().start();
    }
    
    public int getInvulnerabilityDuration() {
        return mechaConfig.durations().invulnerability();
    }
    
    public int getEndDuration() {
        return mechaConfig.durations().end();
    }
    
    public int getKillScore() {
        return mechaConfig.scores().kill();
    }
    
    public int getSurviveTeamScore() {
        return mechaConfig.scores().surviveTeam();
    }
}
