package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;

import java.util.List;

public record MechaConfig (String world, NamespacedKey spawnLootTable, List<WeightedNamespacedKey> weightedMechaLootTables, double initialBorderSize, List<BorderStage> borderStages, List<Vector> spawnChestCoords, List<Vector> mapChestCoords) {
}
