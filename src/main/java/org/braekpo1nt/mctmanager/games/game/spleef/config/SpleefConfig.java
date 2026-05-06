package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.Config;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.braekpo1nt.mctmanager.geometry.CompositeGeometry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SpleefConfig implements Config {
    private World world;
    private List<Location> startingLocations;
    private List<Structure> structures;
    private @Nullable SpectatorBoundary spectatorBoundary;
    private List<Location> structureOrigins;
    private List<BoundingBox> decayLayers;
    private List<DecayStage> decayStages;
    private @Nullable Material stencilBlock;
    private @NotNull Material layerBlock;
    private @NotNull Material decayBlock;
    private ItemStack tool;
    private Powerup playerSwapper;
    private Powerup blockBreaker;
    private Powerup shield;
    private Map<Powerup.Type, @NotNull Integer> initialLoadout;
    private long minTimeBetween;
    private int maxPowerups;
    private Map<Powerup.Source, @NotNull Double> chances;
    /**
     * Maps a source to the weights of the types that can come from that source
     */
    private Map<Powerup.Source, Map<Powerup.Type, @NotNull Integer>> sourceToPowerupWeights;
    private int roundStartingDuration;
    private int roundOverDuration;
    private int gameOverDuration;
    private int surviveScore;
    private int rounds;
    private List<Material> preventInteractions;
    /**
     * Players will be forced to stay in this area until the game starts
     */
    private @Nullable CompositeGeometry safetyArea;
    private List<DecayStage> stages;
    private int descriptionDuration;
    private Component description;
    
    public double getChance(@NotNull Powerup.Source source) {
        return chances.get(source);
    }
    
    /**
     * @param source the source that the powerups should come from
     * @return the weights for the powerups which come from the given source (all powerup weights if source is null).
     * The key is the powerup which can come from the source, the value is the weight.
     */
    public @NotNull Map<Powerup.Type, @NotNull Integer> getPowerupWeights(@Nullable Powerup.Source source) {
        return sourceToPowerupWeights.get(source);
    }
    
    /**
     * @param type the type of the powerup to get
     * @return the powerup of the given type
     */
    public @NotNull Powerup getPowerup(@NotNull Powerup.Type type) {
        return switch (type) {
            case PLAYER_SWAPPER -> this.playerSwapper;
            case BLOCK_BREAKER -> this.blockBreaker;
            case SHIELD -> this.shield;
        };
    }
    
}
