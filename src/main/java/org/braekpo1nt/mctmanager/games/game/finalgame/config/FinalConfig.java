package org.braekpo1nt.mctmanager.games.game.finalgame.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGameKit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class FinalConfig {
    private World world;
    /**
     * Items and arrows on the ground in this area will be removed between rounds
     */
    private BoundingBox itemRemoval;
    private @Nullable SpectatorBoundary spectatorBoundary;
    private Lava lava;
    /**
     * Which block to replace with the team colors
     * defaults to WHITE_CONCRETE
     */
    private @NotNull Material replacementType;
    private MapHalf northMap;
    private MapHalf southMap;
    /**
     * Unique Names to kits
     */
    private Map<String, FinalGameKit> kits;
    private Location spectatorSpawn;
    /**
     * How many round wins before a game win
     */
    private int requiredWins;
    private List<Material> preventInteractions;
    private int descriptionDuration;
    private int roundStartingDuration;
    private int classSelectionDuration;
    private int roundOverDuration;
    private int gameOverDuration;
    private Component description;
    /**
     * Multiplied by the damage an arrow deals to result in a new damage value.
     * E.g. to make arrows deal 50% damage, set to 0.5.
     * Can't be negative. Defaults to 1.0.
     */
    private double arrowDamageModifier;
    
    /**
     * Represents the details about each half of the map (north or south)
     */
    @Data
    @AllArgsConstructor
    public static class MapHalf {
        private @NotNull Affiliation affiliation;
        private Location spawn;
        private BoundingBox replacementArea;
    }
    
    @Data
    public static class Lava {
        /**
         * This is the maximum volume the lava should take up. Air blocks
         * are replaced by lava within this area according. The first lava layer
         * is the bottom-most block layer of this lava area. No lava
         * will be spawned above the max Z of the given area.
         */
        private BoundingBox lavaArea;
        /**
         * How many blocks the lava should rise on each rise action
         */
        private int blocksPerRise;
        /**
         * How many seconds between rises of the lava
         */
        private int riseSeconds;
        /**
         * How many players have to die (on either team) for the lava to rise
         * an extra level. This will be repeated (e.g. if it's set to 2, then
         * when two players die the lava will rise; and when two more players
         * die after that, the lava will rise again).
         */
        private int riseDeaths;
    }
}

