package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.loot.LootTable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MechaStorageUtil extends GameConfigStorageUtil<MechaConfig> {
    
    protected MechaConfig mechaConfig = getExampleConfig();
    private Map<LootTable, Integer> weightedMechaLootTables;
    private World world;
    
    public MechaStorageUtil(File configDirectory) {
        super(configDirectory, "mechaConfig.json", MechaConfig.class);
    }
    
    @Override
    protected MechaConfig getConfig() {
        return mechaConfig;
    }
    
    @Override
    protected void setConfig(MechaConfig config) {
        world = Bukkit.getWorld(config.world());
        List<MechaConfig.WeightedNamespacedKey> weightedNamespacedKeys = config.weightedMechaLootTables();
        weightedMechaLootTables = new HashMap<>(weightedNamespacedKeys.size());
        for (MechaConfig.WeightedNamespacedKey weightedNamespacedKey : weightedNamespacedKeys) {
            String namespace = weightedNamespacedKey.namespace();
            String key = weightedNamespacedKey.key();
            int weight = weightedNamespacedKey.weight();
            LootTable lootTable = Bukkit.getLootTable(new NamespacedKey(namespace, key));
            weightedMechaLootTables.put(lootTable, weight);
        }
        this.mechaConfig = config;
        
    }
    
    @Override
    protected boolean configIsValid(@Nullable MechaConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Saved config is null");
        }
        World configWorld = Bukkit.getWorld(config.world());
        if (configWorld == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
        if (lootTableDoesNotExist(config.spawnLootTable())) {
            throw new IllegalArgumentException(String.format("Could not find spawn loot table \"%s\"", config.spawnLootTable()));
        }
        if (config.weightedMechaLootTables() == null) {
            throw new IllegalArgumentException("weightedMechaLootTables can't be null");
        }
        // weightedNamespacedKey list can be empty and still be valid
        for (MechaConfig.WeightedNamespacedKey weightedNamespacedKey : config.weightedMechaLootTables()) {
            if (weightedNamespacedKey.namespace() == null) {
                throw new IllegalArgumentException("weightedNamespacedKey.namespace can't be null");
            }
            if (weightedNamespacedKey.key() == null) {
                throw new IllegalArgumentException("weightedNamespacedKey.key can't be null");
            }
            NamespacedKey namespacedKey = new NamespacedKey(weightedNamespacedKey.namespace(), weightedNamespacedKey.key());
            if (lootTableDoesNotExist(namespacedKey)) {
                throw new IllegalArgumentException(String.format("Could not find loot table \"%s\"", namespacedKey));
            }
            if (weightedNamespacedKey.weight() < 1) {
                throw new IllegalArgumentException(String.format("weightedNamespacedKey (%s) can't have a weight (%s) less than 1", namespacedKey, weightedNamespacedKey.weight()));
            }
        }
        if (config.initialBorderSize() < 1.0) {
            throw new IllegalArgumentException(String.format("initialBorderSize can't be less than 1.0: %s", config.initialBorderSize()));
        }
        if (config.borderStages() == null) {
            throw new IllegalArgumentException("borderStages can't be null");
        }
        if (config.borderStages().size() < 1) {
            throw new IllegalArgumentException("borderStages must have at least one stage");
        }
        for (MechaConfig.BorderStage borderStage : config.borderStages()) {
            if (borderStage.size() < 1.0) {
                throw new IllegalArgumentException(String.format("border stage size (%s) can't be less than 1.0", borderStage.size()));
            }
            if (borderStage.delay() < 0) {
                throw new IllegalArgumentException(String.format("border stage delay (%s) can't be less than 0", borderStage.delay()));
            }
            if (borderStage.duration() < 0) {
                throw new IllegalArgumentException(String.format("border stage duration (%s) can't be less than 0", borderStage.duration()));
            }
        }
        if (config.spawnChestCoords() == null) {
            throw new IllegalArgumentException("spawnChestCoords can't be null");
        }
        if (config.spawnChestCoords().contains(null)) {
            throw new IllegalArgumentException("spawnChestCoords can't contain a null position");
        }
        if (config.mapChestCoords() == null) {
            throw new IllegalArgumentException("mapChestCoords can't be null");
        }
        if (config.mapChestCoords().contains(null)) {
            throw new IllegalArgumentException("mapChestCoords can't contain a null position");
        }
        return true;
    }
    
    private boolean lootTableDoesNotExist(@Nullable NamespacedKey lootTable) {
        return lootTable != null && Bukkit.getLootTable(lootTable) == null;
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
}
