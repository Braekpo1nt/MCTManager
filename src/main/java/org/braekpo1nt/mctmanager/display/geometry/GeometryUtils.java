package org.braekpo1nt.mctmanager.display.geometry;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtils {
    
    /**
     * @param box a bounding box to create the edges for
     * @return a list of the 12 edges along the box
     */
    public static List<Edge> toEdges(BoundingBox box) {
        Vector min = box.getMin();
        Vector max = box.getMax();
        double minX = min.getX();
        double minY = min.getY();
        double minZ = min.getZ();
        double maxX = max.getX();
        double maxY = max.getY();
        double maxZ = max.getZ();
        
        List<Edge> edges = new ArrayList<>();
        
        Vector a = new Vector(minX, minY, minZ);
        Vector b = new Vector(minX, minY, maxZ);
        Vector c = new Vector(maxX, minY, minZ);
        Vector d = new Vector(maxX, minY, maxZ);
        Vector e = new Vector(minX, maxY, minZ);
        Vector f = new Vector(minX, maxY, maxZ);
        Vector g = new Vector(maxX, maxY, minZ);
        Vector h = new Vector(maxX, maxY, maxZ);
        
        // Bottom edges
        edges.add(new Edge(a, b));
        edges.add(new Edge(b, d));
        edges.add(new Edge(d, c));
        edges.add(new Edge(c, a));
        // Top edges
        edges.add(new Edge(e, f));
        edges.add(new Edge(f, h));
        edges.add(new Edge(h, g));
        edges.add(new Edge(g, e));
        // Vertical edges
        edges.add(new Edge(a, e));
        edges.add(new Edge(b, f));
        edges.add(new Edge(d, h));
        edges.add(new Edge(c, g));
        
        return edges;
    }
    
    /**
     * @param box the box to convert to points
     * @param n the number of points along each edge (not the total number of points)
     * @return n*12 points which represent the edges of the given box. Points will be equidistant along each edge.
     */
    public static List<Vector> toPoints(BoundingBox box, int n) {
        List<Edge> edges = GeometryUtils.toEdges(box);
        List<Vector> points = new ArrayList<>(n*12);
        for (Edge edge : edges) {
            points.addAll(edge.pointsAlongEdge(n));
        }
        return points;
    }
    
    private GeometryUtils() {
        // do not instantiate
    }
}
