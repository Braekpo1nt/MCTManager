package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import org.braekpo1nt.mctmanager.display.BoxRenderer;
import org.braekpo1nt.mctmanager.display.LocationRenderer;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckpointRenderer implements Renderer {
    
    public static List<CheckpointRenderer> of(
            @NotNull World world, 
            @NotNull List<CheckPoint> checkPoints, 
            @NotNull BlockData detectionAreaBlock, 
            @NotNull BlockData respawnBlock) {
        return checkPoints.stream()
                .map(checkPoint -> new CheckpointRenderer(world, checkPoint, detectionAreaBlock, respawnBlock))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static List<CheckpointRenderer> of(
            @NotNull World world,
            @NotNull List<CheckPoint> checkPoints,
            @NotNull Material detectionAreaBlock,
            @NotNull Material respawnBlock) {
        return of(world, checkPoints, detectionAreaBlock.createBlockData(), respawnBlock.createBlockData());
    }
    
    private final BoxRenderer detectionArea;
    private final LocationRenderer respawn;
    
    public CheckpointRenderer(@NotNull World world, @NotNull CheckPoint checkPoint, @NotNull BlockData detectionAreaBlock, @NotNull BlockData respawnBlock) {
        this.detectionArea = new BoxRenderer(world, checkPoint.getDetectionArea(), detectionAreaBlock);
        this.respawn = new LocationRenderer(checkPoint.getRespawn(), respawnBlock);
    }
    
    @Override
    public @NotNull Location getLocation() {
        return respawn.getLocation();
    }
    
    @Override
    public void show() {
        detectionArea.show();
        respawn.show();
    }
    
    @Override
    public void hide() {
        detectionArea.hide();
        respawn.hide();
    }
    
    public void setGlowing(boolean glowing) {
        detectionArea.setGlowing(glowing);
        respawn.setGlowing(glowing);
    }
    
    public void setDetectionArea(@NotNull BoundingBox boundingBox) {
        detectionArea.setBoundingBox(boundingBox);
    }
    
    public void setRespawn(@NotNull Location location) {
        respawn.setLocation(location);
    }
}
