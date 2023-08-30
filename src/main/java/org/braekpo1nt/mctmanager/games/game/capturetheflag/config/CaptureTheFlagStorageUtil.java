package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

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
        if (config == null) {
            throw new IllegalArgumentException("Saved config is null");
        }
        World ctfWorld = Bukkit.getWorld(config.world());
        if (ctfWorld == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
        if (config.arenas() == null) {
            throw new IllegalArgumentException("arenas can't be null");
        }
        if (config.arenas().size() < 1) {
            throw new IllegalArgumentException("there must be at least 1 arena");
        }
        for (CaptureTheFlagConfig.ArenaDTO arena : config.arenas()) {
            if (arena.northSpawn() == null) {
                throw new IllegalArgumentException("northSpawn can't be null");
            }
            if (arena.southSpawn() == null) {
                throw new IllegalArgumentException("southSpawn can't be null");
            }
            if (arena.northFlag() == null) {
                throw new IllegalArgumentException("northFlag can't be null");
            }
            if (arena.southFlag() == null) {
                throw new IllegalArgumentException("southFlag can't be null");
            }
            if (arena.northFlag().equals(arena.southFlag())) {
                throw new IllegalArgumentException(String.format("northFlag and southFlag can't be identical (%s)", arena.northFlag()));
            }
            if (arena.northBarrier() == null) {
                throw new IllegalArgumentException("northBarrier can't be null");
            }
            if (arena.southBarrier() == null) {
                throw new IllegalArgumentException("southBarrier can't be null");
            }
            if (arena.barrierSize() == null) {
                throw new IllegalArgumentException("barrierSize can't be null");
            }
            if (arena.barrierSize().xSize() < 1 
                    || arena.barrierSize().ySize() < 1 
                    || arena.barrierSize().zSize() < 1) {
                throw new IllegalArgumentException(String.format("barrierSize can't have a dimension less than 1 (%s)", arena.barrierSize()));
            }
            if (arena.boundingBox() == null) {
                throw new IllegalArgumentException("boundingBox can't be null");
            }
            if (arena.getBoundingBox().getVolume() < 2.0) {
                throw new IllegalArgumentException(String.format("boundingBox (%s) volume (%s) must be at least 2.0", arena.getBoundingBox(), arena.getBoundingBox().getVolume()));
            }
            if (!arena.getBoundingBox().contains(arena.northFlag())) {
                throw new IllegalArgumentException(String.format("boundingBox (%s) must contain northFlag (%s)", arena.getBoundingBox(), arena.northFlag()));
            }
            if (!arena.getBoundingBox().contains(arena.southFlag())) {
                throw new IllegalArgumentException(String.format("boundingBox (%s) must contain southFlag (%s)", arena.getBoundingBox(), arena.southFlag()));
            }
        }
        if (config.spectatorArea() == null) {
            throw new IllegalArgumentException("spectatorArea can't be null");
        }
        if (config.getSpectatorArea().getVolume() < 1.0) {
            throw new IllegalArgumentException(String.format("spectatorArea (%s) volume (%s) must be at least 1.0", config.getSpectatorArea(), config.getSpectatorArea().getVolume()));
        }
        if (config.scores() == null) {
            throw new IllegalArgumentException("scores can't be null");
        }
        if (config.durations() == null) {
            throw new IllegalArgumentException("durations can't be null");
        }
        if (config.durations().matchesStarting() < 0) {
            throw new IllegalArgumentException(String.format("durations.matchesStarting (%s) can't be negative", config.durations().matchesStarting()));
        }
        if (config.durations().classSelection() < 0) {
            throw new IllegalArgumentException(String.format("durations.classSelection (%s) can't be negative", config.durations().classSelection()));
        }
        if (config.durations().roundTimer() < 0) {
            throw new IllegalArgumentException(String.format("durations.roundTimer (%s) can't be negative", config.durations().roundTimer()));
        }
        
        return true;
    }
    
    @Override
    protected void setConfig(CaptureTheFlagConfig config) {
        this.captureTheFlagConfig = config;
        world = Bukkit.getWorld(captureTheFlagConfig.world());
        if (world == null) {
            throw new IllegalArgumentException("world can't be null");
        }
        spawnObservatory = captureTheFlagConfig.spawnObservatory().toLocation(world);
        this.arenas = toArenas(config.arenas(), world);
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
