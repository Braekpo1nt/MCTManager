package org.braekpo1nt.mctmanager.games.game.clockwork.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClockworkStorageUtil extends GameConfigStorageUtil<ClockworkConfig> {
    
    private ClockworkConfig clockworkConfig;
    private World world;
    private Location startingLocation;
    private List<Wedge> wedges;
    
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public ClockworkStorageUtil(File configDirectory) {
        super(configDirectory, "clockworkConfig.json", ClockworkConfig.class);
    }
    
    @Override
    protected ClockworkConfig getConfig() {
        return clockworkConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable ClockworkConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocation() != null, "startingLocation can't be null");
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.getSpectatorArea().getVolume() >= 1.0, "spectatorArea (%s) must have a volume (%s) of at least 1.0", config.spectatorArea(), config.getSpectatorArea().getVolume());
        Preconditions.checkArgument(config.wedges() != null, "wedges can't be null");
        Preconditions.checkArgument(config.wedges().size() == 12, "wedges must have 12 entries");
        for (ClockworkConfig.WedgeDTO wedgeDTO : config.wedges()) {
            Preconditions.checkArgument(wedgeDTO != null, "wedge can't be null");
            Preconditions.checkArgument(wedgeDTO.detectionArea() != null, "wedge.detectionArea can't be null");
            Preconditions.checkArgument(wedgeDTO.getDetectionArea().getVolume() >= 1.0, "wedge.detectionArea (%s) volume (%s) must be at least 1.0", wedgeDTO.detectionArea(), wedgeDTO.getDetectionArea().getVolume());
        }
        Preconditions.checkArgument(config.rounds() >= 1, "rounds must be at least 1");
        Preconditions.checkArgument(config.clockChime() != null, "clockChime can't be null");
        Preconditions.checkArgument(config.clockChime().sound() != null, "clockChime.sound can't be null");
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().breather() >= 0, "durations.breather can't be negative");
        Preconditions.checkArgument(config.durations().getToWedge() >= 0, "durations.getToWedge can't be negative");
        Preconditions.checkArgument(config.durations().stayOnWedge() >= 0, "durations.stayOnWedge can't be negative");
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    @Override
    protected void setConfig(ClockworkConfig config) throws IllegalArgumentException {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newStartingLocation = config.startingLocation().toLocation(newWorld);
        List<Wedge> newWedges = new ArrayList<>(config.wedges().size());
        for (ClockworkConfig.WedgeDTO wedgeDTO : config.wedges()) {
            newWedges.add(new Wedge(wedgeDTO.getDetectionArea()));
        }
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocation = newStartingLocation;
        this.wedges = newWedges;
        this.clockworkConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return ClockworkStorageUtil.class.getResourceAsStream("exampleClockworkConfig.json");
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getStartingLocation() {
        return startingLocation;
    }
    
    public List<Wedge> getWedges() {
        return wedges;
    }
    
    public int getRounds() {
        return clockworkConfig.rounds();
    }
    
    public int getPlayerEliminationScore() {
        return clockworkConfig.scores().playerElimination();
    }
    
    public int getTeamEliminationScore() {
        return clockworkConfig.scores().teamElimination();
    }
    
    public int getWinRoundScore() {
        return clockworkConfig.scores().winRound();
    }
    
    public int getBreatherDuration() {
        return clockworkConfig.durations().breather();
    }
    
    public int getGetToWedgeDuration() {
        return clockworkConfig.durations().getToWedge();
    }
    
    public String getClockChimeSound() {
        return clockworkConfig.clockChime().sound();
    }
    
    public float getClockChimeVolume() {
        return clockworkConfig.clockChime().volume();
    }
    
    public float getClockChimePitch() {
        return clockworkConfig.clockChime().pitch();
    }
    
    public int getStayOnWedgeDuration() {
        return clockworkConfig.durations().stayOnWedge();
    }
}
