package org.braekpo1nt.mctmanager.display;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class DisplayUtils {
    
    /**
     * 
     * @param plugin the plugin (used for scheduling tasks)
     * @param viewer the player to show the display to
     * @param points the points to display
     * @param duration the duration to display the points for (in ticks)
     */
    public static void display(Main plugin, Player viewer, List<Vector> points, long duration) {
        long period = 1; // number of ticks per period
        long periods = duration / period;
        new BukkitRunnable() {
            long periodsLeft = periods;
            @Override
            public void run() {
                if (periodsLeft <= 0) {
                    this.cancel();
                    return;
                }
                displayPoints(points, viewer, 1, 1.0F);
                periodsLeft--;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, period);
    }
    
    public static void displayPoints(List<Vector> points, Player viewer) {
        DisplayUtils.displayPoints(points, viewer, 1, 1.0F, Color.RED);
    }
    
    public static void displayPoints(List<Vector> points, Player viewer, Color color) {
        DisplayUtils.displayPoints(points, viewer, 1, 1.0F, color);
    }
    
    public static void displayPoints(List<Vector> points, Player viewer, int count, float size) {
        DisplayUtils.displayPoints(points, viewer, count, size, Color.RED);
    }
    
    public static void displayPoints(List<Vector> points, Player viewer, int count, float size, Color color) {
        for (Vector v : points) {
            viewer.spawnParticle(Particle.REDSTONE, v.getX(), v.getY(), v.getZ(), count, new Particle.DustOptions(color, size));
        }
    }
    
    private DisplayUtils() {
        // do not instantiate
    }
}
