package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.Config;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.TeamSpawn;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class ParkourPathwayConfig implements Config {
    private World world;
    private Location startingLocation;
    private @Nullable SpectatorBoundary spectatorBoundary;
    private List<Puzzle> puzzles;
    private @Nullable List<TeamSpawn> teamSpawns;
    private @Nullable BoundingBox glassBarrier;
    private @Nullable Component glassBarrierOpenMessage;
    private @Nullable Component teamSpawnsOpenMessage;
    private int startingDuration;
    private int timeLimitDuration;
    private int teamSpawnsDuration;
    private int mercyRuleDuration;
    private int mercyRuleAlertDuration;
    private int gameOverDuration;
    private int[] checkpointScore;
    private int[] winScore;
    /**
     * the number of skips each player gets. 0 or negative means no skips.
     */
    private int numOfSkips;
    /**
     * the item that players interact with to use their skips
     */
    private ItemStack skipItem;
    /**
     * the number of points to award for unused skips
     */
    private int unusedSkipScore;
    /**
     * The sound that plays
     */
    private Sound reachedCheckpointSound;
    /**
     * The puzzle after which no skips are allowed, and players
     * will be given points for their remaining unused skips.
     * Values less than zero will allow skips to be used the entire
     * game. Values greater than the number of puzzles will essentially
     * do the same.
     */
    private int maxSkipPuzzle;
    private List<Material> preventInteractions;
    private int descriptionDuration;
    /**
     * The duration in seconds of a skip cooldown
     */
    private int skipCooldownDuration;
    private Component description;
    
    // Chat Mode Toggle Fields
    private @NotNull Material chatToggleMaterial;
    private @NotNull Component chatToggleName;
    private @NotNull List<Component> chatToggleLoreALL;
    private @NotNull List<Component> chatToggleLoreTEAM;
    private @NotNull List<Component> chatToggleLoreLOCAL;
    private @NotNull List<Component> chatToggleLoreOFF;
    
    public int getPuzzlesSize() {
        return puzzles.size();
    }
    
    public Puzzle getPuzzle(int index) {
        return puzzles.get(index);
    }
    
}
