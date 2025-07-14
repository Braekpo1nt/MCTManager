package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.loot.LootTable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SurvivalGamesConfig {
    private World world;
    private @Nullable SpectatorBoundary spectatorBoundary;
    private List<Vector> spawnChestCoords;
    private List<Vector> mapChestCoords;
    private LootTable spawnLootTable;
    private Map<LootTable, Integer> weightedLootTables;
    private BoundingBox removeArea;
    private List<BoundingBox> platformBarriers;
    private List<Location> platformSpawns;
    private Location adminSpawn;
    private int rounds;
    private int roundStartingDuration;
    private int gracePeriodDuration;
    private int gameOverDuration;
    private int roundOverDuration;
    private int killScore;
    private int surviveTeamScore;
    private int firstPlaceScore;
    private int secondPlaceScore;
    private int thirdPlaceScore;
    private boolean lockOtherInventories;
    private boolean shouldClearContainers;
    private boolean showDeathCount;
    private double initialBorderSize;
    private double worldBorderCenterX;
    private double worldBorderCenterZ;
    private double worldBorderDamageAmount;
    private double worldBorderDamageBuffer;
    private int worldBorderWarningDistance;
    private int worldBorderWarningTime;
    private int[] sizes;
    private int[] delays;
    private int[] durations;
    private List<Material> preventInteractions;
    private int descriptionDuration;
    private Component description;
    
    /**
     * @return true if the other inventories should be locked, false otherwise
     */
    public boolean lockOtherInventories() {
        return lockOtherInventories;
    }
    
    public boolean shouldClearContainers() {
        return shouldClearContainers;
    }
    
    public boolean showDeathCount() {
        return showDeathCount;
    }
}
