package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import lombok.Getter;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayComposite;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

/**
 * A Renderer to display a {@link org.bukkit.Location}
 */
public class LocationRenderer implements BlockDisplayComposite {
    
    /**
     * The default length (in blocks) of the direction vector edge renderer
     */
    private final double directionLength;
    @Getter
    private @NotNull Location location;
    private final @NotNull BlockDisplayEntityRenderer positionRenderer;
    private final @NotNull EdgeRenderer directionRenderer;
    
    /**
     * @param location the location to display
     * @param scale the scale factor of the square block representing the location (square will be centered on the location)
     * @param directionLength the length of the line indicating the direction (yaw and pitch) of the location
     * @param directionStrokeWidth the stroke width of the line indicating the direction (yaw and pitch) of the location
     * @param blockData the BlockData used for the block representing the location
     * @param directionBlockData the BlockData used for the line indicating the direction (yaw and pitch) of the location
     */
    @Builder
    public LocationRenderer(
            @NotNull Location location,
            @Nullable Float scale,
            @Nullable Double directionLength,
            @Nullable Float directionStrokeWidth,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData,
            @Nullable BlockData directionBlockData) {
        this.location = location;
        float scaleFactor = (scale != null) ? scale : 0.6f;
        this.directionLength = (directionLength != null) ? directionLength : 1.2;
        this.positionRenderer = BlockDisplayEntityRenderer.builder()
                .location(location)
                .blockData(blockData)
                .glowing(glowing)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .transformation(
                        new Transformation(
                                new Vector3f(scaleFactor / -2f),
                                new Quaternionf(),
                                new Vector3f(scaleFactor),
                                new Quaternionf()
                        )
                )
                .build();
        this.directionRenderer = EdgeRenderer.builder()
                .world(location.getWorld())
                .edge(Edge.from(location, this.directionLength))
                .strokeWidth((directionStrokeWidth != null) ? directionStrokeWidth : 0.15f)
                .glowing(glowing)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .blockData((directionBlockData != null) ? directionBlockData : Material.BLUE_WOOL.createBlockData())
                .build();
    }
    
    /**
     * @param scaleFactor the scale factor of the square block representing the location (square will be centered on the location)
     */
    public void setScale(float scaleFactor) {
        this.positionRenderer.setTransformation(
                new Transformation(
                        new Vector3f(scaleFactor / -2f),
                        new Quaternionf(),
                        new Vector3f(scaleFactor),
                        new Quaternionf()
                )
        );
    }
    
    /**
     * @param blockData the BlockData used for the block representing the location
     */
    public void setPositionBlockData(@NotNull BlockData blockData) {
        positionRenderer.setBlockData(blockData);
    }
    
    /**
     * @param blockData the BlockData used for the line indicating the direction of the location
     */
    public void setDirectionBlockData(@NotNull BlockData blockData) {
        directionRenderer.setBlockData(blockData);
    }
    
    @Override
    public @NotNull Collection<? extends BlockDisplayDelegate> getRenderers() {
        return List.of(positionRenderer, directionRenderer);
    }
    
    @Override
    public @NotNull BlockDisplayDelegate getPrimaryRenderer() {
        return positionRenderer;
    }
    
    public void setLocation(@NotNull Location location) {
        this.location = location;
        this.positionRenderer.setLocation(location);
        this.directionRenderer.setEdge(Edge.from(location, directionLength));
    }
}
