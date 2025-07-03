package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import lombok.Getter;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplaySingleton;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * A Renderer representing an edge between two points
 */
public class EdgeRenderer implements BlockDisplaySingleton {
    
    private final @NotNull BlockDisplayEntityRenderer renderer;
    /**
     * The stroke width (thickness) of the line
     * TODO: make this mutable (will need to make translation mutable as well
     */
    private final float strokeWidth;
    /**
     * The translation to keep the edge centered on the points. 
     */
    private final @NotNull Vector3f translation;
    @Getter
    private @NotNull Location location;
    
    @Builder
    public EdgeRenderer(@NotNull World world, @NotNull Edge edge, @Nullable Float strokeWidth, @Nullable BlockData blockData) {
        Objects.requireNonNull(world, "world can't be null");
        Objects.requireNonNull(edge, "edge can't be null");
        this.strokeWidth = (strokeWidth != null) ? strokeWidth : 0.05f;
        this.translation = new Vector3f(-this.strokeWidth/2, 0, -this.strokeWidth/2);
        this.location = edge.getA().toLocation(world);
        this.renderer = BlockDisplayEntityRenderer.builder()
                .location(location)
                .transformation(edgeToTransformation(edge))
                .blockData(blockData)
                .build(); 
    }
    
    @Override
    public @NotNull BlockDisplayDelegate getRenderer() {
        return renderer;
    }
    
    private @NotNull Transformation edgeToTransformation(@NotNull Edge e) {
        Vector direction = e.getB().clone().subtract(e.getA());
        float length = (float) direction.length();
        Vector unitDirection = direction.clone().normalize();
        Vector3f scale = new Vector3f(strokeWidth, length, strokeWidth);
        Vector3f defaultDirection = new Vector3f(0, 1, 0);
        Vector3f targetDirection = new Vector3f((float) unitDirection.getX(), (float) unitDirection.getY(), (float) unitDirection.getZ());
        Quaternionf rotationQuaternion = new Quaternionf().rotateTo(defaultDirection, targetDirection);
        return new Transformation(
                translation,
                rotationQuaternion, // left rotation
                scale, // long and thin
                new Quaternionf() // no right rotation
        );
    }
    
    public void setEdge(@NotNull Edge edge) {
        this.location = edge.getA().toLocation(location.getWorld());
        renderer.setLocation(location);
        renderer.setTransformation(edgeToTransformation(edge));
    }
    
}
