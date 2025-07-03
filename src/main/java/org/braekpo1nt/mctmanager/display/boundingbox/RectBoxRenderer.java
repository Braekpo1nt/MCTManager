package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import lombok.Getter;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.braekpo1nt.mctmanager.display.RectangleRenderer;
import org.braekpo1nt.mctmanager.display.geometry.rectangle.Rectangle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Displays the faces of a BoundingBox using a series of faces, rather than a single block
 */
public class RectBoxRenderer implements BoundingBoxRenderer {
    
    public static List<RectBoxRenderer> of(@NotNull World world, @NotNull List<BoundingBox> boundingBoxes, @NotNull BlockData blockData) {
        return boundingBoxes.stream()
                .map(boundingBox -> RectBoxRenderer.builder()
                        .world(world)
                        .boundingBox(boundingBox)
                        .blockData(blockData)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static List<RectBoxRenderer> of(@NotNull World world, @NotNull List<BoundingBox> boundingBoxes, @NotNull Material material) {
        return of(world, boundingBoxes, material.createBlockData());
    }
    
    @Getter
    private @NotNull Location location;
    
    private final List<RectangleRenderer> rectRenderers;
    
    @Builder
    public RectBoxRenderer(@NotNull World world, @NotNull BoundingBox boundingBox, @Nullable Display.Brightness brightness, @NotNull BlockData blockData) {
        Objects.requireNonNull(world, "world can't be null");
        Objects.requireNonNull(boundingBox, "boundingBox can't be null");
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(world);
        List<Rectangle> rectangles = Rectangle.toRectangles(boundingBox);
        rectRenderers = rectangles.stream()
                .map(rect -> RectangleRenderer.builder()
                        .world(world)
                        .rectangle(rect)
                        .blockData(blockData)
                        .brightness(brightness)
                        .build())
                .toList();
    }
    
    @Override
    public @NotNull Collection<? extends BlockDisplayDelegate> getRenderers() {
        return rectRenderers;
    }
    
    @Override
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(location.getWorld());
        List<Rectangle> rectangles = Rectangle.toRectangles(boundingBox);
        for (int i = 0; i < rectRenderers.size(); i++) {
            RectangleRenderer rectangleRenderer = rectRenderers.get(i);
            Rectangle rectangle = rectangles.get(i);
            rectangleRenderer.setRectangle(rectangle);
        }
    }
    
    @Override
    public void setGlowing(boolean glowing) {
        rectRenderers.forEach(rect -> rect.setGlowing(glowing));
    }
    
    @Override
    public void setBrightness(@Nullable Display.Brightness brightness) {
        rectRenderers.forEach(rect -> rect.setBrightness(brightness));
    }
    
    @Override
    public void show() {
        rectRenderers.forEach(RectangleRenderer::show);
    }
    
    @Override
    public void hide() {
        rectRenderers.forEach(RectangleRenderer::hide);
    }
}
