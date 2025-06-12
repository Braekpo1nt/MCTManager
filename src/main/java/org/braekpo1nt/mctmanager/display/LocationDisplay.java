package org.braekpo1nt.mctmanager.display;

import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * For displaying {@link org.bukkit.Location}s
 */
public class LocationDisplay implements Display {
    
    private final @NotNull BlockDisplayEntityRenderer renderer;
    
    public LocationDisplay(@NotNull Location location, @NotNull Material material) {
        this.renderer = new BlockDisplayEntityRenderer(
                location,
                material.createBlockData()
        );
    }
    
    @Override
    public void show() {
        renderer.show();
    }
    
    @Override
    public void hide() {
        renderer.hide();
    }
}
