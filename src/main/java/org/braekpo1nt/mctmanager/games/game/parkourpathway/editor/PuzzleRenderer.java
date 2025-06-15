package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import org.braekpo1nt.mctmanager.display.BoxRenderer;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PuzzleRenderer implements Renderer {
    
    private final List<BoxRenderer> inBounds;
    private final List<CheckpointRenderer> checkpoints;
    
    public PuzzleRenderer(@NotNull World world, @NotNull Puzzle puzzle) {
        this.inBounds = BoxRenderer.of(world, puzzle.getInBounds(), Material.GLASS.createBlockData());
        this.checkpoints = CheckpointRenderer.of(world, puzzle.getCheckPoints(), Material.GLASS, Material.GREEN_WOOL);
    }
    
    @Override
    public @NotNull Location getLocation() {
        return inBounds.getFirst().getLocation();
    }
    
    @Override
    public void show() {
        inBounds.forEach(BoxRenderer::show);
        checkpoints.forEach(CheckpointRenderer::show);
    }
    
    @Override
    public void hide() {
        inBounds.forEach(BoxRenderer::hide);
        checkpoints.forEach(CheckpointRenderer::hide);
    }
    
    public void setInBounds(int index, @NotNull BoundingBox boundingBox) {
        inBounds.get(index).setBoundingBox(boundingBox);
    }
    
    public void setHighlight(int inBoundsIndex, int checkpointIndex, boolean highlight) {
        setHighlightInBounds(inBoundsIndex, highlight);
        setHighlightCheckpoint(checkpointIndex, highlight);
    }
    
    public void setHighlightInBounds(int index, boolean highlight) {
        inBounds.get(index).setGlowing(highlight);
    }
    
    public void setHighlightCheckpoint(int index, boolean highlight) {
        checkpoints.get(index).setGlowing(highlight);
    }
    
    public void addInBound(BoundingBox boundingBox) {
        BoxRenderer newInBound = new BoxRenderer(
                getLocation().getWorld(),
                boundingBox,
                Material.GLASS
        );
        newInBound.show();
        this.inBounds.add(
                newInBound
        );
    }
    
    public void removeInBound(int index) {
        BoxRenderer removed = this.inBounds.remove(index);
        removed.hide();
    }
}
