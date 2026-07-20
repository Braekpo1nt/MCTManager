package org.braekpo1nt.mctmanager.games.game.footrace.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.Config;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class FootRaceConfig implements Config {
    private World world;
    private Location startingLocation;
    /**
     * the number of laps in the race
     */
    private int laps;
    private BoundingBox glassBarrier;
    private int completeLapScore;
    private int[] placementPoints;
    private int detriment;
    /**
     * points earned for full team race completion
     */
    private int fullTeamCompletion;
    /**
     * how many points less should the next team get for full team race completion
     */
    private int fullTeamCompletionDetriment;
    /**
     * points earned for full team lap completion
     */
    private int fullTeamLapCompletion;
    /**
     * how many points less should the next team get for full team lap completion
     */
    private int fullTeamLapCompletionDetriment;
    private int startRaceDuration;
    private int raceEndCountdownDuration;
    private int descriptionDuration;
    private int gameOverDuration;
    private List<Material> preventInteractions;
    private boolean debugView;
    /**
     * The duration in milliseconds that a player has to be going the
     * right way for the wrong way indicator to disappear.
     * Works best when set to lower than {@link #wrongWayMilliseconds}.
     * Defaults to 1000
     */
    private int rightWayMilliseconds;
    /**
     * The duration in milliseconds that a player has to be going the
     * wrong way for the wrong way indicator to appear.
     * Works best when set to higher than {@link #rightWayMilliseconds}.
     * Defaults to 2000
     */
    private int wrongWayMilliseconds;
    /**
     * the checkpoints in the race. The last one is the finish line. Players must
     * pass through all of these in order to be considered a lap.
     */
    private List<BoundingBox> checkpoints;
    private @Nullable SpectatorBoundary spectatorBoundary;
    private Component description;
    
}
