package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.Puzzle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayStorageUtil extends GameConfigStorageUtil<ParkourPathwayConfig> {
    
    protected ParkourPathwayConfig parkourPathwayConfig = getExampleConfig();
    private List<Puzzle> puzzles;
    private World world;
    private Location startingLocation;
    private Component description;
    
    public ParkourPathwayStorageUtil(File configDirectory) {
        super(configDirectory, "parkourPathwayConfig.json", ParkourPathwayConfig.class);
    }
    
    @Override
    protected ParkourPathwayConfig getConfig() {
        return parkourPathwayConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable ParkourPathwayConfig config) {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocation() != null, "startingLocation can't be null");
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.spectatorArea().toBoundingBox().getVolume() >= 1.0, "getSpectatorArea's volume (%s) can't be less than 1. %s", config.spectatorArea().toBoundingBox().getVolume(), config.spectatorArea().toBoundingBox());
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.scores().checkpoint() != null, "scores.checkpoint can't be null");
        Preconditions.checkArgument(config.scores().checkpoint().length >= 2, "scores.checkpoint must have at least two elements");
        Preconditions.checkArgument(config.scores().win() != null, "scores.win can't be null");
        Preconditions.checkArgument(config.scores().win().length >= 2, "scores.win must have at least two elements");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().starting() >= 0, "durations.starting (%s) can't be negative", config.durations().starting());
        Preconditions.checkArgument(config.durations().timeLimit() >= 2, "durations.timeLimit (%s) can't be less than 2", config.durations().timeLimit());
        Preconditions.checkArgument(config.durations().checkpointCounter() >= 1, "durations.checkpointCounter (%s) can't be less than 1", config.durations().checkpointCounter());
        Preconditions.checkArgument(config.durations().checkpointCounterAlert() >= 1 && config.durations().checkpointCounter() >= config.durations().checkpointCounterAlert(), "durations.checkpointCounterAlert (%s) can't be less than 0 or greater than durations.checkpointCounter", config.durations().checkpointCounterAlert());
        Preconditions.checkArgument(config.puzzles() != null, "puzzles can't be null");
        Preconditions.checkArgument(config.puzzles().size() >= 3, "puzzles must have at least 3 puzzles");
        puzzlesAreValid(config.puzzles());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    private void puzzlesAreValid(List<ParkourPathwayConfig.PuzzleDTO> puzzles) {
        for (int i = 0; i < puzzles.size(); i++) {
            ParkourPathwayConfig.PuzzleDTO puzzle = puzzles.get(i);
            Preconditions.checkArgument(puzzle != null, "puzzles[%s] can't be null", i);
            puzzleIsValid(puzzle, i);
            if (i - 1 >= 0) {
                ParkourPathwayConfig.PuzzleDTO previousPuzzle = puzzles.get(i - 1);
                BoundingBox previousInBounds = previousPuzzle.inBounds().toBoundingBox();
                for (ParkourPathwayConfig.PuzzleDTO.CheckPointDTO checkPoint : puzzle.checkPoints()) {
                    Preconditions.checkArgument(previousInBounds.contains(checkPoint.detectionArea().toBoundingBox()), "puzzles[%s].inBounds must contain all puzzles[%s].checkPoints detectionAreas", i - 1, i);
                    Preconditions.checkArgument(previousInBounds.contains(checkPoint.respawn().toVector()), "puzzles[%s].inBounds must contain all puzzles[%s].checkPoints respawns", i - 1, i);
                }
            }
        }
    }
    
    private void puzzleIsValid(@NotNull ParkourPathwayConfig.PuzzleDTO puzzle, int puzzleIndex) {
        Preconditions.checkArgument(puzzle.inBounds() != null, "puzzle[%s].inBounds can't be null", puzzleIndex);
        BoundingBox inBounds = puzzle.inBounds().toBoundingBox();
        Preconditions.checkArgument(inBounds.getVolume() >= 1, "puzzle[%s].inBounds' volume (%s) can't be less than 1 (%s)", puzzleIndex, inBounds.getVolume(), inBounds);
        Preconditions.checkArgument(puzzle.checkPoints() != null, "puzzle[%s].checkPoints can't be null", puzzleIndex);
        Preconditions.checkArgument(!puzzle.checkPoints().isEmpty(), "puzzle.[%s]checkPoints must have at least 1 element", puzzleIndex);
        for (int i = 0; i < puzzle.checkPoints().size(); i++) {
            ParkourPathwayConfig.PuzzleDTO.CheckPointDTO checkPoint = puzzle.checkPoints().get(i);
            Preconditions.checkArgument(checkPoint != null, "puzzle[%s].checkPoints can't have null elements", puzzleIndex);
            puzzleCheckPointIsValid(checkPoint, puzzleIndex, i);
            BoundingBox detectionArea = checkPoint.detectionArea().toBoundingBox();
            Preconditions.checkArgument(inBounds.contains(detectionArea), "puzzle[%s].inBounds must contain all puzzle[%s].checkPoints[%s].detectionAreas", puzzleIndex, puzzleIndex, i);
            Vector respawn = checkPoint.respawn().toVector();
            Preconditions.checkArgument(inBounds.contains(respawn), "puzzle[%s].inBounds must contain all puzzle[%s].checkPoints.respawns", puzzleIndex, puzzleIndex);
            for (int j = 0; j < i; j++) {
                ParkourPathwayConfig.PuzzleDTO.CheckPointDTO earlierCheckpoint = puzzle.checkPoints().get(j);
                BoundingBox earlierDetectionArea = earlierCheckpoint.detectionArea().toBoundingBox();
                Preconditions.checkArgument(!earlierDetectionArea.overlaps(detectionArea), "puzzle[%s].checkPoints[%s].detectionArea and puzzle[%s].checkPoints[%s].detectionArea can't overlap", puzzleIndex, i-1, puzzleIndex, i);
                Preconditions.checkArgument(earlierDetectionArea.contains(respawn), "puzzle[%s].checkPoints[%s].detectionArea can't contain puzzle[%s].checkPoints[%s].respawn", puzzleIndex, i-1, puzzleIndex, i);
            }
        }
        
    }
    
    private void puzzleCheckPointIsValid(@NotNull ParkourPathwayConfig.PuzzleDTO.CheckPointDTO checkPoint, int puzzleIndex, int checkPointIndex) {
        BoundingBox detectionArea = checkPoint.detectionArea().toBoundingBox();
        Preconditions.checkArgument(detectionArea.getVolume() >= 1, "puzzle[%s].checkPoints[%s].detectionArea's volume (%s) can't be less than 1 (%s)", puzzleIndex, checkPointIndex, detectionArea.getVolume(), detectionArea);
        Preconditions.checkArgument(detectionArea.contains(checkPoint.respawn().toVector()), "puzzle[%s].checkPoints[%s].detectionArea must contain respawn", puzzleIndex, checkPointIndex);
        
    }
    
    
    @Override
    protected void setConfig(ParkourPathwayConfig config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newStartingLocation = config.startingLocation().toLocation(newWorld);
        List<Puzzle> newPuzzles = config.puzzles().stream().map(puzzleDTO -> puzzleDTO.toPuzzle(newWorld)).toList();
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocation = newStartingLocation;
        this.puzzles = newPuzzles;
        this.description = newDescription;
        this.parkourPathwayConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return ParkourPathwayStorageUtil.class.getResourceAsStream("exampleParkourPathwayConfig.json");
    }
    
    public List<Puzzle> getPuzzles() {
        return puzzles;
    }
    
    public int getStartingDuration() {
        return parkourPathwayConfig.durations().starting();
    }
    
    /**
     * @return the time limit for the entire game
     */
    public int getTimeLimitDuration() {
        return parkourPathwayConfig.durations().timeLimit();
    }
    
    /**
     * @return how long (in seconds) the game should wait before declaring that no one has made it to a new checkpoint and ending the game
     */
    public int getCheckpointCounterDuration() {
        return parkourPathwayConfig.durations().checkpointCounter();
    }
    
    /**
     * @return How much time (seconds) should be left in the checkpointCounter before you start displaying the countdown to the users
     */
    public int getCheckpointCounterAlertDuration() {
        return parkourPathwayConfig.durations().checkpointCounterAlert();
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getStartingLocation() {
        return startingLocation;
    }
    
    public int[] getCheckpointScore() {
        return parkourPathwayConfig.scores().checkpoint();
    }
    
    public int[] getWinScore() {
        return parkourPathwayConfig.scores().win();
    }
    
    public Component getDescription() {
        return description;
    }
}
