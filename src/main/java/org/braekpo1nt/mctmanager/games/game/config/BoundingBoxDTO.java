package org.braekpo1nt.mctmanager.games.game.config;

import org.bukkit.util.BoundingBox;

/**
 * An abstraction of the {@link BoundingBox} for gson serialization/deserialization purposes.
 * <p>
 * Because gson uses reflection, users can input a minX that is greater than maxX (in both this and {@link BoundingBox}). This object makes sure that when you access the {@link BoundingBox} this is meant to store, it's a valid box. It will only be calculated once upon the first request ({@link BoundingBoxDTO#toBoundingBox()} and then will return the reference to the bounding box. Note that if you change the attributes of the {@link BoundingBoxDTO#boundingBox}, the properties of this DTO will not be changed. 
 */
public class BoundingBoxDTO {
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;
    transient private BoundingBox boundingBox;
    
    /**
     * Get the {@link BoundingBox} this DTO was storing. If this is the first request, creates the BoundingBox from the min and max values stored in this DTO's fields.
     * @return the BoundingBox this DTO was storing. Modifying the return value will not modify the DTO.
     */
    public BoundingBox toBoundingBox() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
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
