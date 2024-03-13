package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
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

public class ColossalCombatStorageUtil extends GameConfigStorageUtil<ColossalCombatConfig> {
    
    private ColossalCombatConfig colossalCombatConfig;
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
    private BoundingBox removeArea;
    private BoundingBox firstPlaceSupport;
    private BoundingBox secondPlaceSupport;
    private BoundingBox firstPlaceAntiSuffocationArea;
    private BoundingBox secondPlaceAntiSuffocationArea;
    private Component description;
    
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public ColossalCombatStorageUtil(File configDirectory) {
        super(configDirectory, "colossalCombatConfig.json", ColossalCombatConfig.class);
    }
    
    @Override
    protected ColossalCombatConfig getConfig() {
        return colossalCombatConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable ColossalCombatConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
    
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        BoundingBox spectatorArea = config.spectatorArea().toBoundingBox();
        Preconditions.checkArgument(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        
        Preconditions.checkArgument(config.firstPlaceSpawn() != null, "firstPlaceSpawn can't be null");
        Preconditions.checkArgument(config.secondPlaceSpawn() != null, "secondPlaceSpawn can't be null");
        Preconditions.checkArgument(config.spectatorSpawn() != null, "spectatorSpawn can't be null");
        Preconditions.checkArgument(config.requiredWins() > 0, "requiredWins must be greater than 0");
        
        Preconditions.checkArgument(config.firstPlaceGate() != null, "firstPlaceGate can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().clearArea() != null, "firstPlaceGate.clearArea can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().placeArea() != null, "firstPlaceGate.placeArea can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().stone() != null, "firstPlaceGate.stone can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().antiSuffocationArea() != null, "firstPlaceGate.antiSuffocationArea can't be null");
        Preconditions.checkArgument(config.firstPlaceGate().antiSuffocationArea().toBoundingBox().getVolume() != 0.0, "firstPlaceGate.antiSuffocationArea volume can't be 0.0");
    
        Preconditions.checkArgument(config.secondPlaceGate() != null, "secondPlaceGate can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().clearArea() != null, "secondPlaceGate.clearArea can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().placeArea() != null, "secondPlaceGate.placeArea can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().stone() != null, "secondPlaceGate.stone can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().antiSuffocationArea() != null, "secondPlaceGate.antiSuffocationArea can't be null");
        Preconditions.checkArgument(config.secondPlaceGate().antiSuffocationArea().toBoundingBox().getVolume() != 0.0, "secondPlaceGate.antiSuffocationArea volume can't be 0.0");
        
        Preconditions.checkArgument(config.removeArea() != null, "removeArea can't be null");
        BoundingBox removeArea = config.removeArea().toBoundingBox();
        Preconditions.checkArgument(removeArea.getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", removeArea, removeArea.getVolume());
        
        Preconditions.checkArgument(config.firstPlaceSupport() != null, "firstPlaceSupport can't be null");
        BoundingBox firstPlaceSupport = config.firstPlaceSupport().toBoundingBox();
        Preconditions.checkArgument(firstPlaceSupport.getVolume() > 0, "firstPlaceSupport volume (%s) must be greater than 0", firstPlaceSupport.getVolume());
        Preconditions.checkArgument(config.secondPlaceSupport() != null, "secondPlaceSupport can't be null");
        BoundingBox secondPlaceSupport = config.secondPlaceSupport().toBoundingBox();
        Preconditions.checkArgument(secondPlaceSupport.getVolume() > 0, "secondPlaceSupport volume (%s) must be greater than 0", secondPlaceSupport.getVolume());
        Preconditions.checkArgument(!firstPlaceSupport.overlaps(secondPlaceSupport), "firstPlaceSupport and secondPlaceSupport can't overlap");
    
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().roundStarting() >= 1, "durations.roundStarting must be at least 1");
        Preconditions.checkArgument(config.durations().antiSuffocation() >= 0, "durations.antiSuffocation can't be negative");
        
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    @Override
    protected void setConfig(ColossalCombatConfig config) throws IllegalArgumentException {
        World newWorld = Bukkit.getWorld(config.world());
        Location newFirstPlaceSpawn = config.firstPlaceSpawn().toLocation(newWorld);
        Location newSecondPlaceSpawn = config.secondPlaceSpawn().toLocation(newWorld);
        Location newSpectatorSpawn = config.spectatorSpawn().toLocation(newWorld);
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        //now that we know it's all valid
        this.world = newWorld;
        this.firstPlaceSpawn = newFirstPlaceSpawn;
        this.secondPlaceSpawn = newSecondPlaceSpawn;
        this.spectatorSpawn = newSpectatorSpawn;
        this.firstPlaceClearArea = config.firstPlaceGate().clearArea().toBoundingBox();
        this.firstPlacePlaceArea = config.firstPlaceGate().placeArea().toBoundingBox();
        this.firstPlaceStone = config.firstPlaceGate().stone().toBoundingBox();
        this.firstPlaceAntiSuffocationArea = config.firstPlaceGate().antiSuffocationArea().toBoundingBox();
        this.secondPlaceClearArea = config.secondPlaceGate().clearArea().toBoundingBox();
        this.secondPlacePlaceArea = config.secondPlaceGate().placeArea().toBoundingBox();
        this.secondPlaceStone = config.secondPlaceGate().stone().toBoundingBox();
        this.secondPlaceAntiSuffocationArea = config.secondPlaceGate().antiSuffocationArea().toBoundingBox();
        this.removeArea = config.removeArea().toBoundingBox();
        this.firstPlaceSupport = config.firstPlaceSupport().toBoundingBox();
        this.secondPlaceSupport = config.secondPlaceSupport().toBoundingBox();
        this.description = newDescription;
        this.colossalCombatConfig = config;
    }

    @Override
    protected InputStream getExampleResourceStream() {
        return ColossalCombatStorageUtil.class.getResourceAsStream("exampleColossalCombatConfig.json");
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
        return colossalCombatConfig.requiredWins();
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
    
    public BoundingBox getFirstPlaceAntiSuffocationArea() {
        return firstPlaceAntiSuffocationArea;
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
    
    public BoundingBox getSecondPlaceAntiSuffocationArea() {
        return secondPlaceAntiSuffocationArea;
    }
    
    public long getAntiSuffocationDuration() {
        return colossalCombatConfig.durations().antiSuffocation();
    }
    
    public int getRoundStartingDuration() {
        return colossalCombatConfig.durations().roundStarting();
    }
    
    public BoundingBox getRemoveArea() {
        return removeArea;
    }
    
    public BoundingBox getFirstPlaceSupport() {
        return firstPlaceSupport;
    }
    
    public BoundingBox getSecondPlaceSupport() {
        return secondPlaceSupport;
    }
    
    public Component getDescription() {
        return description;
    }
}
