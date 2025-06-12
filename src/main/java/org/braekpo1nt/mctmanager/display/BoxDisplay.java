package org.braekpo1nt.mctmanager.display;

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

public class BoxDisplay implements Display {
    
    private final @NotNull World world;
    private final @NotNull BlockDisplayEntityRenderer normal;
    private final @NotNull BlockDisplayEntityRenderer inverted;
    
    
    public BoxDisplay(@NotNull World world, @NotNull BoundingBox boundingBox, @NotNull Material material) {
        this.world = world;
        Vector origin = boundingBox.getMin();
        Location location = origin.toLocation(world);
        Location invertedLocation = invertOrigin(origin, boundingBox.getWidthX()).toLocation(world);
        Transformation transformation = boundingBoxToTransformation(boundingBox);
        Transformation invertedTransformation = boundingBoxToTransformationInverted(boundingBox);
        BlockData blockData = material.createBlockData();
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
        Location location = origin.toLocation(world);
        Location invertedLocation = invertOrigin(origin, boundingBox.getWidthX()).toLocation(world);
        Transformation transformation = boundingBoxToTransformation(boundingBox);
        Transformation invertedTransformation = boundingBoxToTransformationInverted(boundingBox);
        normal.setLocation(location);
        inverted.setLocation(invertedLocation);
        normal.setTransformation(transformation);
        inverted.setTransformation(invertedTransformation);
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
