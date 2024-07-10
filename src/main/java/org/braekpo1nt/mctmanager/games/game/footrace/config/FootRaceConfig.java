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
    private @Nullable BoundingBox spectatorArea;
    private Component description;
    
}
