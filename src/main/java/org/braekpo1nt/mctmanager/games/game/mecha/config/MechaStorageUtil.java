package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.Vector;

import java.util.*;

public class MechaStorageUtil extends GameConfigStorageUtil<MechaConfig> {
    
    private final Main plugin;
    protected MechaConfig mechaConfig = null;
    
    public MechaStorageUtil(Main plugin) {
        super(plugin, "mechaConfig.json", MechaConfig.class);
        this.plugin = plugin;
    }
    
    @Override
    protected MechaConfig getConfig() {
        return mechaConfig;
    }
    
    @Override
    protected void setConfig(MechaConfig config) {
        this.mechaConfig = config;
    }
    
    /**
     * Returns a new instance of the default config. Note that the returned config instance may
     * be modified, so this should return a fresh instance to avoid errors with future default
     * uses.
     *
     * @return A new config instance with default values for use if no user-config is present
     */
    @Override
    protected MechaConfig getDefaultConfig() {
        return new MechaConfig(
                new NamespacedKey("mctdatapack", "mecha/spawn-chest"),
                List.of(new WeightedNamespacedKey("mctdatapack", "mecha/better-chest", 1)),
                List.of(new BorderStage(400, 120, 45),
                        new BorderStage(180, 90, 50)),
                List.of(new Vector(0.0, -45.0, 1.0),
                        new Vector(0.0, -46.0, 1.0)),
                List.of(new Vector(-1.0, -44.0, 1.0),
                        new Vector(-1.0, -43.0, 1.0))
        );
    }
    
    public List<Vector> getSpawnChestCoords() {
        return mechaConfig.spawnChestCoords();
    }
    
    
    public List<Vector> getMapChestCoords() {
        return mechaConfig.mapChestCoords();
    }
    
    
    public LootTable getSpawnLootTable() {
        NamespacedKey spawnLootTableNamespacedKey = mechaConfig.spawnLootTable();
        if (spawnLootTableNamespacedKey == null) {
            return LootTables.EMPTY.getLootTable();
        }
        return Bukkit.getLootTable(spawnLootTableNamespacedKey);
    }
    
    
    public Map<LootTable, Integer> getWeightedMechaLootTables() {
        List<WeightedNamespacedKey> weightedNamespacedKeys = mechaConfig.weightedMechaLootTables();
        Map<LootTable, Integer> weightedMechaLootTables = new HashMap<>(weightedNamespacedKeys.size());
        for (WeightedNamespacedKey weightedNamespacedKey : weightedNamespacedKeys) {
            String namespace = weightedNamespacedKey.namespace();
            String key = weightedNamespacedKey.key();
            int weight = weightedNamespacedKey.weight();
            LootTable lootTable = Bukkit.getLootTable(new NamespacedKey(namespace, key));
            weightedMechaLootTables.put(lootTable, weight);
        }
        return weightedMechaLootTables;
    }
    
    public int[] getSizes() {
        List<BorderStage> borderStages = mechaConfig.borderStages();
        int[] sizes = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            sizes[i] = borderStages.get(i).size();
        }
        return sizes;
    }

    public int[] getDelays() {
        List<BorderStage> borderStages = mechaConfig.borderStages();
        int[] delays = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            delays[i] = borderStages.get(i).delay();
        }
        return delays;
    }

    public int[] getDurations() {
        List<BorderStage> borderStages = mechaConfig.borderStages();
        int[] durations = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            durations[i] = borderStages.get(i).duration();
        }
        return durations;
    }
}
