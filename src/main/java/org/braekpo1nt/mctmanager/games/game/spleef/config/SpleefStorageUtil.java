package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

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
        Preconditions.checkArgument(world != null, "Could not find world \"%s\"", config.world());
        startingLocation = config.startingLocation().toLocation(world);
        this.spleefConfig = config;
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocation() != null, "startingLocation can't be null");
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.getSpectatorArea().getVolume() >= 1.0, "spectatorArea (%s) must have a volume (%s) of at least 1.0", config.spectatorArea(), config.getSpectatorArea().getVolume());
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().roundStarting() >= 0, "durations.roundStarting (%s) can't be negative", config.durations().roundStarting());
        Preconditions.checkArgument(config.durations().decayTopLayers() >= 0, "durations.decayTopLayers (%s) can't be negative", config.durations().decayTopLayers());
        Preconditions.checkArgument(config.durations().decayBottomLayers() >= 0, "duration.decayBottomLayers (%s) can't be negative", config.durations().decayBottomLayers());
        Preconditions.checkArgument(config.durations().roundEnding() >= 0, "duration.roundEnding (%s) can't be negative", config.durations().roundEnding());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
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
    
    public int getRoundStartingDuration() {
        return spleefConfig.durations().roundStarting();
    }

    public int getDecayTopLayersDuration() {
        return spleefConfig.durations().decayTopLayers();
    }

    public int getDecayBottomLayersDuration() {
        return spleefConfig.durations().decayBottomLayers();
    }

    public int getRoundEndingDuration() {
        return spleefConfig.durations().roundEnding();
    }
    
    public int getSurviveScore() {
        return spleefConfig.scores().survive();
    }
}
