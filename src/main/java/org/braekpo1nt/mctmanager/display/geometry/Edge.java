package org.braekpo1nt.mctmanager.display.geometry;

import org.braekpo1nt.mctmanager.commands.MCTDebugCommand;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
     * @return a list of points that are equidistant along the edge
     */
    public static List<Vector> pointsAlongEdgeWithDistance(Edge edge, double distance) {
        List<Vector> points = new ArrayList<>();
        Vector a = edge.getA();
        Vector b = edge.getB();
        
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
    
    @Override
    public String toString() {
        return "[" +
                a +
                ", " +
                b +
                "]";
    }
}
