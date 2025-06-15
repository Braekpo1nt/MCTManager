package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * For displaying {@link org.bukkit.Location}s
 */
public class LocationRenderer implements Renderer {
    
    @Getter
    private @NotNull Location location;
    private final @NotNull BlockDisplayEntityRenderer renderer;
    
    public LocationRenderer(@NotNull Location location, @NotNull BlockData blockData) {
        this.location = location;
        this.renderer = new BlockDisplayEntityRenderer(
                location,
                blockData
        );
    }
    
    public LocationRenderer(@NotNull Location location, @NotNull Material material) {
        this(location, material.createBlockData());
    }
    
    public void setLocation(@NotNull Location location) {
        this.location = location;
        this.renderer.setLocation(location);
    }
    
    @Override
    public void show() {
        renderer.show();
    }
    
    @Override
    public void hide() {
        renderer.hide();
    }
    
    public void setGlowing(boolean glowing) {
        renderer.setGlowing(glowing);
    }
}
