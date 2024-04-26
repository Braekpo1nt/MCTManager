package org.braekpo1nt.mctmanager.games.game.clockwork.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.scoreboard.Team;
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
    private Component description;
    
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
        Preconditions.checkArgument(Main.VALID_CONFIG_VERSIONS.contains(config.version()), "invalid config version (%s)", config.version());
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.startingLocation() != null, "startingLocation can't be null");
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        Preconditions.checkArgument(config.spectatorArea().toBoundingBox().getVolume() >= 1.0, "spectatorArea (%s) must have a volume (%s) of at least 1.0", config.spectatorArea(), config.spectatorArea().toBoundingBox().getVolume());
        validateChaos(config.chaos());
        Preconditions.checkArgument(config.wedges() != null, "wedges can't be null");
        Preconditions.checkArgument(config.wedges().size() == 12, "wedges must have 12 entries");
        for (ClockworkConfig.WedgeDTO wedgeDTO : config.wedges()) {
            Preconditions.checkArgument(wedgeDTO != null, "wedge can't be null");
            Preconditions.checkArgument(wedgeDTO.detectionArea() != null, "wedge.detectionArea can't be null");
            Preconditions.checkArgument(wedgeDTO.detectionArea().toBoundingBox().getVolume() >= 1.0, "wedge.detectionArea (%s) volume (%s) must be at least 1.0", wedgeDTO.detectionArea(), wedgeDTO.detectionArea().toBoundingBox().getVolume());
        }
        Preconditions.checkArgument(config.rounds() >= 1, "rounds must be at least 1");
        Preconditions.checkArgument(config.clockChime() != null, "clockChime can't be null");
        config.clockChime().isValid();
        Preconditions.checkArgument(config.initialChimeInterval() >= 0, "initialChimeInterval can't be negative");
        Preconditions.checkArgument(config.chimeIntervalDecrement() >= 0, "chimeIntervalDecrement can't be negative");
        Preconditions.checkArgument(config.chimeIntervalDecrement() > 0, "chimeIntervalDecrement (%s) can't be greater than initialChimeInterval (%s)", config.chimeIntervalDecrement(), config.initialChimeInterval());
        Preconditions.checkArgument(config.collisionRule() != null, "collisionRule can't be null");
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
    
    private void validateChaos(ClockworkConfig.Chaos chaos) {
        Preconditions.checkArgument(chaos != null, "chaos can't be null");
        Preconditions.checkArgument(chaos.cylinder() != null, "chaos.cylinder can't be null");
        Preconditions.checkArgument(chaos.cylinder().spawnY() != null, "chaos.cylinder.spawnY can't be null");
        Preconditions.checkArgument(chaos.cylinder().spawnY().min() < chaos.cylinder().spawnY().max(), "chaos.cylinder.spawnY min must be less than max");
        
        Preconditions.checkArgument(chaos.arrows() != null, "chaos.arrows can't be null");
        Preconditions.checkArgument(chaos.arrows().initial() != null, "chaos.arrows.initial can't be null");
        Preconditions.checkArgument(chaos.arrows().increment() != null, "chaos.arrows.increment can't be null");
        Preconditions.checkArgument(((int) chaos.arrows().initial().min()) < ((int) chaos.arrows().initial().max())+1, "chaos.arrows.initial floor(min) must be less than floor(max)+1");
        Preconditions.checkArgument(chaos.arrows().increment().min() <= chaos.arrows().increment().max(), "chaos.arrows.increment min must be less than or equal to max");
    
        Preconditions.checkArgument(chaos.fallingBlocks() != null, "chaos.fallingBlocks can't be null");
        Preconditions.checkArgument(chaos.fallingBlocks().initial() != null, "chaos.fallingBlocks.initial can't be null");
        Preconditions.checkArgument(chaos.fallingBlocks().increment() != null, "chaos.fallingBlocks.increment can't be null");
        Preconditions.checkArgument(((int) chaos.fallingBlocks().initial().min()) < ((int) chaos.fallingBlocks().initial().max())+1, "chaos.fallingBlocks.initial floor(min) must be less than floor(max)+1");
        Preconditions.checkArgument(chaos.fallingBlocks().increment().min() <= chaos.fallingBlocks().increment().max(), "chaos.fallingBlocks.increment min must be less than or equal to max");
    
        Preconditions.checkArgument(chaos.summonDelay() != null, "chaos.summonDelay can't be null");
        Preconditions.checkArgument(chaos.summonDelay().initial() != null, "chaos.summonDelay.initial can't be null");
        Preconditions.checkArgument(chaos.summonDelay().decrement() != null, "chaos.summonDelay.decrement can't be null");
        Preconditions.checkArgument(chaos.summonDelay().initial().min() >= 5, "chaos.summonDelay.initial.min must be greater than or equal to 5");
        Preconditions.checkArgument(((long) chaos.summonDelay().initial().min()) < ((long) chaos.summonDelay().initial().min() + 1), "chaos.summonDelay.initial floor(min) must be less than floor(max)+1");
        Preconditions.checkArgument(chaos.summonDelay().decrement().min() <= chaos.summonDelay().decrement().max(), "chaos.summonDelay.decrement min must be less than or equal to max");
        
        Preconditions.checkArgument(chaos.arrowSpeed() != null, "chaos.arrowSpeed can't be null");
        Preconditions.checkArgument(chaos.arrowSpeed().min() >= 0f, "chaos.arrowSpeed.min can't be negative");
        Preconditions.checkArgument(chaos.arrowSpeed().max() >= 0f, "chaos.arrowSpeed.max can't be negative");
    
        Preconditions.checkArgument(chaos.arrowSpread() != null, "chaos.arrowSpread can't be null");
        Preconditions.checkArgument(chaos.arrowSpread().min() >= 0f, "chaos.arrowSpread.min can't be negative");
        Preconditions.checkArgument(chaos.arrowSpread().max() >= 0f, "chaos.arrowSpread.max can't be negative");
    }
    
    @Override
    protected void setConfig(ClockworkConfig config) throws IllegalArgumentException {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newStartingLocation = config.startingLocation().toLocation(newWorld);
        List<Wedge> newWedges = new ArrayList<>(config.wedges().size());
        for (ClockworkConfig.WedgeDTO wedgeDTO : config.wedges()) {
            newWedges.add(new Wedge(wedgeDTO.detectionArea().toBoundingBox()));
        }
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.startingLocation = newStartingLocation;
        this.wedges = newWedges;
        this.description = newDescription;
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
        return clockworkConfig.clockChime().getKey();
    }
    
    public float getClockChimeVolume() {
        return clockworkConfig.clockChime().getVolume();
    }
    
    public float getClockChimePitch() {
        return clockworkConfig.clockChime().getPitch();
    }
    
    public int getStayOnWedgeDuration() {
        return clockworkConfig.durations().stayOnWedge();
    }
    
    public double getInitialChimeInterval() {
        return clockworkConfig.initialChimeInterval();
    }
    
    public double getChimeIntervalDecrement() {
        return clockworkConfig.chimeIntervalDecrement();
    }
    
    public ClockworkConfig.Chaos getChaos() {
        return clockworkConfig.chaos();
    }
    
    /**
     * @return the collision rule for players
     */
    public Team.OptionStatus getCollisionRule() {
        return clockworkConfig.collisionRule();
    }
    
    public Component getDescription() {
        return description;
    }
}
