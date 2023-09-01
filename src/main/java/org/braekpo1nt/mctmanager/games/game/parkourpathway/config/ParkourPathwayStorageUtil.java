package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.CheckPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayStorageUtil extends GameConfigStorageUtil<ParkourPathwayConfig> {
    
    protected ParkourPathwayConfig parkourPathwayConfig = getExampleConfig();
    private List<CheckPoint> checkPoints;
    private World world;
    
    public ParkourPathwayStorageUtil(File configDirectory) {
        super(configDirectory, "parkourPathwayConfig.json", ParkourPathwayConfig.class);
    }
    
    @Override
    protected ParkourPathwayConfig getConfig() {
        return parkourPathwayConfig;
    }
    
    @Override
    protected void setConfig(ParkourPathwayConfig config) {
        world = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(world != null, "Could not find world \"%s\"", config.world());
        checkPoints = new ArrayList<>();
        for (ParkourPathwayConfig.CheckPointDTO checkpointDTO : config.checkpoints()) {
            Vector configRespawn = checkpointDTO.respawn();
            Location respawn = new Location(world, configRespawn.getX(), configRespawn.getY(), configRespawn.getZ());
            checkPoints.add(new CheckPoint(checkpointDTO.yValue(), checkpointDTO.getDetectionBox(), respawn));
        }
        this.parkourPathwayConfig = config;
    }
    
    @Override
    protected boolean configIsValid(@Nullable ParkourPathwayConfig config) {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().timeLimit() >= 2, "durations.timeLimit (%s) can't be less than 2", config.durations().timeLimit());
        Preconditions.checkArgument(config.durations().checkpointCounter() >= 1, "durations.checkpointCounter (%s) can't be less than 1", config.durations().checkpointCounter());
        Preconditions.checkArgument(config.durations().checkpointCounterAlert() >= 1 && config.durations().checkpointCounter() >= config.durations().checkpointCounterAlert(), "durations.checkpointCounterAlert (%s) can't be less than 0 or greater than durations.checkpointCounter", config.durations().checkpointCounterAlert());
        Preconditions.checkArgument(config.checkpoints() != null, "checkpoints can't be null");
        Preconditions.checkArgument(config.checkpoints().size() >= 3, "checkpoints must have at least 3 checkpoints");
        for (int i = 0; i < config.checkpoints().size(); i++) {
            ParkourPathwayConfig.CheckPointDTO checkPoint = config.checkpoints().get(i);
            Preconditions.checkArgument(checkPoint != null, "checkpoint %s is null", i);
            Preconditions.checkArgument(checkPoint.getDetectionBox() != null, "checkpoint %s's detectionBox is null", i);
            Preconditions.checkArgument(checkPoint.getDetectionBox().getVolume() >= 1, "detectionBox's volume (%s) can't be less than 1. %s", checkPoint.getDetectionBox().getVolume(), checkPoint.getDetectionBox());
            Preconditions.checkArgument(checkPoint.respawn().getY() >= checkPoint.yValue(), "checkpoint's respawn's y-value (%s) can't be lower than its yValue (%s)", checkPoint.respawn().getY(), checkPoint.yValue());
            if (i-1 >= 0) {
                ParkourPathwayConfig.CheckPointDTO lastCheckPoint = config.checkpoints().get(i-1);
                Preconditions.checkArgument(checkPoint.getDetectionBox().getMaxY() >= lastCheckPoint.yValue(), "checkpoint %s's detectionBox (%s) can't have a maxY (%s) lower than checkpoint %s's yValue (%s)", i, checkPoint.getDetectionBox(), checkPoint.getDetectionBox().getMaxY(), i-1, lastCheckPoint.yValue());
                
                Preconditions.checkArgument(!checkPoint.getDetectionBox().contains(lastCheckPoint.respawn()), "checkpoint %s's detectionBox (%s) can't contain checkpoint %s's respawn (%s)", i, checkPoint.getDetectionBox(), i-1, lastCheckPoint.respawn());
            }
        }
        return true;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return ParkourPathwayStorageUtil.class.getResourceAsStream("exampleParkourPathwayConfig.json");
    }
    
    public List<CheckPoint> getCheckPoints() {
        return checkPoints;
    }
    
    /**
     * @return the time limit for the entire game
     */
    public int getTimeLimit() {
        return parkourPathwayConfig.durations().timeLimit();
    }
    
    /**
     * @return how long (in seconds) the game should wait before declaring that no one has made it to a new checkpoint and ending the game
     */
    public int getCheckpointCounter() {
        return parkourPathwayConfig.durations().checkpointCounter();
    }
    
    /**
     * @return How much time (seconds) should be left in the checkpointCounter before you start displaying the countdown to the users
     */
    public int getCheckpointCounterAlert() {
        return parkourPathwayConfig.durations().checkpointCounterAlert();
    }
    
    public World getWorld() {
        return world;
    }
}
