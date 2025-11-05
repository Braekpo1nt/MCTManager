package org.braekpo1nt.mctmanager.io;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class IOUtils {
    private IOUtils() {
        // do not instantiate
    }
    
    /**
     * @param imageFile the file to convert to a {@link BufferedImage}
     * @return A {@link BufferedImage} containing the file in the image
     * @throws IOException if the file does not exist or can't be read
     */
    public static @NotNull BufferedImage toBufferedImage(@NotNull File imageFile) throws IOException {
        // Check if the file exists and is readable
        if (!imageFile.exists()) {
            throw new IOException(String.format("File not found at path: %s", imageFile.getAbsolutePath()));
        }
        if (!imageFile.canRead()) {
            throw new IOException(String.format("Cannot read the file at path: %s", imageFile.getAbsolutePath()));
        }
        
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new IOException(String.format("File is not an image: %s", imageFile.getAbsolutePath()));
            }
            return image;
        } catch (IOException e) {
            throw new IOException(String.format("Unable to draw image file at path: %s ", imageFile.getAbsolutePath()));
        }
    }
    
}
