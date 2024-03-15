package org.braekpo1nt.mctmanager.display;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Display {
    
    private final Main plugin;
    private List<Vector> points;
    private Color color;
    private int taskId;
    
    public Display(Main plugin, List<Vector> points, Color color) {
        this.plugin = plugin;
        this.points = points;
        this.color = color;
    }
    
    public void setPoints(List<Vector> points) {
        this.points = points;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void show(Player viewer) {
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                DisplayUtils.displayPoints(points, viewer, color);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L).getTaskId();
    }
    
    public void hide() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
