package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.TeamSpawn;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class ParkourPathwayConfig {
    private World world;
    private Location startingLocation;
    private @Nullable BoundingBox spectatorArea;
    private List<Puzzle> puzzles;
    private @Nullable List<TeamSpawn> teamSpawns;
    private @Nullable BoundingBox glassBarrier;
    private @Nullable Component glassBarrierOpenMessage;
    private @Nullable Component teamSpawnsOpenMessage;
    private int startingDuration;
    private int timeLimitDuration;
    private int teamSpawnsDuration;
    private int checkpointCounterDuration;
    private int checkpointCounterAlertDuration;
    public int[] checkpointScore;
    public int[] winScore;
    private List<Material> preventInteractions;
    private int descriptionDuration;
    private Component description;
    
    public int getPuzzlesSize() {
        return puzzles.size();
    }
    
    public Puzzle getPuzzle(int index) {
        return puzzles.get(index);
    }
}
