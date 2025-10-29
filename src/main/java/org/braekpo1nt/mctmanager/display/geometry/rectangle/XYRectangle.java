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

public class XYRectangle implements Rectangle {
    private final double minX;
    private final double minY;
    private final double z;
    private final double xLength;
    private final double yLength;
    
    public XYRectangle(double x1, double y1, double x2, double y2, double z) {
//        Preconditions.checkArgument(x1 != x2, "x-values can't be identical");
//        Preconditions.checkArgument(y1 != y2, "y-values can't be identical");
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.z = z;
        this.xLength = Math.max(x1, x2) - minX;
        this.yLength = Math.max(y1, y2) - minY;
    }
    
    @Override
    public @NotNull List<@NotNull Vector> toPoints(double distance) {
        List<Vector> points = new ArrayList<>();
        int numPointsX = (int) Math.ceil(xLength / distance);
        int numPointsY = (int) Math.ceil(yLength / distance);
        double stepX = xLength / numPointsX;
        double stepY = yLength / numPointsY;
        
        for (int i = 0; i <= numPointsX; i++) {
            for (int j = 0; j <= numPointsY; j++) {
                double x = minX + i * stepX;
                double y = minY + j * stepY;
                points.add(new Vector(x, y, z));
            }
        }
        
        return points;
    }
    
    @Override
    public @NotNull Transformation toTransformation() {
        return new Transformation(
                new Vector3f(), // translation relative to display location
                new Quaternionf(),   // no left rotation
                new Vector3f((float) xLength, (float) yLength, RectangleRenderer.THICKNESS), // XY face, thin Z
                new Quaternionf() // no right rotation
        );
    }
    
    @Override
    public @NotNull Location getOrigin(@NotNull World world) {
        return new Location(world, minX, minY, z);
    }
    
    @Override
    public @NotNull Location getCenter(@NotNull World world) {
        return new Location(world, minX + xLength / 2, minY + yLength / 2, z);
    }
    
    @Override
    public String toString() {
        return String.format("XY (%s, %s, %s) (%s, %s, %s)", minX, minY, z, minX + xLength, minY + yLength, z);
    }
}
