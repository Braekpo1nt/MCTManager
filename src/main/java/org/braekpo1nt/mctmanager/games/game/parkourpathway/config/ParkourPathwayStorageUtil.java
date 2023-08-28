package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.CheckPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayStorageUtil extends GameConfigStorageUtil<ParkourPathwayConfig> {
    
    protected ParkourPathwayConfig parkourPathwayConfig = getExampleConfig();
    
    public ParkourPathwayStorageUtil(File configDirectory) {
        super(configDirectory, "parkourPathwayConfig.json", ParkourPathwayConfig.class);
    }
    
    @Override
    protected ParkourPathwayConfig getConfig() {
        return parkourPathwayConfig;
    }
    
    @Override
    protected void setConfig(ParkourPathwayConfig config) {
        parkourPathwayConfig = config;
    }
    
    @Override
    protected boolean configIsValid(@Nullable ParkourPathwayConfig config) {
        return true;
    }
    
    @Override
    protected InputStream getDefaultResourceStream() {
        return ParkourPathwayStorageUtil.class.getResourceAsStream("exampleParkourPathwayConfig.json");
    }
    
    public List<CheckPoint> getCheckPoints() {
        World checkpointWorld = Bukkit.getWorld(parkourPathwayConfig.getWorld());
        List<CheckPoint> newCheckpoints = new ArrayList<>();
        for (CheckPointDTO checkpointDTO : parkourPathwayConfig.getCheckpoints()) {
            Vector min = checkpointDTO.min();
            Vector max = checkpointDTO.max();
            BoundingBox boundingBox = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            Vector configRespawn = checkpointDTO.respawn();
            Location respawn = new Location(checkpointWorld, configRespawn.getX(), configRespawn.getY(), configRespawn.getZ());
            newCheckpoints.add(new CheckPoint(checkpointDTO.yValue(), boundingBox, respawn));
        }
        return newCheckpoints;
    }
    
    public void setCheckpoints(List<CheckPoint> checkpoints) {
        parkourPathwayConfig = new ParkourPathwayConfig();
        List<CheckPointDTO> checkpointDTOS = new ArrayList<>();
        for (CheckPoint checkpoint : checkpoints) {
            checkpointDTOS.add(new CheckPointDTO(checkpoint.yValue(), checkpoint.boundingBox().getMin(), checkpoint.boundingBox().getMax(), checkpoint.respawn().toVector()));
        }
        parkourPathwayConfig.setCheckpoints(checkpointDTOS);
        saveConfig();
    }
    
    public void setWorld(String world) {
        parkourPathwayConfig.setWorld(world);
        saveConfig();
    }
    
    public int getTimeLimit() {
        return parkourPathwayConfig.getTimeLimit();
    }
    
    public int getCheckpointCounter() {
        return parkourPathwayConfig.getCheckpointCounter();
    }
    
    public int getCheckpointCounterAlert() {
        return parkourPathwayConfig.getCheckpointCounterAlert();
    }
    
    public String getWorld() {
        return parkourPathwayConfig.getWorld();
    }
}
