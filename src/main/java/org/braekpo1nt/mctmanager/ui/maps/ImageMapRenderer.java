package org.braekpo1nt.mctmanager.ui.maps;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageMapRenderer extends MapRenderer {
    
    private final @NotNull BufferedImage image;
    
    /**
     * Assigns the image as-is. Does not perform any resizing on the passed in image.
     * @param image the image to display on the map. Should be 127x127 pixels.
     * @see org.braekpo1nt.mctmanager.ui.UIUtils#createMapRenderer(File) for how to instantiate
     * a resized image from a file.
     */
    public ImageMapRenderer(@NotNull BufferedImage image) {
        this.image = image;
    }
    
    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        canvas.drawImage(0, 0, image);
    }
}
