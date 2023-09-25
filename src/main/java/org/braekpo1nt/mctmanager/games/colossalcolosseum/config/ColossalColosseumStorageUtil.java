package org.braekpo1nt.mctmanager.games.colossalcolosseum.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class ColossalColosseumStorageUtil extends GameConfigStorageUtil<ColossalColosseumConfig> {
    
    private ColossalColosseumConfig colossalColosseumConfig;
    private World world;
    private Location firstPlaceSpawn;
    private Location secondPlaceSpawn;
    private Location spectatorSpawn;
    
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public ColossalColosseumStorageUtil(File configDirectory) {
        super(configDirectory, "colossalColosseumConfig.json", ColossalColosseumConfig.class);
    }
    
    @Override
    protected ColossalColosseumConfig getConfig() {
        return colossalColosseumConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable ColossalColosseumConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.firstPlaceSpawn() != null, "firstPlaceSpawn can't be null");
        Preconditions.checkArgument(config.secondPlaceSpawn() != null, "secondPlaceSpawn can't be null");
        Preconditions.checkArgument(config.spectatorSpawn() != null, "spectatorSpawn can't be null");
        Preconditions.checkArgument(config.requiredWins() > 0, "requiredWins must be greater than 0");
        gateIsValid(config.firstPlaceGate());
        gateIsValid(config.secondPlaceGate());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    private void gateIsValid(ColossalColosseumConfig.Gate gate) {
        Preconditions.checkArgument(gate != null, "gate can't be null");
        
        Preconditions.checkArgument(gate.clearArea() != null, "clearArea can't be null");
        BoundingBox clearArea = gate.clearArea().getBoundingBox();
        Preconditions.checkArgument(clearArea.getWidthX() >= 1, "clearArea x-width must be >= 1");
        Preconditions.checkArgument(clearArea.getHeight() >= 1, "clearArea height must be >= 1");
        Preconditions.checkArgument(clearArea.getWidthZ() >= 1, "clearArea z-width must be >= 1");
        
        Preconditions.checkArgument(gate.placeArea() != null, "placeArea can't be null");
        BoundingBox placeArea = gate.placeArea().getBoundingBox();
        Preconditions.checkArgument(placeArea.getWidthX() >= 1, "placeArea x-width must be >= 1");
        Preconditions.checkArgument(placeArea.getHeight() >= 1, "placeArea height must be >= 1");
        Preconditions.checkArgument(placeArea.getWidthZ() >= 1, "placeArea z-width must be >= 1");
        
        Preconditions.checkArgument(gate.stone() != null, "stone can't be null");
        BoundingBox stone = gate.stone().getBoundingBox();
        Preconditions.checkArgument(stone.getWidthX() >= 1, "stone x-width must be >= 1");
        Preconditions.checkArgument(stone.getHeight() >= 1, "stone height must be >= 1");
        Preconditions.checkArgument(stone.getWidthZ() >= 1, "stone z-width must be >= 1");
    }

    @Override
    protected void setConfig(ColossalColosseumConfig config) throws IllegalArgumentException {
        World newWorld = Bukkit.getWorld(config.world());
        Location newFirstPlaceSpawn = config.firstPlaceSpawn().toLocation(newWorld);
        Location newSecondPlaceSpawn = config.secondPlaceSpawn().toLocation(newWorld);
        Location newSpectatorSpawn = config.spectatorSpawn().toLocation(newWorld);
        //now that we know it's all valid
        this.world = newWorld;
        this.firstPlaceSpawn = newFirstPlaceSpawn;
        this.secondPlaceSpawn = newSecondPlaceSpawn;
        this.spectatorSpawn = newSpectatorSpawn;
        this.colossalColosseumConfig = config;
    }

    @Override
    protected InputStream getExampleResourceStream() {
        return ColossalColosseumStorageUtil.class.getResourceAsStream("exampleColossalColosseumConfig.json");
    }

    public World getWorld() {
        return world;
    }

    public Location getFirstPlaceSpawn() {
        return firstPlaceSpawn;
    }

    public Location getSecondPlaceSpawn() {
        return secondPlaceSpawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }
    
    public int getRequiredWins() {
        return colossalColosseumConfig.requiredWins();
    }
}
