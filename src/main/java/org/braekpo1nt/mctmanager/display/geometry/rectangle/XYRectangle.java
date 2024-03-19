package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import com.google.common.base.Preconditions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class XYRectangle implements Rectangle {
    private final double minX;
    private final double minY;
    private final double z;
    private final double xLength;
    private final double yLength;
    public XYRectangle(double x1, double y1, double x2, double y2, double z) {
        Preconditions.checkArgument(x1 != x2, "x-values can't be identical");
        Preconditions.checkArgument(y1 != y2, "y-values can't be identical");
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.z = z;
        this.xLength = Math.max(x1, x2) - minX;
        this.yLength = Math.max(y1, y2) - minY;
    }
    
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
}
