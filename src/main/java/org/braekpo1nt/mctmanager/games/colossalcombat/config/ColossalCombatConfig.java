package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@Data
@Builder
public class ColossalCombatConfig {
    private World world;
    private Location firstPlaceSpawn;
    private Location secondPlaceSpawn;
    private Location spectatorSpawn;
    /**
     * each index corresponds to an index in the itemDropsList
     */
    private List<Location> itemDropLocations;
    /**
     * each index corresponds to an index in the itemDropLocations list
     */
    private List<ItemStack> itemDrops;
    private List<Boolean> glowingItemDrops;
    private int requiredWins;
    private @NotNull ItemStack[] loadout;
    private BoundingBox firstPlaceClearArea;
    private BoundingBox firstPlacePlaceArea;
    private BoundingBox firstPlaceStone;
    private BoundingBox firstPlaceAntiSuffocationArea;
    private BoundingBox firstPlaceFlagGoal;
    private BoundingBox secondPlaceClearArea;
    private BoundingBox secondPlacePlaceArea;
    private BoundingBox secondPlaceStone;
    private BoundingBox secondPlaceAntiSuffocationArea;
    private BoundingBox secondPlaceFlagGoal;
    private BoundingBox removeArea;
    private BoundingBox firstPlaceSupport;
    private BoundingBox secondPlaceSupport;
    private long antiSuffocationDuration;
    private int roundStartingDuration;
    private int descriptionDuration;
    private Component description;
}
