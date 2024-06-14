package org.braekpo1nt.mctmanager.geometry;

import lombok.Data;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Data
public class BoundingCylinder implements Geometry {
    private double centerX;
    private double centerZ;
    private double radius;
    
    @Override
    public boolean overlaps(@NotNull Geometry other) {
        if (other instanceof BoundingRectangle) {
            return overlaps((BoundingRectangle) other);
        } else if (other instanceof BoundingCylinder) {
            return overlaps((BoundingCylinder) other);
        }
        return false;
    }
    
    public boolean overlaps(@NotNull BoundingRectangle other) {
        return other.overlaps(this);
    }
    
    public boolean overlaps(@NotNull BoundingCylinder other) {
        double dx = this.centerX - other.centerX;
        double dz = this.centerZ - other.centerZ;
        double distanceSquared = dx * dx + dz * dz;
        double radiusSum = this.radius + other.radius;
        return distanceSquared < (radiusSum * radiusSum);
    }
    
    @Override
    public boolean contains(Vector vector) {
        double distanceX = centerX - vector.getX();
        double distanceZ = centerZ - vector.getZ();
        double distanceSquared = distanceX * distanceX + distanceZ * distanceZ;
        return distanceSquared <= radius * radius;
    }
}

