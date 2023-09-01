package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
            Preconditions.checkArgument(arena.getBoundingBox().getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", arena.getBoundingBox(), arena.getBoundingBox().getVolume());
            Preconditions.checkArgument(arena.getBoundingBox().contains(arena.northFlag()), "boundingBox (%s) must contain northFlag (%s)", arena.getBoundingBox(), arena.northFlag());
            Preconditions.checkArgument(arena.getBoundingBox().contains(arena.southFlag()), "boundingBox (%s) must contain southFlag (%s)", arena.getBoundingBox(), arena.southFlag());
        }
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.getSpectatorArea().getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", config.getSpectatorArea(), config.getSpectatorArea().getVolume());
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().matchesStarting() >= 0, "durations.matchesStarting (%s) can't be negative", config.durations().matchesStarting());
        Preconditions.checkArgument(config.durations().classSelection() >= 0, "durations.classSelection (%s) can't be negative", config.durations().classSelection());
        Preconditions.checkArgument(config.durations().roundTimer() >= 0, "durations.roundTimer (%s) can't be negative", config.durations().roundTimer());
        return true;
    }
    
    @Override
    protected void setConfig(CaptureTheFlagConfig config) {
        world = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(world != null, "Could not find world \"%s\"", config.world());
        spawnObservatory = config.spawnObservatory().toLocation(world);
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
                    arenaDTO.getBoundingBox()
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
}
