package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor {
    
    private final Main plugin;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        long ticks = Long.parseLong(args[0]); // 1 
        int duration = Integer.parseInt(args[1]); // in ticks
        int count = Integer.parseInt(args[2]); // 1
        float size = Float.parseFloat(args[3]); // 1.0
        
        float x1 = 1;
        float y1 = 1;
        float z1 = 1;
        float x2 = 10;
        float y2 = 10;
        float z2 = 10;
        if (args.length == 10) {
            x1 = Float.parseFloat(args[4]);
            y1 = Float.parseFloat(args[5]);
            z1 = Float.parseFloat(args[6]);
            x2 = Float.parseFloat(args[7]);
            y2 = Float.parseFloat(args[8]);
            z2 = Float.parseFloat(args[9]);
        }
        
        BoundingBox box = new BoundingBox(
                x1, y1, z1, 
                x2, y2, z2
        );
        List<Edge> edges = createEdges(box);
        List<Vector> points = new ArrayList<>(10*12);
        for (Edge edge : edges) {
            points.addAll(edge.pointsAlongEdgeWithDistance(1.0));
        }
        new BukkitRunnable() {
            int timeLeft = duration;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    this.cancel();
                    return;
                }
                displayPoints(points, player, count, size);
                timeLeft--;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, ticks);
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    public static List<Vector> createHollowCube(BoundingBox area) {
        List<Vector> result = new ArrayList<>();
        int minX = area.getMin().getBlockX();
        int minY = area.getMin().getBlockY();
        int minZ = area.getMin().getBlockZ();
        int maxX = area.getMax().getBlockX();
        int maxY = area.getMax().getBlockY();
        int maxZ = area.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (
                        x == minX || x == maxX
                        || y == minY || y == maxY
                        || z == minZ || z == maxZ
                    ) {
                        result.add(new Vector(x, y, z));
                    }
                }
            }
        }
        return result;
    }
    
    
    static class Edge {
        private final Vector a;
        private final Vector b;
        
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
        
        public Edge(Vector a, Vector b) {
            this.a = a;
            this.b = b;
        }
        
        public Vector getA() {
            return a;
        }
        
        public Vector getB() {
            return b;
        }
        
        public List<Vector> pointsAlongEdge(int n) {
            return Edge.pointsAlongEdge(this, n);
        }
    
        public List<Vector> pointsAlongEdgeWithDistance(double distance) {
            return Edge.pointsAlongEdgeWithDistance(this, distance);
        }
        
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
    
        @Override
        public String toString() {
            return "[" +
                    a +
                    ", " +
                    b +
                    "]";
        }
    }
    
    /**
     * 
     * @param area a bounding box to create the edges for
     * @return a list of 12 edges, where each edge is a list of two 
     */
    public static List<Edge> createEdges(BoundingBox area) {
        Vector min = area.getMin();
        Vector max = area.getMax();
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
    
    private void displayPoints(List<Vector> points, Player viewer, int count, float size) {
        for (Vector v : points) {
            viewer.spawnParticle(Particle.REDSTONE, v.getX(), v.getY(), v.getZ(), count, new Particle.DustOptions(Color.RED, size));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
