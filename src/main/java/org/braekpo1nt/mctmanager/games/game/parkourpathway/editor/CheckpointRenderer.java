package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import lombok.Builder;
import org.braekpo1nt.mctmanager.display.LocationRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.display.delegates.DisplayComposite;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CheckpointRenderer implements DisplayComposite {
    
    private final BoundingBoxRendererImpl detectionArea;
    private final LocationRenderer respawn;
    
    @Builder
    public CheckpointRenderer(
            @NotNull World world,
            @NotNull CheckPoint checkPoint,
            @Nullable BoundingBoxRendererImpl.Type type,
            @NotNull Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @NotNull BlockData detectionAreaBlock,
            @NotNull BlockData respawnBlock) {
        this.detectionArea = BoundingBoxRendererImpl.builder()
                .world(world)
                .boundingBox(checkPoint.getDetectionArea())
                .type(type)
                .blockData(detectionAreaBlock)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
        this.respawn = LocationRenderer.builder()
                .location(checkPoint.getRespawn())
                .blockData(respawnBlock)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
    }
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return detectionArea;
    }
    
    @Override
    public @NotNull Collection<? extends DisplayDelegate> getDisplays() {
        return List.of(detectionArea, respawn);
    }
    
    @Override
    public @NotNull Location getLocation() {
        return respawn.getLocation();
    }
    
    public void setDetectionArea(@NotNull BoundingBox boundingBox) {
        detectionArea.setBoundingBox(boundingBox);
    }
    
    public void setRespawn(@NotNull Location location) {
        respawn.setLocation(location);
    }
    
    public void setDetectionAreaBlock(@NotNull BlockData blockData) {
        detectionArea.setBlockData(blockData);
    }
    
    public void setRespawnBlock(@NotNull BlockData blockData) {
        respawn.setPositionBlockData(blockData);
    }
    
    public void setDetectionAreaType(BoundingBoxRendererImpl.Type type) {
        detectionArea.setType(type);
    }
}
