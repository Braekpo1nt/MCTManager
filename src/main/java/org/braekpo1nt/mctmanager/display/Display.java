package org.braekpo1nt.mctmanager.display;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Display {
    
    private final Main plugin;
    private final int maxTicks;
    private @NotNull List<Vector> points;
    private @NotNull Color color;
    private int taskId;
    private final List<Display> children = new ArrayList<>();
    
    public Display(Main plugin) {
        this.plugin = plugin;
        this.points = Collections.emptyList();
        this.color = Color.RED;
        this.maxTicks = 60*20;
    }
    
    public Display(Main plugin, @NotNull List<Vector> points, @NotNull Color color, int maxTicks) {
        this.plugin = plugin;
        this.points = points;
        this.color = color;
        this.maxTicks = maxTicks;
    }
    
    public Display(Main plugin, @NotNull List<Vector> points) {
        this.plugin = plugin;
        this.points = points;
        this.color = Color.RED;
        this.maxTicks = 60*20;
    }
    
    public Display(Main plugin, @NotNull List<Vector> points, @NotNull Color color) {
        this.plugin = plugin;
        this.points = points;
        this.color = color;
        this.maxTicks = 60*20;
    }
    
    public Display(Main plugin, @NotNull List<Vector> points, int maxTicks) {
        this.plugin = plugin;
        this.points = points;
        this.color = Color.RED;
        this.maxTicks = maxTicks;
    }
    
    public void setPoints(@NotNull List<Vector> points) {
        this.points = points;
    }
    
    public void setColor(@NotNull Color color) {
        this.color = color;
    }
    
    public void addChild(@NotNull Display child) {
        children.add(child);
    }
    
    public void show(Player viewer) {
        hide();
        if (!points.isEmpty()) {
            taskId = new BukkitRunnable() {
                int count = maxTicks;
                @Override
                public void run() {
                    if (count <= 0) {
                        this.cancel();
                        return;
                    }
                    DisplayUtils.displayPoints(points, viewer, color);
                    count--;
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L).getTaskId();
        }
        for (Display child : children) {
            child.show(viewer);
        }
    }
    
    public void hide() {
        Bukkit.getScheduler().cancelTask(taskId);
        for (Display child : children) {
            child.hide();
        }
    }
}
