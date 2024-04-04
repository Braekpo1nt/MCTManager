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
    
    private static final int MAX_TICKS = 60*8*20;
    private final @NotNull Main plugin;
    private final int maxTicks;
    private @NotNull List<Vector> points;
    private @NotNull Color color;
    private int taskId;
    private final @NotNull List<@NotNull Display> children = new ArrayList<>();
    
    public Display(@NotNull Main plugin) {
        this.plugin = plugin;
        this.points = Collections.emptyList();
        this.color = Color.RED;
        this.maxTicks = MAX_TICKS;
    }
    
    public Display(@NotNull Main plugin, @NotNull List<@NotNull Vector> points, @NotNull Color color, int maxTicks) {
        this.plugin = plugin;
        this.points = points;
        this.color = color;
        this.maxTicks = maxTicks;
    }
    
    public Display(@NotNull Main plugin, @NotNull List<@NotNull Vector> points) {
        this.plugin = plugin;
        this.points = points;
        this.color = Color.RED;
        this.maxTicks = MAX_TICKS;
    }
    
    public Display(@NotNull Main plugin, @NotNull List<@NotNull Vector> points, @NotNull Color color) {
        this.plugin = plugin;
        this.points = points;
        this.color = color;
        this.maxTicks = MAX_TICKS;
    }
    
    public Display(@NotNull Main plugin, @NotNull List<@NotNull Vector> points, int maxTicks) {
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
    
    public void show(@NotNull Player viewer) {
        show(viewer, maxTicks);
    }
    
    public void show(@NotNull Player viewer, int ticks) {
        hide();
        if (!points.isEmpty()) {
            taskId = new BukkitRunnable() {
                int count = ticks;
                @Override
                public void run() {
                    if (count <= 0) {
                        this.cancel();
                        return;
                    }
                    displayPoints(viewer);
                    count--;
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L).getTaskId();
        }
        for (Display child : children) {
            child.show(viewer);
        }
    }
    
    /**
     * Used internally to display this Display's points and all children's points for 1 frame
     * @param viewer the player to show the Display to
     */
    private void displayPoints(Player viewer) {
        DisplayUtils.displayPoints(points, viewer, color);
        for (Display child : children) {
            child.displayPoints(viewer);
        }
    }
    
    public void hide() {
        Bukkit.getScheduler().cancelTask(taskId);
        for (Display child : children) {
            child.hide();
        }
    }
}
