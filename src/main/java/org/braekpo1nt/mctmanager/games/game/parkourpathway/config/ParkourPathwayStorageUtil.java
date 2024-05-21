package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.ConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.TeamSpawn;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
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

public class ParkourPathwayStorageUtil extends ConfigStorageUtil<ParkourPathwayConfig> {
    
    protected ParkourPathwayConfig parkourPathwayConfig = null;
    private @Nullable List<TeamSpawn> teamSpawns;
    private List<Puzzle> puzzles;
    private World world;
    private Location startingLocation;
    private Component description;
    private @Nullable BoundingBox glassBarrier;
    private @Nullable Component glassBarrierOpenMessage;
    private @Nullable Component teamSpawnsOpenMessage;
    
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
        Preconditions.checkArgument(Main.VALID_CONFIG_VERSIONS.contains(config.getVersion()), "invalid config version (%s)", config.getVersion());
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
        
        if (config.getTeamSpawns() != null) {
            teamSpawnsIsValid(config.getTeamSpawns(), config.getPuzzles().get(0));
        }
        if (config.getGlassBarrierOpenMessage() != null) {
            ConfigUtils.toComponent(config.getGlassBarrierOpenMessage());
        }
        if (config.getTeamSpawnsOpenMessage() != null) {
            ConfigUtils.toComponent(config.getTeamSpawnsOpenMessage());
        }
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.getDescription());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    private static void teamSpawnsIsValid(@NotNull List<TeamSpawnDTO> teamSpawns, @NotNull PuzzleDTO firstPuzzle) {
        Preconditions.checkArgument(!teamSpawns.isEmpty(), "teamSpawns must have at least one entry");
        for (int i = 0; i < teamSpawns.size(); i++) {
            TeamSpawnDTO teamSpawnDTO = teamSpawns.get(i);
            Preconditions.checkArgument(teamSpawnDTO != null, "teamSpawns[%s] can't be null", i);
            teamSpawnDTO.isValid();
            Preconditions.checkArgument(firstPuzzle.isInBounds(teamSpawnDTO.getBarrierArea().toBoundingBox()), "teamSpawns[%s].barrierArea must be contained in at least one of the inBounds boxes of puzzles[0]", i);
            Preconditions.checkArgument(firstPuzzle.isInBounds(teamSpawnDTO.getSpawn().toVector()), "teamSpawns[%s].spawn must be contained in at least one of the inBounds boxes of puzzles[0]", i);
        }
    }
    
    private void puzzlesAreValid(List<PuzzleDTO> puzzles) {
        for (int i = 0; i < puzzles.size(); i++) {
            PuzzleDTO puzzle = puzzles.get(i);
            Preconditions.checkArgument(puzzle != null, "puzzles[%s] can't be null", i);
            puzzleIsValid(puzzle, i);
            if (i - 1 >= 0) {
                PuzzleDTO previousPuzzle = puzzles.get(i - 1);
                for (int j = 0 ; j < puzzle.getCheckPoints().size(); j++) {
                    PuzzleDTO.CheckPointDTO checkPoint = puzzle.getCheckPoints().get(j);
                    Preconditions.checkArgument(previousPuzzle.isInBounds(checkPoint.getDetectionArea().toBoundingBox()), "at least one entry in puzzles[%s].inBounds must contain puzzles[%s].checkPoints[%s].detectionArea", i - 1, i, j);
                }
            }
        }
    }
    
    private void puzzleIsValid(@NotNull PuzzleDTO puzzle, int puzzleIndex) {
        Preconditions.checkArgument(puzzle.getInBounds() != null, "puzzle[%s].inBounds can't be null", puzzleIndex);
        Preconditions.checkArgument(!puzzle.getInBounds().isEmpty(), "puzzle[%s].inBounds can't be empty", puzzleIndex);
        inBoundsIsValid(puzzle.getInBounds(), puzzleIndex);
        Preconditions.checkArgument(puzzle.getCheckPoints() != null, "puzzle[%s].checkPoints can't be null", puzzleIndex);
        checkPointsIsValid(puzzle, puzzleIndex);
    
    }
    
    private void inBoundsIsValid(@NotNull List<BoundingBoxDTO> inBoundsDTO, int puzzleIndex) {
        Preconditions.checkArgument(!inBoundsDTO.contains(null), "puzzle[%s].inBounds can't contain null");
        List<BoundingBox> inBounds = inBoundsDTO.stream().map(BoundingBoxDTO::toBoundingBox).toList();
        for (int i = 0; i < inBounds.size(); i++) {
            BoundingBox inBound = inBounds.get(i);
            Preconditions.checkArgument(inBound.getVolume() >= 1, "puzzle[%s].inBounds[%s]'s volume (%s) can't be less than 1 (%s)", puzzleIndex, i, inBound.getVolume(), inBound);
            if (inBounds.size() == 1) {
                return; // no need to check for overlapping
            }
            Preconditions.checkArgument(overlapsOneOtherBox(i, inBounds), "puzzle[%s].inBounds[%s] must overlap at least one other inBounds box in the list", puzzleIndex, i);
        }
    }
    
    /**
     * @param index the index of the bounding box to check if it overlaps at least one other box in the list
     * @param boxes a list of bounding boxes
     * @return true if the box with the given index overlaps at least one other box in the list, false otherwise
     */
    private boolean overlapsOneOtherBox(int index, @NotNull List<@NotNull BoundingBox> boxes) {
        BoundingBox box = boxes.get(index);
        for (int i = 0; i < boxes.size(); i++) {
            if (i != index) {
                BoundingBox otherBox = boxes.get(i);
                if (box.overlaps(otherBox)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void checkPointsIsValid(@NotNull PuzzleDTO puzzle, int puzzleIndex) {
        Preconditions.checkArgument(!puzzle.getCheckPoints().isEmpty(), "puzzle.[%s]checkPoints must have at least 1 element", puzzleIndex);
        for (int i = 0; i < puzzle.getCheckPoints().size(); i++) {
            PuzzleDTO.CheckPointDTO checkPoint = puzzle.getCheckPoints().get(i);
            Preconditions.checkArgument(checkPoint != null, "puzzle[%s].checkPoints can't have null elements", puzzleIndex);
            puzzleCheckPointIsValid(checkPoint, puzzleIndex, i);
            BoundingBox detectionArea = checkPoint.getDetectionArea().toBoundingBox();
            Preconditions.checkArgument(puzzle.isInBounds(detectionArea), "puzzle[%s].inBounds must contain all puzzle[%s].checkPoints[%s].detectionAreas", puzzleIndex, puzzleIndex, i);
            Vector respawn = checkPoint.getRespawn().toVector();
            Preconditions.checkArgument(detectionArea.contains(respawn), "puzzle[%s].checkPoints[%s].detectionArea must contain puzzle[%s].checkPoints[%s].respawn", puzzleIndex, i, puzzleIndex, i);
            for (int j = 0; j < i; j++) {
                PuzzleDTO.CheckPointDTO earlierCheckpoint = puzzle.getCheckPoints().get(j);
                BoundingBox earlierDetectionArea = earlierCheckpoint.getDetectionArea().toBoundingBox();
                Preconditions.checkArgument(!earlierDetectionArea.overlaps(detectionArea), "puzzle[%s].checkPoints[%s].detectionArea (%s) and puzzle[%s].checkPoints[%s].detectionArea (%s) can't overlap", puzzleIndex, i-1, earlierDetectionArea, puzzleIndex, i, detectionArea);
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
        BoundingBox newGlassBarrier = null;
        if (config.getGlassBarrier() != null) {
            newGlassBarrier = config.getGlassBarrier().toBoundingBox();
        }
        List<TeamSpawn> newTeamSpawns = null;
        if (config.getTeamSpawns() != null) {
            newTeamSpawns = TeamSpawnDTO.toTeamSpawns(newWorld, config.getTeamSpawns());
        }
        List<Puzzle> newPuzzles = PuzzleDTO.toPuzzles(newWorld, config.getPuzzles());
        Location newStartingLocation = newPuzzles.get(0).checkPoints().get(0).respawn();
        Component newGlassBarrierOpenMessage = null;
        if (config.getGlassBarrierOpenMessage() != null) {
            newGlassBarrierOpenMessage = ConfigUtils.toComponent(config.getGlassBarrierOpenMessage());
        }
        Component newTeamSpawnsOpenMessage = null;
        if (config.getTeamSpawnsOpenMessage() != null) {
            newTeamSpawnsOpenMessage = ConfigUtils.toComponent(config.getTeamSpawnsOpenMessage());
        }
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.getDescription());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocation = newStartingLocation;
        this.glassBarrier = newGlassBarrier;
        this.glassBarrierOpenMessage = newGlassBarrierOpenMessage;
        this.teamSpawns = newTeamSpawns;
        this.teamSpawnsOpenMessage = newTeamSpawnsOpenMessage;
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
    
    /**
     * @return the configured list of team spawns. Might be null if unspecified.
     */
    public @Nullable List<TeamSpawn> getTeamSpawns() {
        return teamSpawns;
    }
    
    /**
     * @return the bounding box for the glass barrier. Null if no glass barrier should be spawned.
     */
    public @Nullable BoundingBox getGlassBarrier() {
        return glassBarrier;
    }
    
    public int getTeamSpawnsDuration() {
        return parkourPathwayConfig.getDurations().getTeamSpawn();
    }
    
    public @Nullable Component getGlassBarrierOpenMessage() {
        return glassBarrierOpenMessage;
    }
    
    public @Nullable Component getTeamSpawnsOpenMessage() {
        return teamSpawnsOpenMessage;
    }
}
