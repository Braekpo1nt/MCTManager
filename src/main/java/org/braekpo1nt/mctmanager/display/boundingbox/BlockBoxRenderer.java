package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.BlockDisplayEntityRenderer;
import org.braekpo1nt.mctmanager.display.delegates.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Renders a BoundingBox using a single block, scaled to the size of the BoundingBox.
 * Includes an inverted block of the same size and location so that it can be viewed from the inside.
 */
public class BlockBoxRenderer implements BoundingBoxRenderer, DisplayComposite, HasBlockDataComposite {
    
    private final @NotNull BlockDisplayEntityRenderer normal;
    private final @NotNull BlockDisplayEntityRenderer inverted;
    @Getter
    private @NotNull BoundingBox boundingBox;
    @Getter
    private @NotNull Location location;
    
    @Builder
    public BlockBoxRenderer(
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
        Location invertedLocation = invertOrigin(origin, boundingBox.getWidthX()).toLocation(world);
        Transformation transformation = boundingBoxToTransformation(boundingBox);
        Transformation invertedTransformation = boundingBoxToTransformationInverted(boundingBox);
        this.normal = BlockDisplayEntityRenderer.builder()
                .location(location)
                .transformation(transformation)
                .blockData(blockData)
                .brightness(brightness)
                .customName(customName)
                .customNameVisible(customNameVisible)
                .glowing(glowing)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
        this.inverted = BlockDisplayEntityRenderer.builder()
                .location(invertedLocation)
                .transformation(invertedTransformation)
                .blockData(blockData)
                .brightness(brightness)
                .glowing(glowing)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
    }
    
    private @NotNull Transformation boundingBoxToTransformation(@NotNull BoundingBox box) {
        return new Transformation(
                new Vector3f(), // no translation
                new AxisAngle4f(), // no left rotation
                new Vector3f(
                        (float) box.getWidthX(),
                        (float) box.getHeight(),
                        (float) box.getWidthZ()),
                new AxisAngle4f() // no right rotation
        );
    }
    
    private @NotNull Transformation boundingBoxToTransformationInverted(@NotNull BoundingBox box) {
        return new Transformation(
                new Vector3f(), // no translation
                new AxisAngle4f(), // no left rotation
                new Vector3f(
                        (float) -box.getWidthX(), // inverted on the x-axis
                        (float) box.getHeight(),
                        (float) box.getWidthZ()),
                new AxisAngle4f() // no right rotation
        );
    }
    
    private @NotNull Vector invertOrigin(@NotNull Vector vector, double offset) {
        return vector.clone().add(new Vector(offset, 0, 0));
    }
    
    @Override
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        Vector origin = boundingBox.getMin();
        this.boundingBox = boundingBox.clone();
        this.location = origin.toLocation(location.getWorld());
        Location invertedLocation = invertOrigin(origin, boundingBox.getWidthX())
                .toLocation(location.getWorld());
        Transformation transformation = boundingBoxToTransformation(boundingBox);
        Transformation invertedTransformation = boundingBoxToTransformationInverted(boundingBox);
        normal.setLocation(location);
        inverted.setLocation(invertedLocation);
        normal.setTransformation(transformation);
        inverted.setTransformation(invertedTransformation);
    }
    
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return normal;
    }
    
    @Override
    public @NotNull HasBlockData getHasBlockData() {
        return normal;
    }
    
    @Override
    public @NotNull Collection<? extends DisplayDelegate> getDisplays() {
        return List.of(normal, inverted);
    }
    
    @Override
    public @NotNull Collection<? extends HasBlockData> getHasBlockDatas() {
        return List.of(normal, inverted);
    }
}
