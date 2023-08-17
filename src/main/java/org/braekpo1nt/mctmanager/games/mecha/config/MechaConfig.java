package org.braekpo1nt.mctmanager.games.mecha.config;

import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;

import java.util.List;

public record MechaConfig (NamespacedKey spawnLootTable,List<WeightedNamespacedKey> weightedMechaLootTables, List<BorderStage> borderStages, List<Vector> spawnChestCoords,List<Vector> mapChestCoords) {}
