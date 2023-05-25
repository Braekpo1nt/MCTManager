package org.braekpo1nt.mctmanager.games.parkourpathway;

import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayConfig {
    protected List<List<CheckPointConfig>> checkpoints;
    
    public ParkourPathwayConfig() {
        this.checkpoints = new ArrayList<>();
    }
    
    public List<List<CheckPointConfig>> getCheckpoints() {
        return checkpoints;
    }
    
    public void setCheckpoints(List<List<CheckPointConfig>> checkpoints) {
        this.checkpoints = checkpoints;
    }
}
