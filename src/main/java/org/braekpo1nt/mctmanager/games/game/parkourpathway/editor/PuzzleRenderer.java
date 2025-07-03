package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import org.braekpo1nt.mctmanager.display.boundingbox.RectBoxRenderer;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PuzzleRenderer implements Renderer {
    
    private final List<RectBoxRenderer> inBounds;
    private final List<CheckpointRenderer> checkpoints;
    
    public PuzzleRenderer(@NotNull World world, @NotNull Puzzle puzzle) {
        this.inBounds = RectBoxRenderer.of(world, puzzle.getInBounds(), Material.RED_STAINED_GLASS);
        this.checkpoints = CheckpointRenderer.of(world, puzzle.getCheckPoints(), Material.BLUE_STAINED_GLASS, Material.GREEN_WOOL);
    }
    
    @Override
    public @NotNull Location getLocation() {
        return inBounds.getFirst().getLocation();
    }
    
    @Override
    public void show() {
        inBounds.forEach(RectBoxRenderer::show);
        checkpoints.forEach(CheckpointRenderer::show);
    }
    
    @Override
    public void hide() {
        inBounds.forEach(RectBoxRenderer::hide);
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
        inBounds.get(index).setGlowing(highlight);
    }
    
    public void setHighlightCheckpoint(int index, boolean highlight) {
        if (index >= checkpoints.size()) {
            return;
        }
        checkpoints.get(index).setGlowing(highlight);
    }
    
    public void addInBound(BoundingBox boundingBox) {
        RectBoxRenderer newInBound = RectBoxRenderer.builder()
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
        RectBoxRenderer removed = this.inBounds.remove(index);
        removed.hide();
    }
    
    public void addCheckPoint(CheckPoint checkPoint) {
        CheckpointRenderer newCheckPoint = new CheckpointRenderer(
                getLocation().getWorld(), 
                checkPoint,
                Material.BLUE_STAINED_GLASS.createBlockData(),
                Material.GREEN_WOOL.createBlockData()
        );
        newCheckPoint.show();
        this.checkpoints.add(newCheckPoint);
    }
    
    public void removeCheckpoint(int index) {
        CheckpointRenderer removed = this.checkpoints.remove(index);
        removed.hide();
    }
}
