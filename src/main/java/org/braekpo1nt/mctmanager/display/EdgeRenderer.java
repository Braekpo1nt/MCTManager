package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import lombok.Getter;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplaySingleton;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
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
     */
    private float strokeWidth;
    private @NotNull Edge edge;
    @Getter
    private @NotNull Location location;
    private boolean centered;
    
    /**
     * @param world the world this should be in
     * @param edge the edge to display
     * @param strokeWidth the stroke width (thickness) in blocks of the line
     * @param centered whether the line should have its local bottom (XZ) face centered on {@link Edge#getA()}.
     *                 It's computationally faster not to, but less pretty. Defaults to true; 
     * @param blockData the block data to use for the line
     */
    @Builder
    public EdgeRenderer(
            @NotNull World world, 
            @NotNull Edge edge, 
            @Nullable Float strokeWidth, 
            @Nullable Boolean centered,
            @Nullable Display.Brightness brightness,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        Objects.requireNonNull(world, "world can't be null");
        Objects.requireNonNull(edge, "edge can't be null");
        this.edge = edge;
        this.strokeWidth = (strokeWidth != null) ? strokeWidth : 0.05f;
        this.location = edge.getA().toLocation(world);
        this.centered = (centered != null) ? centered : true;
        this.renderer = BlockDisplayEntityRenderer.builder()
                .location(location)
                .transformation(createTransformation())
                .blockData(blockData)
                .brightness(brightness)
                .glowing(glowing)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build(); 
    }
    
    @Override
    public @NotNull BlockDisplayDelegate getRenderer() {
        return renderer;
    }
    
    private @NotNull Transformation createTransformation() {
        Vector direction = edge.getB().clone().subtract(edge.getA());
        float length = (float) direction.length();
        Vector unitDirection = direction.clone().normalize();
        
        Vector3f scale = new Vector3f(strokeWidth, length, strokeWidth); // thin, long
        
        Vector3f defaultDirection = new Vector3f(0, 1, 0);
        Vector3f targetDirection = new Vector3f(
                (float) unitDirection.getX(), 
                (float) unitDirection.getY(), 
                (float) unitDirection.getZ()
        );
        Quaternionf rotation = new Quaternionf().rotateTo(defaultDirection, targetDirection);
        
        Vector3f localOffset;
        if (centered) {
            // compute the offset in local (rotated) frame
            localOffset = new Vector3f(-strokeWidth / 2f, 0, -strokeWidth / 2f);
            rotation.transform(localOffset);  // apply rotation to offset
        } else {
            localOffset = new Vector3f();
        }
        
        return new Transformation(
                localOffset, // no translation
                rotation, // left rotation
                scale, // long and thin
                new Quaternionf() // no right rotation
        );
    }
    
    public void setCentered(boolean centered) {
        this.centered = centered;
        renderer.setTransformation(createTransformation());
    }
    
    public void setEdge(@NotNull Edge edge) {
        this.edge = edge;
        this.location = edge.getA().toLocation(location.getWorld());
        renderer.setLocation(location);
        renderer.setTransformation(createTransformation());
    }
    
    public void setStrokeWidth(float strokeWidth) {
        if (this.strokeWidth == strokeWidth) {
            return;
        }
        this.strokeWidth = strokeWidth;
        renderer.setTransformation(createTransformation());
    }
}
