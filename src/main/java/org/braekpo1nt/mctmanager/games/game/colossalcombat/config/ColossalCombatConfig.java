package org.braekpo1nt.mctmanager.games.game.colossalcombat.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.Config;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.Gate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class ColossalCombatConfig implements Config {
    private World world;
    private Location spectatorSpawn;
    private @Nullable SpectatorBoundary spectatorBoundary;
    /**
     * each index corresponds to an index in the itemDropsList
     */
    private @Nullable List<Location> itemDropLocations;
    /**
     * each index corresponds to an index in the itemDropLocations list
     */
    private @Nullable List<ItemStack> itemDrops;
    private List<Boolean> glowingItemDrops;
    private int requiredWins;
    private @NotNull ItemStack[] loadout;
    private Gate northGate;
    private Gate southGate;
    private BoundingBox removeArea;
    private BoundingBox northSupport;
    private BoundingBox southSupport;
    private Material flagMaterial;
    private BlockFace initialFlagDirection;
    private Location flagLocation;
    private Component flagSpawnMessage;
    private boolean shouldStartCaptureTheFlag;
    /**
     * if the number of living players falls below this amount on each team, then the capture the flag countdown will
     * start.
     */
    private int captureTheFlagMaximumPlayers;
    private int captureTheFlagDuration;
    /**
     * If null, no blocks will be replaced.
     */
    private @Nullable Material replaceBlock;
    /**
     * The area to replace with the concrete of the team's color. At the start of the game, the
     * {@link ColossalCombatConfig#replaceBlock} material will be replaced with the concrete of the team's color, and at
     * the end of the game it will be returned to what it was before.
     */
    private BoundingBox northFlagReplaceArea;
    /**
     * The same as {@link ColossalCombatConfig#northFlagReplaceArea}, but for second place
     */
    private BoundingBox southFlagReplaceArea;
    private long antiSuffocationDuration;
    private int roundStartingDuration;
    private int roundOverDuration;
    private int gameOverDuration;
    private List<Material> preventInteractions;
    private int descriptionDuration;
    private Component description;
    
    public boolean shouldStartCaptureTheFlag() {
        return shouldStartCaptureTheFlag;
    }
    
    
    public boolean shouldReplaceWithConcrete() {
        return replaceBlock != null;
    }
}
