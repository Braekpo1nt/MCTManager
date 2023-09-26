package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CaptureTheFlagStorageUtil extends GameConfigStorageUtil<CaptureTheFlagConfig> {
    private CaptureTheFlagConfig captureTheFlagConfig = null;
    private World world;
    private Location spawnObservatory;
    private List<Arena> arenas;
    
    public CaptureTheFlagStorageUtil(File configDirectory) {
        super(configDirectory, "captureTheFlagConfig.json", CaptureTheFlagConfig.class);
    }
    
    @Override
    protected CaptureTheFlagConfig getConfig() {
        return captureTheFlagConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable CaptureTheFlagConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.arenas() != null, "arenas can't be null");
        Preconditions.checkArgument(config.arenas().size() >= 1, "there must be at least 1 arena");
        for (CaptureTheFlagConfig.ArenaDTO arena : config.arenas()) {
            Preconditions.checkArgument(arena.northSpawn() != null, "northSpawn can't be null");
            Preconditions.checkArgument(arena.southSpawn() != null, "southSpawn can't be null");
            Preconditions.checkArgument(arena.northFlag() != null, "northFlag can't be null");
            Preconditions.checkArgument(arena.southFlag() != null, "southFlag can't be null");
            Preconditions.checkArgument(!arena.northFlag().equals(arena.southFlag()), "northFlag and southFlag can't be identical (%s)", arena.northFlag());
            Preconditions.checkArgument(arena.northBarrier() != null, "northBarrier can't be null");
            Preconditions.checkArgument(arena.southBarrier() != null, "southBarrier can't be null");
            Preconditions.checkArgument(arena.barrierSize() != null, "barrierSize can't be null");
            Preconditions.checkArgument(arena.barrierSize().xSize() >= 1
                    && arena.barrierSize().ySize() >= 1
                    && arena.barrierSize().zSize() >= 1, "barrierSize can't have a dimension less than 1 (%s)", arena.barrierSize());
            Preconditions.checkArgument(arena.boundingBox() != null, "boundingBox can't be null");
            BoundingBox arenaBoundingBox = arena.boundingBox().toBoundingBox();
            Preconditions.checkArgument(arenaBoundingBox.getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", arenaBoundingBox, arenaBoundingBox.getVolume());
            Preconditions.checkArgument(arenaBoundingBox.contains(arena.northFlag()), "boundingBox (%s) must contain northFlag (%s)", arenaBoundingBox, arena.northFlag());
            Preconditions.checkArgument(arenaBoundingBox.contains(arena.southFlag()), "boundingBox (%s) must contain southFlag (%s)", arenaBoundingBox, arena.southFlag());
        }
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        BoundingBox spectatorArea = config.spectatorArea().toBoundingBox();
        Preconditions.checkArgument(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().matchesStarting() >= 0, "durations.matchesStarting (%s) can't be negative", config.durations().matchesStarting());
        Preconditions.checkArgument(config.durations().classSelection() >= 0, "durations.classSelection (%s) can't be negative", config.durations().classSelection());
        Preconditions.checkArgument(config.durations().roundTimer() >= 0, "durations.roundTimer (%s) can't be negative", config.durations().roundTimer());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    @Override
    protected void setConfig(CaptureTheFlagConfig config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newSpawnObservatory = config.spawnObservatory().toLocation(newWorld);
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.spawnObservatory = newSpawnObservatory;
        this.arenas = toArenas(config.arenas(), world);
        this.captureTheFlagConfig = config;
    }
    
    private List<Arena> toArenas(List<CaptureTheFlagConfig.ArenaDTO> arenaDTOS, World arenaWorld) {
        List<Arena> newArenas = new ArrayList<>(arenaDTOS.size());
        for (CaptureTheFlagConfig.ArenaDTO arenaDTO : arenaDTOS) {
            newArenas.add(new Arena(
                    arenaDTO.northSpawn().toLocation(arenaWorld),
                    arenaDTO.southSpawn().toLocation(arenaWorld),
                    arenaDTO.northFlag().toLocation(arenaWorld),
                    arenaDTO.southFlag().toLocation(arenaWorld),
                    arenaDTO.northBarrier().toLocation(arenaWorld),
                    arenaDTO.southBarrier().toLocation(arenaWorld),
                    arenaDTO.barrierSize(),
                    arenaDTO.boundingBox().toBoundingBox()
            ));
        }
        return newArenas;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return CaptureTheFlagStorageUtil.class.getResourceAsStream("exampleCaptureTheFlagConfig.json");
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getSpawnObservatory() {
        return spawnObservatory;
    }
    
    public CaptureTheFlagConfig.Scores getScores() {
        return captureTheFlagConfig.scores();
    }
    
    public CaptureTheFlagConfig.Durations getDurations() {
        return captureTheFlagConfig.durations();
    }
    
    public List<Arena> getArenas() {
        return arenas;
    }
    
    public int getMatchesStartingDuration() {
        return captureTheFlagConfig.durations().matchesStarting();
    }
    
    public int getRoundTimerDuration() {
        return captureTheFlagConfig.durations().roundTimer();
    }
    
    public int getClassSelectionDuration() {
        return captureTheFlagConfig.durations().classSelection();
    }
    
    public int getWinScore() {
        return captureTheFlagConfig.scores().win();
    }
    
    public int getKillScore() {
        return captureTheFlagConfig.scores().kill();
    }
}
