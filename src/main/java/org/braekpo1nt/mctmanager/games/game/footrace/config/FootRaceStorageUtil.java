package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class FootRaceStorageUtil extends GameConfigStorageUtil<FootRaceConfig> {
    private FootRaceConfig footRaceConfig;
    private World world;
    private Location startingLocation;
    private Component description;
    
    public FootRaceStorageUtil(File configDirectory) {
        super(configDirectory, "footRaceConfig.json", FootRaceConfig.class);
    }
    
    @Override
    protected FootRaceConfig getConfig() {
        return footRaceConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable FootRaceConfig config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Saved config is null");
        }
        if (config.world() == null) {
            throw new IllegalArgumentException("world can't be null");
        }
        if (Bukkit.getWorld(config.world()) == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
        if (config.startingLocation() == null) {
            throw new IllegalArgumentException("startingLocation can't be null");
        }
        if (config.finishLine() == null) {
            throw new IllegalArgumentException("finishLine can't be null");
        }
        if (config.getFinishLine().getVolume() < 1.0) {
            throw new IllegalArgumentException(String.format("finishLine's volume (%s) can't be less than 1. %s", config.getFinishLine().getVolume(), config.getFinishLine()));
        }
        if (config.getFinishLine().contains(config.startingLocation())) {
            throw new IllegalArgumentException(String.format("startingLocation (%S) can't be inside finishLine (%S)", config.startingLocation(), config.getFinishLine()));
        }
        if (config.spectatorArea() == null) {
            throw new IllegalArgumentException("spectatorArea can't be null");
        }
        if (config.getSpectatorArea().getVolume() < 1.0) {
            throw new IllegalArgumentException(String.format("getSpectatorArea's volume (%s) can't be less than 1. %s", config.getSpectatorArea().getVolume(), config.getSpectatorArea()));
        }
        if (config.scores() == null) {
            throw new IllegalArgumentException("scores can't be null");
        }
        if (config.scores().placementPoints() == null) {
            throw new IllegalArgumentException("placementPoints can't be null");
        }
        if (config.scores().placementPoints().length < 1) {
            throw new IllegalArgumentException("placementPoints must have at least one entry");
        }
        if (config.durations() == null) {
            throw new IllegalArgumentException("durations can't be null");
        }
        if (config.durations().startRace() < 0) {
            throw new IllegalArgumentException(String.format("durations.startRace (%s) can't be negative", config.durations().startRace()));
        }
        if (config.durations().raceEndCountdown() < 0) {
            throw new IllegalArgumentException(String.format("durations.raceEndCountdown (%s) can't be negative", config.durations().raceEndCountdown()));
        }
        if (config.description() == null) {
            throw new IllegalArgumentException("description can't be null");
        }
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
        
    }
    
    @Override
    protected void setConfig(FootRaceConfig config) {
        description = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        world = Bukkit.getWorld(config.world());
        if (world == null) {
            throw new IllegalArgumentException(String.format("Could not find world \"%s\"", config.world()));
        }
        startingLocation = config.startingLocation().toLocation(world);
        this.footRaceConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return FootRaceStorageUtil.class.getResourceAsStream("exampleFootRaceConfig.json");
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getStartingLocation() {
        return startingLocation;
    }
    
    public FootRaceConfig.Scores getScores() {
        return footRaceConfig.scores();
    }
    
    public BoundingBox getFinishLine() {
        return footRaceConfig.getFinishLine();
    }
    
    public int getStartRaceDuration() {
        return footRaceConfig.durations().startRace();
    }
    
    public int getRaceEndCountdownDuration() {
        return footRaceConfig.durations().raceEndCountdown();
    }
    
    public int[] getPlacementPoints() {
        return footRaceConfig.scores().placementPoints();
    }
    
    public int getDetriment() {
        return footRaceConfig.scores().detriment();
    }
    
    public Component getDescription() {
        return description;
    }
    
}
