package org.braekpo1nt.mctmanager.display.geometry;

import org.braekpo1nt.mctmanager.display.geometry.rectangle.Rectangle;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtils {
    
    public static List<Vector> toRectanglePoints(BoundingBox box, double distance) {
        List<Rectangle> rects = Rectangle.toRectangles(box);
        List<Vector> points = new ArrayList<>();
        for (Rectangle rect : rects) {
            points.addAll(rect.toPoints(distance));
        }
        return points;
    }
    
    /**
     * @param box the box to convert to points
     * @param distance the distance between points
     * @return a list of equidistant points (using the given distance) along the edges of the box
     */
    public static List<Vector> toEdgePoints(BoundingBox box, double distance) {
        List<Edge> edges = Edge.toEdges(box);
        List<Vector> points = new ArrayList<>();
        for (Edge edge : edges) {
            points.addAll(edge.pointsAlongEdgeWithDistance(distance));
        }
        return points;
    }
    
    /**
     * Returns points along the faces of the cube, where each point is a block apart
     * @param box the bounding box
     * @return the points representing the faces of the cube
     */
    public static List<Vector> toFacePoints(BoundingBox box) {
        List<Vector> points = new ArrayList<>();
        int minX = box.getMin().getBlockX();
        int minY = box.getMin().getBlockY();
        int minZ = box.getMin().getBlockZ();
        int maxX = box.getMax().getBlockX();
        int maxY = box.getMax().getBlockY();
        int maxZ = box.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX
                        || y == minY || y == maxY
                        || z == minZ || z == maxZ) {
                        points.add(new Vector(x, y, z));
                    }
                }
            }
        }
        return points;
    }
    
    /**
     * Returns points along the faces of the cube at the given axial distance between points
     * @param box the bounding box
     * @param distance the axial distance between points (x y and z axis distance)
     * @return the points representing the faces of the box
     */
    public static List<Vector> toFacePoints(BoundingBox box, double distance) {
        List<Vector> points = new ArrayList<>();
        int numPointsX = (int) Math.ceil(box.getWidthX() / distance);
        int numPointsY = (int) Math.ceil(box.getHeight() / distance);
        int numPointsZ = (int) Math.ceil(box.getWidthZ() / distance);
        double stepX = box.getWidthX() / numPointsX;
        double stepY = box.getHeight() / numPointsY;
        double stepZ = box.getWidthZ() / numPointsZ;
        for (int i = 0; i <= numPointsX; i++) {
            for (int j = 0; j <= numPointsY; j++) {
                for (int k = 0; k <= numPointsZ; k++) {
                    if (i == 0 || i == numPointsX 
                        || j == 0 || j == numPointsY 
                        || k == 0 || k == numPointsZ) {
                        double x = box.getMinX() + i * stepX;
                        double y = box.getMinY() + i * stepY;
                        double z = box.getMinZ() + i * stepZ;
                        points.add(new Vector(x, y, z));
                    }
                }
            }
        }
        return points;
    }
    
    
    
    private GeometryUtils() {
        // do not instantiate
    }
}
