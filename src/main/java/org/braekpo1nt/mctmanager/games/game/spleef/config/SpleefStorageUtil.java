package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
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
    private Location startingLocation;
    
    public SpleefStorageUtil(File configDirectory) {
        super(configDirectory, "spleefConfig.json", SpleefConfig.class);
    }
    
    @Override
    protected SpleefConfig getConfig() {
        return spleefConfig;
    }
    
    @Override
    protected void setConfig(SpleefConfig config) {
        world = Bukkit.getWorld(config.world());
        if (world == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
        startingLocation = config.startingLocation().toLocation(world);
        this.spleefConfig = config;
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocation() != null, "startingLocation can't be null");
        Preconditions.checkArgument(config.spectatorBoundary() != null, "spectatorBoundary can't be null");
        Preconditions.checkArgument(config.getSpectatorBoundary().getVolume() >= 1.0, "spectatorBoundary (%s) must have a volume (%s) of at least 1.0", config.spectatorBoundary(), config.getSpectatorBoundary().getVolume());
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().decayTopLayers() >= 0, "Duration decayTopLayers (%s) can't be negative", config.durations().decayTopLayers());
        Preconditions.checkArgument(config.durations().decayBottomLayers() >= 0, "Duration decayBottomLayers (%s) can't be negative", config.durations().decayBottomLayers());
        return true;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return SpleefStorageUtil.class.getResourceAsStream("exampleSpleefConfig.json");
    }
    
    public Location getStartingLocation() {
        return startingLocation;
    }
    
    public World getWorld() {
        return world;
    }
}
