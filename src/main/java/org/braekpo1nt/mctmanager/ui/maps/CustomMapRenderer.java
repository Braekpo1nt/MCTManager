package org.braekpo1nt.mctmanager.ui.maps;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

public class CustomMapRenderer extends MapRenderer {
    
    private final @NotNull BufferedImage image;
    
    /**
     * Generate a {@link CustomMapRenderer} using the image at the provided url
     * The image is resized to be 127x127 pixels.
     * @param imageUrl the URL to the desired image
     * @return The {@link CustomMapRenderer} with the given image from the url, or null if it could not be created.
     */
    public static @Nullable CustomMapRenderer fromURL(@NotNull String imageUrl) {
        try {
            URL url = new URI(imageUrl).toURL();
            return resized(ImageIO.read(url));
        } catch (IOException e) {
            Main.logger().log(Level.SEVERE, "Unable to draw image: ", e);
        } catch (URISyntaxException e) {
            Main.logger().log(Level.SEVERE, "Unable to find image link: ", e);
        }
        return null;
    }
    
    /**
     * 
     * @param imageFile the image file
     * @return a {@link CustomMapRenderer} with the given image file displayed, or null if the file was not found or could not be displayed.
     */
    public static @Nullable CustomMapRenderer fromFile(@NotNull File imageFile) {
        // Check if the file exists and is readable
        if (!imageFile.exists()) {
            Main.logger().severe(String.format("Error: File not found at path: %s", imageFile.getAbsolutePath()));
            return null;
        }
        if (!imageFile.canRead()) {
            Main.logger().severe(String.format("Error: Cannot read the file at path: %s", imageFile.getAbsolutePath()));
            return null;
        }
        
        try {
            return resized(ImageIO.read(imageFile));
        } catch (IOException e) {
            Main.logger().log(Level.SEVERE, String.format("Unable to draw image file at path: %s ", imageFile.getAbsolutePath()), e);
        }
        return null;
    }
    
    /**
     * @param image the image to be resized and displayed on the renderer
     * @return a {@link CustomMapRenderer} with the given image resized to 127x127
     */
    public static @NotNull CustomMapRenderer resized(@NotNull BufferedImage image) {
        BufferedImage resizedImage = MapPalette.resizeImage(image);
        return new CustomMapRenderer(resizedImage);
    }
    
    /**
     * Assigns the image as-is. Does not perform any resizing on the passed in image.
     * @param image the image to display on the map. Should be 127x127 pixels. 
     */
    public CustomMapRenderer(@NotNull BufferedImage image) {
        this.image = image;
    }
    
    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        canvas.drawImage(0, 0, image);
    }
}
