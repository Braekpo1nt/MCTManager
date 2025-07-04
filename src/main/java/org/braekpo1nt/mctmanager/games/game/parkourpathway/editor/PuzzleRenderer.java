package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PuzzleRenderer implements Renderer {
    
    private final List<BoundingBoxRendererImpl> inBounds;
    private final List<CheckpointRenderer> checkpoints;
    public static final @NotNull Material IN_BOUND_BLOCK = Material.LIGHT_GRAY_STAINED_GLASS;
    public static final @NotNull Material DETECTION_AREA_BLOCK = Material.LIGHT_BLUE_STAINED_GLASS;
    public static final @NotNull Material RESPAWN_BLOCK = Material.LIME_WOOL;
    
    public static final @NotNull Material IN_BOUND_BLOCK_HIGHLIGHTED = Material.RED_STAINED_GLASS;
    public static final @NotNull Material DETECTION_AREA_BLOCK_HIGHLIGHTED = Material.BLUE_STAINED_GLASS;
    public static final @NotNull Material RESPAWN_BLOCK_HIGHLIGHTED = Material.GREEN_WOOL;
    
    public PuzzleRenderer(@NotNull World world, @NotNull Puzzle puzzle, @Nullable BoundingBoxRendererImpl.Type inBoundType, @Nullable BoundingBoxRendererImpl.Type checkpointType) {
        BlockData inBoundBlockData = IN_BOUND_BLOCK.createBlockData();
        this.inBounds = puzzle.getInBounds().stream()
                .map(boundingBox -> BoundingBoxRendererImpl.builder()
                        .world(world)
                        .boundingBox(boundingBox)
                        .type(inBoundType)
                        .blockData(inBoundBlockData)
                        .interpolationDuration(1)
                        .teleportDuration(1)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        BlockData detectionAreaBlockData = DETECTION_AREA_BLOCK.createBlockData();
        BlockData respawnBlockData = RESPAWN_BLOCK.createBlockData();
        this.checkpoints = puzzle.getCheckPoints().stream()
                .map(checkPoint -> CheckpointRenderer.builder()
                        .world(world)
                        .checkPoint(checkPoint)
                        .detectionAreaBlock(detectionAreaBlockData)
                        .respawnBlock(respawnBlockData)
                        .type(checkpointType)
                        .glowColor(Color.BLUE)
                        .interpolationDuration(1)
                        .teleportDuration(1)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    @Override
    public @NotNull Location getLocation() {
        return inBounds.getFirst().getLocation();
    }
    
    @Override
    public void show() {
        inBounds.forEach(BoundingBoxRenderer::show);
        checkpoints.forEach(CheckpointRenderer::show);
    }
    
    @Override
    public void hide() {
        inBounds.forEach(BoundingBoxRenderer::hide);
        checkpoints.forEach(CheckpointRenderer::hide);
    }
    
    public void setInBounds(int index, @NotNull BoundingBox boundingBox) {
        inBounds.get(index).setBoundingBox(boundingBox);
    }
    
    public void setCheckPointDetectionArea(int checkpointIndex, @NotNull BoundingBox detectionArea) {
        checkpoints.get(checkpointIndex).setDetectionArea(detectionArea);
    }
    
    public void setCheckPointRespawn(int checkpointIndex, @NotNull Location respawn) {
        checkpoints.get(checkpointIndex).setRespawn(respawn);
    }
    
    public void setHighlight(int inBoundsIndex, int checkpointIndex, boolean highlight) {
        setHighlightInBounds(inBoundsIndex, highlight);
        setHighlightCheckpoint(checkpointIndex, highlight);
    }
    
    public void setHighlightInBounds(int index, boolean highlight) {
        if (index >= inBounds.size()) {
            return;
        }
        BoundingBoxRenderer inBound = inBounds.get(index);
        inBound.setGlowing(highlight);
        inBound.setMaterial(highlight ? IN_BOUND_BLOCK_HIGHLIGHTED : IN_BOUND_BLOCK);
    }
    
    public void setHighlightCheckpoint(int index, boolean highlight) {
        if (index >= checkpoints.size()) {
            return;
        }
        CheckpointRenderer checkpoint = checkpoints.get(index);
        checkpoint.setGlowing(highlight);
        checkpoint.setDetectionAreaBlock(highlight ? DETECTION_AREA_BLOCK_HIGHLIGHTED.createBlockData() : DETECTION_AREA_BLOCK.createBlockData());
        checkpoint.setRespawnBlock(highlight ? RESPAWN_BLOCK_HIGHLIGHTED.createBlockData() : RESPAWN_BLOCK.createBlockData());
    }
    
    public void addInBound(BoundingBox boundingBox) {
        BoundingBoxRendererImpl newInBound = BoundingBoxRendererImpl.builder()
                .world(getLocation().getWorld())
                .boundingBox(boundingBox)
                .blockData(Material.GLASS.createBlockData())
                .build();
        newInBound.show();
        this.inBounds.add(
                newInBound
        );
    }
    
    public void removeInBound(int index) {
        BoundingBoxRenderer removed = this.inBounds.remove(index);
        removed.hide();
    }
    
    public void addCheckPoint(CheckPoint checkPoint) {
        CheckpointRenderer newCheckPoint = CheckpointRenderer.builder()
                .world(getLocation().getWorld())
                .checkPoint(checkPoint)
                .detectionAreaBlock(DETECTION_AREA_BLOCK.createBlockData())
                .respawnBlock(RESPAWN_BLOCK.createBlockData())
                .glowColor(Color.BLUE)
                .build();
        newCheckPoint.show();
        this.checkpoints.add(newCheckPoint);
    }
    
    public void removeCheckpoint(int index) {
        CheckpointRenderer removed = this.checkpoints.remove(index);
        removed.hide();
    }
    
    public void setInBoundsType(@NotNull BoundingBoxRendererImpl.Type type) {
        inBounds.forEach(inBound -> inBound.setType(type));
    }
    
    public void setCheckpointType(@NotNull BoundingBoxRendererImpl.Type type) {
        checkpoints.forEach(checkpoint -> checkpoint.setDetectionAreaType(type));
    }
}
