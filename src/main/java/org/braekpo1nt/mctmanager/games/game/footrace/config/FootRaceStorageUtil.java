package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.GameConfigStorageUtil;
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
    private BoundingBox finishLine;
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
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(Main.VALID_CONFIG_VERSIONS.contains(config.version()), "invalid config version (%s)", config.version());
        Preconditions.checkArgument(config.world() != null, "world can't be null");
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocation() != null, "startingLocation can't be null");
        Preconditions.checkArgument(config.finishLine() != null, "finishLine can't be null");
        BoundingBox finishLine = config.finishLine().toBoundingBox();
        Preconditions.checkArgument(finishLine.getVolume() >= 1.0, "finishLine's volume (%s) can't be less than 1. %s", finishLine.getVolume(), finishLine);
        Preconditions.checkArgument(!finishLine.contains(config.startingLocation().toVector()), "startingLocation (%S) can't be inside finishLine (%S)", config.startingLocation(), finishLine);
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.spectatorArea().toBoundingBox().getVolume() >= 1.0, "getSpectatorArea's volume (%s) can't be less than 1. %s", config.spectatorArea().toBoundingBox().getVolume(), config.spectatorArea().toBoundingBox());
        Preconditions.checkArgument(config.glassBarrier() != null, "glassBarrier can't be null");
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.scores().placementPoints() != null, "placementPoints can't be null");
        Preconditions.checkArgument(config.scores().placementPoints().length >= 1, "placementPoints must have at least one entry");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().startRace() >= 0, "durations.startRace (%s) can't be negative", config.durations().startRace());
        Preconditions.checkArgument(config.durations().raceEndCountdown() >= 0, "durations.raceEndCountdown (%s) can't be negative", config.durations().raceEndCountdown());
        Preconditions.checkArgument(config.description() != null, "description can't be null");
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
        
    }
    
    @Override
    protected void setConfig(FootRaceConfig config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newStartingLocation = config.startingLocation().toLocation(newWorld);
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocation = newStartingLocation;
        this.finishLine = config.finishLine().toBoundingBox();
        this.description = newDescription;
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
        return finishLine;
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
    
    public BoundingBox getGlassBarrier() {
        return footRaceConfig.glassBarrier().toBoundingBox();
    }
    
    public Component getDescription() {
        return description;
    }
    
    public int getCompleteLap() {
        return footRaceConfig.scores().completeLap();
    }
}
