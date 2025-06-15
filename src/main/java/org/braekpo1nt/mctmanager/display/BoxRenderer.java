package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoxRenderer implements Renderer {
    
    public static List<BoxRenderer> of(@NotNull World world, @NotNull List<BoundingBox> boundingBoxes, @NotNull BlockData blockData) {
        return boundingBoxes.stream()
                .map(boundingBox -> new BoxRenderer(world, boundingBox, blockData))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private final @NotNull BlockDisplayEntityRenderer normal;
    private final @NotNull BlockDisplayEntityRenderer inverted;
    @Getter
    private @NotNull Location location;
    
    public BoxRenderer(@NotNull World world, @NotNull BoundingBox boundingBox, @NotNull BlockData blockData) {
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(world);
        Location invertedLocation = invertOrigin(origin, boundingBox.getWidthX()).toLocation(world);
        Transformation transformation = boundingBoxToTransformation(boundingBox);
        Transformation invertedTransformation = boundingBoxToTransformationInverted(boundingBox);
        this.normal = new BlockDisplayEntityRenderer(
                location,
                transformation,
                blockData
        );
        this.inverted = new BlockDisplayEntityRenderer(
                invertedLocation,
                invertedTransformation,
                blockData
        );
    }
    
    public BoxRenderer(@NotNull World world, @NotNull BoundingBox boundingBox, @NotNull Material material) {
        this(world, boundingBox, material.createBlockData());
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
    
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        Vector origin = boundingBox.getMin();
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
    
    public void setGlowing(boolean glowing) {
        normal.setGlowing(glowing);
        inverted.setGlowing(glowing);
    }
    
    @Override
    public void show() {
        normal.show();
        inverted.show();
    }
    
    @Override
    public void hide() {
        normal.hide();
        inverted.hide();
    }
}
