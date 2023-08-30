package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

public class SpleefStorageUtil extends GameConfigStorageUtil<SpleefConfig> {
    
    protected SpleefConfig spleefConfig = null;
    private World world;
    
    public SpleefStorageUtil(File configDirectory) {
        super(configDirectory, "spleefConfig.json", SpleefConfig.class);
    }
    
    @Override
    protected SpleefConfig getConfig() {
        return spleefConfig;
    }
    
    @Override
    protected void setConfig(SpleefConfig config) {
        this.spleefConfig = config;
        world = Bukkit.getWorld(spleefConfig.world());
        if (world == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfig config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Saved config is null");
        }
        World spleefWorld = Bukkit.getWorld(config.world());
        if (spleefWorld == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
        if (config.startingLocation() == null) {
            throw new IllegalArgumentException("startingLocation can't be null");
        }
        if (config.spectatorBoundary() == null) {
            throw new IllegalArgumentException("spectatorBoundary can't be null");
        }
        double volume = config.getSpectatorBoundary().getVolume();
        if (volume <= 1.0) {
            throw new IllegalArgumentException(String.format("spectatorBoundary (%s) must have a volume greater than 1.0, but its %s", config.spectatorBoundary(), volume));
        }
        if (config.scores() == null) {
            throw new IllegalArgumentException("scores can't be null");
        }
        // score value can be anything, even negative
        if (config.durations() == null) {
            throw new IllegalArgumentException("durations can't be null");
        }
        if (config.durations().decayTopLayers() < 0) {
            throw new IllegalArgumentException(String.format("Duration decayTopLayers (%s) can't be negative", config.durations().decayTopLayers()));
        }
        if (config.durations().decayBottomLayers() < 0) {
            throw new IllegalArgumentException(String.format("Duration decayBottomLayers (%s) can't be negative", config.durations().decayBottomLayers()));
        }
        return true;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return SpleefStorageUtil.class.getResourceAsStream("exampleSpleefConfig.json");
    }
    
    public Location getStartingLocation() {
        return spleefConfig.startingLocation().toLocation(world);
    }
    
    public World getWorld() {
        return world;
    }
}
