package org.braekpo1nt.mctmanager.games.parkourpathway.config;

import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayConfig {
    protected int timeLimit;
    protected int checkpointCounter;
    protected int checkpointCounterAlert;
    protected List<CheckPointDTO> checkpoints;
    protected String world;
    
    public ParkourPathwayConfig() {
        this.checkpoints = new ArrayList<>();
    }
    
    public List<CheckPointDTO> getCheckpoints() {
        return checkpoints;
    }
    
    public void setCheckpoints(List<CheckPointDTO> checkpoints) {
        this.checkpoints = checkpoints;
    }
    
    public String getWorld() {
        return world;
    }
    
    public void setWorld(String world) {
        this.world = world;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public int getCheckpointCounter() {
        return checkpointCounter;
    }
    
    public int getCheckpointCounterAlert() {
        return checkpointCounterAlert;
    }
}
