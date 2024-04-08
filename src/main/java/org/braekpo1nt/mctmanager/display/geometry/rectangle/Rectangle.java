package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import com.google.common.base.Preconditions;
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
    
    /**
     * Turn the given BoundingBox into rectangles. If the box is 2-dimensional in all 3 axis (i.e. min and max corners are equal) throws {@link IllegalArgumentException}. Otherwise, if the box is 2 dimensional on 1 or 2 of the axes, will return the appropriate number of rectangles
     * @param box the BoundingBox to convert to rectangles
     * @return between 1 and 6 rectangles representing the faces of the given BoundingBox
     * @throws IllegalArgumentException if the min and max corners of the box are equal
     */
    static List<Rectangle> toRectangles(BoundingBox box) {
        List<Rectangle> rects = new ArrayList<>(6);
        Vector min = box.getMin();
        Vector max = box.getMax();
        Preconditions.checkArgument(!min.equals(max), "the min and max corners of the box can't be equal");
        double minX = min.getX();
        double minY = min.getY();
        double minZ = min.getZ();
        double maxX = max.getX();
        double maxY = max.getY();
        double maxZ = max.getZ();
        // XY
        rects.add(new XYRectangle(minX, minY, maxX, maxY, minZ)); // 3 NORTH
        rects.add(new XYRectangle(minX, minY, maxX, maxY, maxZ)); // 4 SOUTH
        // XZ
        rects.add(new XZRectangle(minX, minZ, maxX, maxZ, minY)); // 1 BOTTOM
        rects.add(new XZRectangle(minX, minZ, maxX, maxZ, maxY)); // 6 TOP
        // YZ
        rects.add(new YZRectangle(minY, minZ, maxY, maxZ, minX)); // 2 WEST
        rects.add(new YZRectangle(minY, minZ, maxY, maxZ, maxX)); // 5 EAST
        
        return rects;
    }
    
    @NotNull List<@NotNull Vector> toPoints(double distance);
}
