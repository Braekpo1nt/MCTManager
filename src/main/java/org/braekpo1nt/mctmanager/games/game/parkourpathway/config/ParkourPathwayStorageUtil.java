package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
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
import java.util.stream.Collectors;

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
        Preconditions.checkArgument(config.getVersion() != null, "version can't be null");
        Preconditions.checkArgument(config.getVersion().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.getVersion(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.getWorld()) != null, "Could not find world \"%s\"", config.getWorld());
        Preconditions.checkArgument(config.getSpectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.getSpectatorArea().toBoundingBox().getVolume() >= 1.0, "getSpectatorArea's volume (%s) can't be less than 1. %s", config.getSpectatorArea().toBoundingBox().getVolume(), config.getSpectatorArea().toBoundingBox());
        Preconditions.checkArgument(config.getScores() != null, "scores can't be null");
        Preconditions.checkArgument(config.getScores().getCheckpoint() != null, "scores.checkpoint can't be null");
        Preconditions.checkArgument(config.getScores().getCheckpoint().length >= 2, "scores.checkpoint must have at least two elements");
        Preconditions.checkArgument(config.getScores().getWin() != null, "scores.win can't be null");
        Preconditions.checkArgument(config.getScores().getWin().length >= 2, "scores.win must have at least two elements");
        Preconditions.checkArgument(config.getDurations() != null, "durations can't be null");
        Preconditions.checkArgument(config.getDurations().getStarting() >= 0, "durations.starting (%s) can't be negative", config.getDurations().getStarting());
        Preconditions.checkArgument(config.getDurations().getTimeLimit() >= 2, "durations.timeLimit (%s) can't be less than 2", config.getDurations().getTimeLimit());
        Preconditions.checkArgument(config.getDurations().getCheckpointCounter() >= 1, "durations.checkpointCounter (%s) can't be less than 1", config.getDurations().getCheckpointCounter());
        Preconditions.checkArgument(config.getDurations().getCheckpointCounterAlert() >= 1 && config.getDurations().getCheckpointCounter() >= config.getDurations().getCheckpointCounterAlert(), "durations.checkpointCounterAlert (%s) can't be less than 0 or greater than durations.checkpointCounter", config.getDurations().getCheckpointCounterAlert());
        Preconditions.checkArgument(config.getPuzzles() != null, "puzzles can't be null");
        Preconditions.checkArgument(config.getPuzzles().size() >= 3, "puzzles must have at least 3 puzzles");
        puzzlesAreValid(config.getPuzzles());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.getDescription());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    private void puzzlesAreValid(List<PuzzleDTO> puzzles) {
        for (int i = 0; i < puzzles.size(); i++) {
            PuzzleDTO puzzle = puzzles.get(i);
            Preconditions.checkArgument(puzzle != null, "puzzles[%s] can't be null", i);
            puzzleIsValid(puzzle, i);
            if (i - 1 >= 0) {
                PuzzleDTO previousPuzzle = puzzles.get(i - 1);
                BoundingBox previousInBounds = previousPuzzle.getInBounds().toBoundingBox();
                for (PuzzleDTO.CheckPointDTO checkPoint : puzzle.getCheckPoints()) {
                    Preconditions.checkArgument(previousInBounds.contains(checkPoint.getDetectionArea().toBoundingBox()), "puzzles[%s].inBounds must contain all puzzles[%s].checkPoints detectionAreas", i - 1, i);
                    Preconditions.checkArgument(previousInBounds.contains(checkPoint.getRespawn().toVector()), "puzzles[%s].inBounds must contain all puzzles[%s].checkPoints respawns", i - 1, i);
                }
            }
        }
    }
    
    private void puzzleIsValid(@NotNull PuzzleDTO puzzle, int puzzleIndex) {
        Preconditions.checkArgument(puzzle.getInBounds() != null, "puzzle[%s].inBounds can't be null", puzzleIndex);
        BoundingBox inBounds = puzzle.getInBounds().toBoundingBox();
        Preconditions.checkArgument(inBounds.getVolume() >= 1, "puzzle[%s].inBounds' volume (%s) can't be less than 1 (%s)", puzzleIndex, inBounds.getVolume(), inBounds);
        Preconditions.checkArgument(puzzle.getCheckPoints() != null, "puzzle[%s].checkPoints can't be null", puzzleIndex);
        Preconditions.checkArgument(!puzzle.getCheckPoints().isEmpty(), "puzzle.[%s]checkPoints must have at least 1 element", puzzleIndex);
        for (int i = 0; i < puzzle.getCheckPoints().size(); i++) {
            PuzzleDTO.CheckPointDTO checkPoint = puzzle.getCheckPoints().get(i);
            Preconditions.checkArgument(checkPoint != null, "puzzle[%s].checkPoints can't have null elements", puzzleIndex);
            puzzleCheckPointIsValid(checkPoint, puzzleIndex, i);
            BoundingBox detectionArea = checkPoint.getDetectionArea().toBoundingBox();
            Preconditions.checkArgument(inBounds.contains(detectionArea), "puzzle[%s].inBounds must contain all puzzle[%s].checkPoints[%s].detectionAreas", puzzleIndex, puzzleIndex, i);
            Vector respawn = checkPoint.getRespawn().toVector();
            Preconditions.checkArgument(inBounds.contains(respawn), "puzzle[%s].inBounds must contain all puzzle[%s].checkPoints.respawns", puzzleIndex, puzzleIndex);
            for (int j = 0; j < i; j++) {
                PuzzleDTO.CheckPointDTO earlierCheckpoint = puzzle.getCheckPoints().get(j);
                BoundingBox earlierDetectionArea = earlierCheckpoint.getDetectionArea().toBoundingBox();
                Preconditions.checkArgument(!earlierDetectionArea.overlaps(detectionArea), "puzzle[%s].checkPoints[%s].detectionArea (%s) and puzzle[%s].checkPoints[%s].detectionArea (%s) can't overlap", puzzleIndex, i-1, earlierDetectionArea, puzzleIndex, i, detectionArea);
                Preconditions.checkArgument(!earlierDetectionArea.contains(respawn), "puzzle[%s].checkPoints[%s].detectionArea (%s) can't contain puzzle[%s].checkPoints[%s].respawn (%s)", puzzleIndex, i-1, earlierDetectionArea, puzzleIndex, i, respawn);
            }
        }
        
    }
    
    private void puzzleCheckPointIsValid(@NotNull PuzzleDTO.CheckPointDTO checkPoint, int puzzleIndex, int checkPointIndex) {
        BoundingBox detectionArea = checkPoint.getDetectionArea().toBoundingBox();
        Preconditions.checkArgument(detectionArea.getVolume() >= 1, "puzzle[%s].checkPoints[%s].detectionArea's volume (%s) can't be less than 1 (%s)", puzzleIndex, checkPointIndex, detectionArea.getVolume(), detectionArea);
        Vector respawn = checkPoint.getRespawn().toVector();
        Preconditions.checkArgument(detectionArea.contains(respawn), "puzzle[%s].checkPoints[%s].detectionArea (%s) must contain puzzle[%s].checkPoints[%s].respawn (%s)", puzzleIndex, checkPointIndex, detectionArea, puzzleIndex, checkPointIndex, respawn);
        
    }
    
    
    @Override
    protected void setConfig(ParkourPathwayConfig config) {
        World newWorld = Bukkit.getWorld(config.getWorld());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.getWorld());
        List<Puzzle> newPuzzles = config.getPuzzles().stream().map(puzzleDTO -> puzzleDTO.toPuzzle(newWorld)).toList();
        Location newStartingLocation = newPuzzles.get(0).checkPoints().get(0).respawn();
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.getDescription());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocation = newStartingLocation;
        this.puzzles = newPuzzles;
        this.description = newDescription;
        this.parkourPathwayConfig = config;
    }
    
    /**
     * @return true if this StorageUtil's config is valid, false otherwise
     * @throws IllegalArgumentException if this config is not valid. The exception includes a detailed message of what was invalid
     */
    public boolean configIsValid() throws IllegalArgumentException {
        return configIsValid(parkourPathwayConfig);
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return ParkourPathwayStorageUtil.class.getResourceAsStream("exampleParkourPathwayConfig.json");
    }
    
    public Puzzle getPuzzle(int index) {
        return puzzles.get(index);
    }
    
    /**
     * @return a deep copy list of the puzzles
     */
    public List<Puzzle> deepCopyPuzzles() {
        return puzzles.stream().map(Puzzle::copy).collect(Collectors.toCollection(ArrayList::new));
    }
    
    public int getPuzzlesSize() {
        return puzzles.size();
    }
    
    public void setPuzzles(List<Puzzle> puzzles) {
        this.puzzles = puzzles;
        this.parkourPathwayConfig.setPuzzles(puzzles);
    }
    
    public int getStartingDuration() {
        return parkourPathwayConfig.getDurations().getStarting();
    }
    
    /**
     * @return the time limit for the entire game
     */
    public int getTimeLimitDuration() {
        return parkourPathwayConfig.getDurations().getTimeLimit();
    }
    
    /**
     * @return how long (in seconds) the game should wait before declaring that no one has made it to a new checkpoint and ending the game
     */
    public int getCheckpointCounterDuration() {
        return parkourPathwayConfig.getDurations().getCheckpointCounter();
    }
    
    /**
     * @return How much time (seconds) should be left in the checkpointCounter before you start displaying the countdown to the users
     */
    public int getCheckpointCounterAlertDuration() {
        return parkourPathwayConfig.getDurations().getCheckpointCounterAlert();
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getStartingLocation() {
        return startingLocation;
    }
    
    public int[] getCheckpointScore() {
        return parkourPathwayConfig.getScores().getCheckpoint();
    }
    
    public int[] getWinScore() {
        return parkourPathwayConfig.getScores().getWin();
    }
    
    public Component getDescription() {
        return description;
    }
}
