package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import lombok.Getter;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplaySingleton;
import org.braekpo1nt.mctmanager.display.geometry.rectangle.Rectangle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * A Renderer representing a {@link Rectangle}. Uses the {@link Rectangle#toTransformation()} method
 * to transform a {@link BlockDisplayEntityRenderer} into a flat surface aligned to the given rectangle. 
 */
public class RectangleRenderer implements Renderer, BlockDisplaySingleton {
    
    // BlockDisplay defaults to a cube aligned Y-up
    public static final Vector3f DEFAULT_UP = new Vector3f(0, 1, 0);
    public static final float THICKNESS = 0.001F;
    @Getter
    private @NotNull Location location;
    private final @NotNull BlockDisplayEntityRenderer renderer;
    
    @Builder
    public RectangleRenderer(@NotNull World world, @NotNull Rectangle rectangle, @Nullable Display.Brightness brightness, @NotNull BlockData blockData) {
        Objects.requireNonNull(world, "world can't be null");
        Objects.requireNonNull(rectangle, "rectangle can't be null");
        this.location = rectangle.getOrigin(world);
        Transformation transformation = rectangle.toTransformation();
        this.renderer = BlockDisplayEntityRenderer.builder()
                .location(this.location)
                .transformation(transformation)
                .blockData(blockData)
                .brightness(brightness)
                .build();
    }
    
    @Override
    public @NotNull BlockDisplayDelegate getRenderer() {
        return renderer;
    }
    
    public void setRectangle(@NotNull Vector origin, @NotNull Vector edge1, @NotNull Vector edge2) {
        location = origin.toLocation(location.getWorld());
        Rectangle rectangle = Rectangle.of(origin, edge1, edge2);
        Transformation transformation = rectangle.toTransformation();
        renderer.setLocation(location);
        renderer.setTransformation(transformation);
    }
    
    public void setRectangle(@NotNull Rectangle rectangle) {
        location = rectangle.getOrigin(location.getWorld());
        Transformation transformation = rectangle.toTransformation();
        renderer.setLocation(location);
        renderer.setTransformation(transformation);
    }
}
