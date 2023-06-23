package org.braekpo1nt.mctmanager.games.parkourpathway.io;

import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayConfig {
    protected List<CheckPointConfig> checkpoints;
    protected String world;
    
    public ParkourPathwayConfig() {
        this.checkpoints = new ArrayList<>();
    }
    
    public List<CheckPointConfig> getCheckpoints() {
        return checkpoints;
    }
    
    public void setCheckpoints(List<CheckPointConfig> checkpoints) {
        this.checkpoints = checkpoints;
    }
    
    public String getWorld() {
        return world;
    }
    
    public void setWorld(String world) {
        this.world = world;
    }
}
