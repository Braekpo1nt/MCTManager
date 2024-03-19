package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface Rectangle {
    
    /**
     * See {@link Rectangle#of(double, double, double, double, double, double)} 
     * @param a the first corner of an axis-aligned rectangle
     * @param b the second corner of an axis-aligned rectangle
     */
    static Rectangle of(Vector a, Vector b) {
        return Rectangle.of(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
    }
    
    /**
     * Returns the axis-aligned rectangle defined by the two points. To define a non-axis-aligned rectangle, see {@link Rectangle#of(double, double, double, double, double, double, double, double, double)}
     * @param x1 x-coord of the first corner of an axis-aligned rectangle
     * @param y1 y-coord of the first corner of an axis-aligned rectangle
     * @param z1 z-coord of the first corner of an axis-aligned rectangle
     * @param x2 x-coord of the second corner of an axis-aligned rectangle
     * @param y2 y-coord of the second corner of an axis-aligned rectangle
     * @param z2 z-coord of the second corner of an axis-aligned rectangle
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
    
    static List<Rectangle> of(BoundingBox box) {
        List<Rectangle> rects = new ArrayList<>(6);
        Vector min = box.getMin();
        Vector max = box.getMax();
        // XY
        rects.add(new XYRectangle(min.getX(), min.getY(), max.getX(), max.getY(), min.getZ())); // 3
        rects.add(new XYRectangle(min.getX(), min.getY(), max.getX(), max.getY(), max.getZ())); // 4
        // XZ
        rects.add(new XZRectangle(min.getX(), min.getZ(), max.getX(), max.getZ(), min.getY())); // 1
        rects.add(new XZRectangle(min.getX(), min.getZ(), max.getX(), max.getZ(), max.getY())); // 6
        // YZ
        rects.add(new YZRectangle(min.getY(), min.getZ(), max.getY(), max.getZ(), min.getX())); // 2
        rects.add(new YZRectangle(min.getY(), min.getZ(), max.getY(), max.getZ(), max.getX())); // 5
        
        return rects;
    }
    
    @NotNull List<@NotNull Vector> toPoints(double distance);
}
