package org.braekpo1nt.mctmanager.games.parkourpathway.io;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.io.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.parkourpathway.CheckPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayStorageUtil extends GameConfigStorageUtil<ParkourPathwayConfig> {
    
    protected ParkourPathwayConfig parkourPathwayConfig = new ParkourPathwayConfig();
    
    public ParkourPathwayStorageUtil(Main plugin) {
        super(plugin, "parkourPathwayConfig.json", ParkourPathwayConfig.class);
    }
    
    @Override
    protected ParkourPathwayConfig initializeConfig() {
        return new ParkourPathwayConfig();
    }
    
    @Override
    protected ParkourPathwayConfig getConfig() {
        return parkourPathwayConfig;
    }
    
    @Override
    protected void setConfig(ParkourPathwayConfig config) {
        parkourPathwayConfig = config;
    }
    
    public List<CheckPoint> getCheckPoints() {
        World checkpointWorld = Bukkit.getWorld(parkourPathwayConfig.getWorld());
        List<CheckPoint> newCheckpoints = new ArrayList<>();
        for (CheckPointConfig checkpointConfig : parkourPathwayConfig.getCheckpoints()) {
            Vector min = checkpointConfig.min();
            Vector max = checkpointConfig.max();
            BoundingBox boundingBox = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            Vector configRespawn = checkpointConfig.respawn();
            Location respawn = new Location(checkpointWorld, configRespawn.getX(), configRespawn.getY(), configRespawn.getZ());
            newCheckpoints.add(new CheckPoint(checkpointConfig.yValue(), boundingBox, respawn));
        }
        return newCheckpoints;
    }
    
    public void setCheckpoints(List<CheckPoint> checkpoints) throws IOException {
        parkourPathwayConfig = new ParkourPathwayConfig();
        List<CheckPointConfig> checkpointConfigs = new ArrayList<>();
        for (CheckPoint checkpoint : checkpoints) {
            checkpointConfigs.add(new CheckPointConfig(checkpoint.yValue(), checkpoint.boundingBox().getMin(), checkpoint.boundingBox().getMax(), checkpoint.respawn().toVector()));
        }
        parkourPathwayConfig.setCheckpoints(checkpointConfigs);
        saveConfig();
    }
    
    public void setWorld(String world) throws IOException {
        parkourPathwayConfig.setWorld(world);
        saveConfig();
    }
}
