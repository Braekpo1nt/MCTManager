package org.braekpo1nt.mctmanager.display.geometry.rectangle;


import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class YZRectangle implements Rectangle {
    
    private final double minY;
    private final double minZ;
    private final double x;
    private final double yLength;
    private final double zLength;
    
    public YZRectangle(double y1, double z1, double y2, double z2, double x) {
//        Preconditions.checkArgument(y1 != y2, "y-values can't be identical");
//        Preconditions.checkArgument(z1 != z2, "z-values can't be identical");
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.x = x;
        this.yLength = Math.max(y1, y2) - minY;
        this.zLength = Math.max(z1, z2) - minZ;
    }
    
    public @NotNull List<@NotNull Vector> toPoints(double distance) {
        List<Vector> points = new ArrayList<>();
        int numPointsY = (int) Math.ceil(yLength / distance);
        int numPointsZ = (int) Math.ceil(zLength / distance);
        double stepY = yLength / numPointsY;
        double stepZ = zLength / numPointsZ;
        
        for (int i = 0; i <= numPointsY; i++) {
            for (int j = 0; j <= numPointsZ; j++) {
                double y = minY + i * stepY;
                double z = minZ + j * stepZ;
                points.add(new Vector(x, y, z));
            }
        }
        
        return points;
    }
    
}
