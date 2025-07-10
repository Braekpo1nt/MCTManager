package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.RectangleRenderer;
import org.braekpo1nt.mctmanager.display.delegates.DisplayComposite;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockData;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockDataComposite;
import org.braekpo1nt.mctmanager.display.geometry.rectangle.Rectangle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Displays the faces of a BoundingBox using a series of faces, rather than a single block
 */
public class RectBoxRenderer implements BoundingBoxRenderer, HasBlockDataComposite, DisplayComposite {
    
    @Getter
    private @NotNull Location location;
    @Getter
    private @NotNull BoundingBox boundingBox;
    private final List<RectangleRenderer> rectRenderers;
    
    @Builder
    public RectBoxRenderer(
            @NotNull World world,
            @NotNull BoundingBox boundingBox,
            @Nullable Display.Brightness brightness,
            @Nullable Component customName,
            boolean customNameVisible,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        Objects.requireNonNull(world, "world can't be null");
        Objects.requireNonNull(boundingBox, "boundingBox can't be null");
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(world);
        this.boundingBox = boundingBox.clone();
        List<Rectangle> rectangles = Rectangle.toRectangles(boundingBox);
        rectRenderers = rectangles.stream()
                .map(rect -> RectangleRenderer.builder()
                        .world(world)
                        .rectangle(rect)
                        .blockData(blockData)
                        .brightness(brightness)
                        .customName(customName)
                        .customNameVisible(customNameVisible)
                        .glowing(glowing)
                        .glowColor(glowColor)
                        .interpolationDuration(interpolationDuration)
                        .teleportDuration(teleportDuration)
                        .build())
                .toList();
    }
    
    @Override
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(location.getWorld());
        this.boundingBox = boundingBox.clone();
        List<Rectangle> rectangles = Rectangle.toRectangles(boundingBox);
        for (int i = 0; i < rectRenderers.size(); i++) {
            RectangleRenderer rectangleRenderer = rectRenderers.get(i);
            Rectangle rectangle = rectangles.get(i);
            rectangleRenderer.setRectangle(rectangle);
        }
    }
    
    @Override
    public @NotNull Collection<? extends DisplayDelegate> getDisplays() {
        return rectRenderers;
    }
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return rectRenderers.getFirst();
    }
    
    @Override
    public @NotNull Collection<? extends HasBlockData> getHasBlockDatas() {
        return rectRenderers;
    }
    
    @Override
    public @NotNull HasBlockData getHasBlockData() {
        return rectRenderers.getFirst();
    }
}
