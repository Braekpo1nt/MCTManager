package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.CheckPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayStorageUtil extends GameConfigStorageUtil<ParkourPathwayConfig> {
    
    protected ParkourPathwayConfig parkourPathwayConfig = new ParkourPathwayConfig();
    protected final Main plugin;
    
    public ParkourPathwayStorageUtil(Main plugin) {
        super(plugin, "parkourPathwayConfig.json", ParkourPathwayConfig.class);
        this.plugin = plugin;
    }
    
    
    
    @Override
    protected ParkourPathwayConfig getConfig() {
        return parkourPathwayConfig;
    }
    
    @Override
    protected void setConfig(ParkourPathwayConfig config) {
        parkourPathwayConfig = config;
    }
    
    /**
     * Returns a new instance of the default config. Note that the returned config instance may
     * be modified, so this should return a fresh instance to avoid errors with future default
     * uses.
     *
     * @return A new config instance with default values for use if no user-config is present
     */
    @Override
    public @NotNull ParkourPathwayConfig getDefaultConfig() {
        InputStream inputStream = plugin.getClass().getResourceAsStream("/parkourpathway/defaultParkourPathwayConfig.json");
        if (inputStream == null) {
            return new ParkourPathwayConfig();
        }
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        ParkourPathwayConfig defaultConfig = gson.fromJson(reader, ParkourPathwayConfig.class);
        if (defaultConfig == null) {
            return new ParkourPathwayConfig();
        }
        return defaultConfig;
    }
    
    @Override
    protected InputStream getDefaultResourceStream() {
        return ParkourPathwayStorageUtil.class.getResourceAsStream("defaultParkourPathwayConfig.json");
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
