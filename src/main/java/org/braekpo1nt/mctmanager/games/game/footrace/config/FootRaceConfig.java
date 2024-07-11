package org.braekpo1nt.mctmanager.games.game.footrace.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class FootRaceConfig {
    private World world;
    private Location startingLocation;
    private BoundingBox finishLine;
    private BoundingBox glassBarrier;
    private int completeLapScore;
    private int[] placementPoints;
    private int detriment;
    private int startRaceDuration;
    private int raceEndCountdownDuration;
    private int descriptionDuration;
    private int endDuration;
    private List<Material> preventInteractions;
    /**
     * the checkpoints in the race. The last one is the finish line. Players must
     * pass through all of these in order to be considered a lap. If this is null, then use
     * the {@link #finishLine}
     */
    private @Nullable List<BoundingBox> checkpoints;
    private @Nullable BoundingBox spectatorArea;
    private Component description;
    
}
