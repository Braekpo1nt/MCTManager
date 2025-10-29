package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import org.braekpo1nt.mctmanager.display.RectangleRenderer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FreeRectangle implements Rectangle {
    private final Vector origin;
    private final Vector edge1;
    private final Vector edge2;
    
    /**
     * Constructs a freeform rectangle in 3D space using an origin point and two edge vectors.
     * <p>
     * The rectangle is defined by:
     * <ul>
     *     <li>{@code origin} — the corner (base point) of the rectangle</li>
     *     <li>{@code edge1} — one adjacent edge vector extending from the origin</li>
     *     <li>{@code edge2} — the second adjacent edge vector extending from the origin</li>
     * </ul>
     * The resulting rectangle spans the area defined by the origin and the parallelogram formed by {@code edge1} and {@code edge2}.
     * The two edge vectors must not be colinear; otherwise, the rectangle would have zero area.
     * @param origin the base point of the rectangle (one of its corners)
     * @param edge1 the first edge vector extending from the origin
     * @param edge2 the second edge vector extending from the origin
     * @throws IllegalArgumentException if {@code edge1} and {@code edge2} are collinear or nearly parallel (zero area)
     */
    public FreeRectangle(@NotNull Vector origin, @NotNull Vector edge1, @NotNull Vector edge2) {
        if (edge1.clone().crossProduct(edge2).lengthSquared() < 1e-6) {
            throw new IllegalArgumentException("Edges must not be collinear (zero area)");
        }
        this.origin = origin;
        this.edge1 = edge1;
        this.edge2 = edge2;
    }
    
    @Override
    public @NotNull List<@NotNull Vector> toPoints(double distance) {
        List<Vector> points = new ArrayList<>();
        
        int numPointsEdge1 = (int) Math.ceil(edge1.length() / distance);
        int numPointsEdge2 = (int) Math.ceil(edge2.length() / distance);
        
        // Calculate the unit vectors along each edge
        Vector unitEdge1 = edge1.normalize();
        Vector unitEdge2 = edge2.normalize();
        
        // Calculate the spacing between points
        double spacing = 1.0;
        
        // Generate evenly spaced points in a grid along the face of the rectangle
        for (int i = 0; i < numPointsEdge1; i++) {
            Vector step1 = unitEdge1.multiply(i * spacing);
            for (int j = 0; j < numPointsEdge2; j++) {
                // Calculate the position of the current point
                Vector step2 = unitEdge2.multiply(j * spacing);
                Vector position = origin.add(step1).add(step2);
                points.add(position);
            }
        }
        
        return points;
    }
    
    @Override
    public @NotNull Transformation toTransformation() {
        float length1 = (float) edge1.length();
        float length2 = (float) edge2.length();
        float thickness = 0.001f;
        Vector normalDouble = edge1.crossProduct(edge2).normalize();
        Vector3f normal = new Vector3f(
                (float) normalDouble.getX(),
                (float) normalDouble.getY(),
                (float) normalDouble.getZ()
        );
        // Rotate from default up to our rectangle's normal
        Quaternionf rotation = new Quaternionf().rotationTo(RectangleRenderer.DEFAULT_UP, normal);
        
        return new Transformation(
                new Vector3f(),            // translation
                rotation,
                new Vector3f(length1, thickness, length2),    // scale along edge1, normal thickness, edge2
                new Quaternionf()      // right rotation = identity
        );
    }
    
    @Override
    public @NotNull Location getOrigin(@NotNull World world) {
        return origin.toLocation(world);
    }
    
    @Override
    public @NotNull Location getCenter(@NotNull World world) {
        return origin.clone()
                .add(edge1.clone().multiply(0.5))
                .add(edge2.clone().multiply(0.5))
                .toLocation(world);
    }
}
