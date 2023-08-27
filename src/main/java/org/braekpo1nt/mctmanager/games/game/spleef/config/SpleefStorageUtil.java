package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class SpleefStorageUtil extends GameConfigStorageUtil<SpleefConfig> {
    
    protected SpleefConfig spleefConfig = null;
    
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
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfig config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Saved config is null");
        }
        World mechaWorld = Bukkit.getWorld(config.world());
        if (mechaWorld == null) {
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
        return SpleefStorageUtil.class.getResourceAsStream("defaultSpleefConfig.json");
    }
    
    public Location getStartingLocation() {
        return spleefConfig.startingLocation().toLocation(Bukkit.getWorld(spleefConfig.world()));
    }
    
    public World getWorld() {
        return Bukkit.getWorld(spleefConfig.world());
    }
}
