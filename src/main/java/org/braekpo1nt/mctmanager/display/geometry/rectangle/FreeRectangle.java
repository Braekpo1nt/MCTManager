package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import com.google.common.base.Preconditions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FreeRectangle implements Rectangle {
    private final Vector origin;
    private final Vector edge1;
    private final Vector edge2;
    
    public FreeRectangle(@NotNull Vector origin, @NotNull Vector edge1, @NotNull Vector edge2) {
        Preconditions.checkArgument(edge1.angle(edge2) == (Math.PI / 2), "edges must form a 90 degree angle");
        this.origin = origin;
        this.edge1 = edge1;
        this.edge2 = edge2;
    }
    
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
    
}
