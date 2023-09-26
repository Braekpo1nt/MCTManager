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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class ColossalColosseumStorageUtil extends GameConfigStorageUtil<ColossalColosseumConfig> {
    
    private ColossalColosseumConfig colossalColosseumConfig;
    private World world;
    private Location firstPlaceSpawn;
    private Location secondPlaceSpawn;
    private Location spectatorSpawn;
    private BoundingBox firstPlaceClearArea;
    private BoundingBox firstPlacePlaceArea;
    private BoundingBox firstPlaceStone;
    private BoundingBox secondPlaceClearArea;
    private BoundingBox secondPlacePlaceArea;
    private BoundingBox secondPlaceStone;
    
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
        
        Preconditions.checkArgument(config.firstPlaceGate() != null, "firstPlaceGate can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().clearArea() != null, "firstPlaceGate.clearArea can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().placeArea() != null, "firstPlaceGate.placeArea can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().stone() != null, "firstPlaceGate.stone can't be null");
        
        Preconditions.checkArgument(config.secondPlaceGate() != null, "secondPlaceGate can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().clearArea() != null, "secondPlaceGate.clearArea can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().placeArea() != null, "secondPlaceGate.placeArea can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().stone() != null, "secondPlaceGate.stone can't be null");
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
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
        this.firstPlaceClearArea = config.firstPlaceGate().clearArea().toBoundingBox();
        this.firstPlacePlaceArea = config.firstPlaceGate().placeArea().toBoundingBox();
        this.firstPlaceStone = config.firstPlaceGate().stone().toBoundingBox();
        this.secondPlaceClearArea = config.secondPlaceGate().clearArea().toBoundingBox();
        this.secondPlacePlaceArea = config.secondPlaceGate().placeArea().toBoundingBox();
        this.secondPlaceStone = config.secondPlaceGate().stone().toBoundingBox();
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
    
    public BoundingBox getFirstPlaceClearArea() {
        return firstPlaceClearArea;
    }
    
    public BoundingBox getFirstPlacePlaceArea() {
        return firstPlacePlaceArea;
    }
    
    public BoundingBox getFirstPlaceStone() {
        return firstPlaceStone;
    }
    
    public BoundingBox getSecondPlaceClearArea() {
        return secondPlaceClearArea;
    }
    
    public BoundingBox getSecondPlacePlaceArea() {
        return secondPlacePlaceArea;
    }
    
    public BoundingBox getSecondPlaceStone() {
        return secondPlaceStone;
    }
}
