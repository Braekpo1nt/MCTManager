package org.braekpo1nt.mctmanager.display.geometry;

import com.google.common.base.Objects;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Edge {
    private final Vector a;
    private final Vector b;
    
    public Edge(Vector a, Vector b) {
        this.a = a;
        this.b = b;
    }
    
    public Edge(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.a = new Vector(x1, y1, z1);
        this.b = new Vector(x2, y2, z2);
    }
    
    public Edge(Vector a, double x2, double y2, double z2) {
        this.a = a;
        this.b = new Vector(x2, y2, z2);
    }
    
    public Edge(double x1, double y1, double z1, Vector b) {
        this.a = new Vector(x1, y1, z1);
        this.b = b;
    }
    
    /**
     * @param location the location to make the edge from
     * @param length the length (in blocks) to make the edge
     * @return an {@link Edge} from the location to the point along the vector of length {@code length} 
     * in the direction of the pitch and yaw of the location  
     */
    public static Edge from(Location location, double length) {
        Vector direction = EntityUtils.getDirection(location).multiply(length);
        return new Edge(location.toVector(), location.toVector().add(direction));
    }
    
    public Vector getA() {
        return a;
    }
    
    public Vector getB() {
        return b;
    }
    
    /**
     * @param n the number of points to get
     * @return a list of n points along the edge, equally spaced
     */
    public List<Vector> pointsAlongEdge(int n) {
        return Edge.pointsAlongEdge(this, n);
    }
    
    /**
     * @param distance the distance between points (should not be longer than the length of the edge)
     * @return a list of points that are equidistant along the edge
     */
    public List<Vector> pointsAlongEdgeWithDistance(double distance) {
        return Edge.pointsAlongEdgeWithDistance(this, distance);
    }
    
    /**
     * @param p a percentage (from [0,1] inclusive)
     * @return a Vector which is p percent along the edge.
     * 0 gives the first endpoint, 1 gives the second endpoint
     */
    public Vector interpolate(double p) {
        return Edge.interpolate(this, p);
    }
    
    /**
     * @param edge the edge to get the points along
     * @param n the number of points to get
     * @return a list of n points along the edge, equally spaced
     */
    public static List<Vector> pointsAlongEdge(Edge edge, int n) {
        List<Vector> points = new ArrayList<>();
        Vector a = edge.getA();
        Vector b = edge.getB();
        
        double deltaX = (b.getX() - a.getX()) / (n - 1);
        double deltaY = (b.getY() - a.getY()) / (n - 1);
        double deltaZ = (b.getZ() - a.getZ()) / (n - 1);
        
        for (int i = 0; i < n; i++) {
            double x = a.getX() + i * deltaX;
            double y = a.getY() + i * deltaY;
            double z = a.getZ() + i * deltaZ;
            points.add(new Vector(x, y, z));
        }
        
        return points;
    }
    
    /**
     * @param edge the edge to get the points along
     * @param distance the distance between points (should not be longer than the length of the edge)
     * @return a list of points that are equidistant along the edge. Ensures that both endpoints of the edge are included (one at the beginning of the list and one at the end), even if the last two points are less than the provided distance apart from each other.
     */
    public static List<Vector> pointsAlongEdgeWithDistance(Edge edge, double distance) {
        List<Vector> points = new ArrayList<>();
        Vector a = edge.getA();
        Vector b = edge.getB();
        
        if (a.equals(b)) {
            return Collections.singletonList(a);
        }
        
        double length = Math.sqrt(Math.pow(b.getX() - a.getX(), 2) +
                Math.pow(b.getY() - a.getY(), 2) +
                Math.pow(b.getZ() - a.getZ(), 2));
        
        int numPoints = (int) Math.ceil(length / distance);
        
        double deltaX = (b.getX() - a.getX()) / length * distance;
        double deltaY = (b.getY() - a.getY()) / length * distance;
        double deltaZ = (b.getZ() - a.getZ()) / length * distance;
        
        double x = a.getX();
        double y = a.getY();
        double z = a.getZ();
        
        for (int i = 0; i < numPoints; i++) {
            points.add(new Vector(x, y, z));
            x += deltaX;
            y += deltaY;
            z += deltaZ;
        }
        
        if (!points.get(points.size() - 1).equals(b)) {
            points.add(b);
        }
        
        return points;
    }
    
    /**
     * @param edge the edge to interpolate along
     * @param p a percentage (from [0,1] inclusive)
     * @return a Vector which is p percent along the edge.
     * 0 gives the first endpoint, 1 gives the second endpoint
     */
    public static Vector interpolate(Edge edge, double p) {
        if (p == 0) {
            return edge.getA();
        } else if (p == 1) {
            return edge.getB();
        }
        Vector a = edge.getA();
        Vector b = edge.getB();
        
        double x = a.getX() + (b.getX() - a.getX()) * p;
        double y = a.getY() + (b.getY() - a.getY()) * p;
        double z = a.getZ() + (b.getZ() - a.getZ()) * p;
        
        return new Vector(x, y, z);
    }
    
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
        
        // vertices of the rectangular prism
        Vector a = new Vector(minX, minY, minZ);
        Vector b = new Vector(minX, minY, maxZ);
        Vector c = new Vector(maxX, minY, minZ);
        Vector d = new Vector(maxX, minY, maxZ);
        Vector e = new Vector(minX, maxY, minZ);
        Vector f = new Vector(minX, maxY, maxZ);
        Vector g = new Vector(maxX, maxY, minZ);
        Vector h = new Vector(maxX, maxY, maxZ);
        
        List<Edge> edges = new ArrayList<>(12);
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
    
    @Override
    public String toString() {
        return "[" +
                a +
                ", " +
                b +
                "]";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge edge)) return false;
        return Objects.equal(a, edge.a) && Objects.equal(b, edge.b);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(a, b);
    }
}
