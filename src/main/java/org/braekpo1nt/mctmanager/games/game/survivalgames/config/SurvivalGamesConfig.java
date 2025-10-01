package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.game.survivalgames.Border;
import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
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
    private @Nullable ItemStack[] starterLoadout;
    private int rounds;
    private int roundStartingDuration;
    private int gracePeriodDuration;
    private int gameOverDuration;
    private int roundOverDuration;
    private int killScore;
    /**
     * If true, successive deaths grant the killer points. 
     * If false, only a participants first death grants its killer points.
     */
    private boolean successiveDeathsGrantPoints;
    private int surviveTeamScore;
    private int firstPlaceScore;
    private int secondPlaceScore;
    private int thirdPlaceScore;
    private boolean lockOtherInventories;
    private boolean shouldClearContainers;
    private boolean showDeathCount;
    private double initialBorderSize;
    private Border border;
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
    
    public List<Location> getRespawnLocations() {
        return border.getRespawnLocations();
    }
    
    public List<BorderStage> getBorderStages() {
        return border.getStages();
    }
}
