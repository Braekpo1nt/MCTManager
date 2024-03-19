package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Rectangle {
    
    /**
     * Returns the axis-aligned rectangle defined by the two points. To define a non-axis-aligned rectangle, see {@link Rectangle#of(double, double, double, double, double, double, double, double, double)}
     * @param x1 x-coord of pos 1
     * @param y1 y-coord of pos 1
     * @param z1 z-coord of pos 1
     * @param x2 x-coord of pos 2
     * @param y2 y-coord of pos 2
     * @param z2 z-coord of pos 2
     * @return the Axis-Aligned Rectangle represented by the two points.
     * @throws IllegalArgumentException if both points are identical, or if the two points are not aligned on exactly one of the cartesian planes.
     */
    static Rectangle of(double x1, double y1, double z1, double x2, double y2, double z2) {
        if (x1 == x2
                && y1 == y2
                && z1 == z2) {
            throw new IllegalArgumentException("(%s, %s, %s) and (%s, %s, %s) are identical or are not along one of the 3 cartesian planes.");
        }
        if (x1 == x2) {
            return new YZRectangle(y1, z1, y2, z2, x1);
        }
        
        if (y1 == y2) {
            return new XZRectangle(x1, z1, x2, z2, y1);
        }
        
        if (z1 == z2) {
            return new XYRectangle(x1, y1, x2, y2, z1);
        }
        
        throw new IllegalArgumentException("(%s, %s, %s) and (%s, %s, %s) do not define a rectangle on one of the 3 cartesian planes");
    }
    
    static Rectangle of(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @NotNull List<@NotNull Vector> toPoints(double distance);
}
