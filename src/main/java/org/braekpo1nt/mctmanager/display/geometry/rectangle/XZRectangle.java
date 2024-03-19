package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import com.google.common.base.Preconditions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class XZRectangle implements Rectangle {
    
    private final double minX;
    private final double minZ;
    private final double y;
    private final double xLength;
    private final double zLength;
    
    
    public XZRectangle(double x1, double z1, double x2, double z2, double y) {
        Preconditions.checkArgument(x1 != x2, "x-values can't be identical");
        Preconditions.checkArgument(z1 != z2, "z-values can't be identical");
        this.minX = Math.min(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.y = y;
        this.xLength = Math.max(x1, x2) - minX;
        this.zLength = Math.max(z1, z2) - minZ;
    }
    
    public @NotNull List<@NotNull Vector> toPoints(double distance) {
        List<Vector> points = new ArrayList<>();
        int numPointsX = (int) Math.ceil(xLength / distance);
        int numPointsZ = (int) Math.ceil(zLength / distance);
        double stepX = xLength / numPointsX;
        double stepZ = zLength / numPointsZ;
        
        for (int i = 0; i <= numPointsX; i++) {
            for (int j = 0; j <= numPointsZ; j++) {
                double x = minX + i * stepX;
                double z = minZ + j * stepZ;
                points.add(new Vector(x, y, z));
            }
        }
        
        return points;
    }
    
}
