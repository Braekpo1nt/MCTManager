package org.braekpo1nt.mctmanager.games.game.mecha.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
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
public class MechaConfig {
    private World world;
    private @Nullable BoundingBox spectatorArea;
    private List<Vector> spawnChestCoords;
    private List<Vector> mapChestCoords;
    private LootTable spawnLootTable;
    private Map<LootTable, Integer> weightedMechaLootTables;
    private BoundingBox removeArea;
    private List<BoundingBox> platformBarriers;
    private List<Location> platformSpawns;
    private Location adminSpawn;
    private int startDuration;
    private int gracePeriodDuration;
    private int endDuration;
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
     * the checkpoints in the race. The last one is the finish line. Players must
     * pass through all of these in order to be considered a lap
     */
    private List<BoundingBox> checkpoints;
    
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
