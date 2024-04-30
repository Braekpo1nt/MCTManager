package org.braekpo1nt.mctmanager.games.game.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.BoundingBox;

import java.util.List;

/**
 * An abstraction of the {@link BoundingBox} for gson serialization/deserialization purposes.
 * <p>
 * Because gson uses reflection, users can input a minX that is greater than maxX (in both this and {@link BoundingBox}). This object makes sure that when you access the {@link BoundingBox} this is meant to store, it's a valid box.
 */
@AllArgsConstructor
public class BoundingBoxDTO {
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;
    
    public static BoundingBoxDTO from(BoundingBox boundingBox) {
        return new BoundingBoxDTO(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }
    
    public static List<BoundingBoxDTO> from(List<BoundingBox> boundingBoxes) {
        return boundingBoxes.stream().map(BoundingBoxDTO::from).toList();
    }
    
    /**
     * Get the {@link BoundingBox} this DTO was storing. If this is the first request, creates the BoundingBox from the min and max values stored in this DTO's fields.
     * @return the BoundingBox this DTO was storing. Modifying the return value will not modify the DTO.
     */
    public BoundingBox toBoundingBox() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static List<BoundingBox> toBoundingBoxes(List<BoundingBoxDTO> boundingBoxDTOS) {
        return boundingBoxDTOS.stream().map(BoundingBoxDTO::toBoundingBox).toList();
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(BoundingBoxDTO [");
        s.append("minX=");
        s.append(minX);
        s.append(", ");
        s.append("minY=");
        s.append(minY);
        s.append(", ");
        s.append("minZ=");
        s.append(minZ);
        s.append(", ");
        s.append("maxX=");
        s.append(maxX);
        s.append(", ");
        s.append("maxY=");
        s.append(maxY);
        s.append(", ");
        s.append("maxZ=");
        s.append(maxZ);
        s.append("])");
        return s.toString();
    }
}
