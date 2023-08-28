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
        double volume = config.spectatorBoundary().getVolume();
        if (volume <= 1) {
            throw new IllegalArgumentException(String.format("Defined spectatorBoundary must have a volume greater than 1: %s", config.spectatorBoundary()));
        }
        if (config.durations().decayTopLayers() < 0) {
            throw new IllegalArgumentException(String.format("Duration decayTopLayers can't be negative: %s", config.durations().decayTopLayers()));
        }
        if (config.durations().decayBottomLayers() < 0) {
            throw new IllegalArgumentException(String.format("Duration decayBottomLayers can't be negative: %s", config.durations().decayBottomLayers()));
        }
        return true;
    }
    
    @Override
    protected InputStream getDefaultResourceStream() {
        return SpleefStorageUtil.class.getResourceAsStream("exampleSpleefConfig.json");
    }
    
    public Location getStartingLocation() {
        return spleefConfig.startingLocation().toLocation(world);
    }
    
    public World getWorld() {
        return world;
    }
}
