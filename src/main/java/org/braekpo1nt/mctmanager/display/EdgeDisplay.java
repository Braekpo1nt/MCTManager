package org.braekpo1nt.mctmanager.display;

import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EdgeDisplay implements Display {
    
    private @NotNull Edge edge;
    private @NotNull Color glowColor;
    private @Nullable BlockDisplay blockDisplay;
    
    private boolean showing;
    
    public EdgeDisplay(@NotNull Edge edge, @NotNull Color glowColor, @NotNull Material material) {
        this.edge = edge;
        this.glowColor = glowColor;
        this.showing = false;
    }
    
    public EdgeDisplay(@NotNull Edge edge, @NotNull Color glowColor) {
        this(edge, glowColor, Material.RED_WOOL);
    }
    
    @Override
    public void show(@NotNull World world) {
        if (showing) {
            return;
        }
        showing = true;
        
    }
    
    @Override
    public void hide() {
        if (!showing) {
            return;
        }
        showing = false;
    }
}
