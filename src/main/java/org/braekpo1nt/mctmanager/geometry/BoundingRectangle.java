package org.braekpo1nt.mctmanager.geometry;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class BoundingRectangle extends org.bukkit.util.BoundingBox implements Geometry {
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
        return this.overlaps(other.getMinX(), other.getMinY(), other.getMinZ(), other.getMaxX(), other.getMaxY(), other.getMaxZ());
    }
    
    private boolean overlaps(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.getMinX() < maxX && this.getMaxX() > minX
                && this.getMinY() < maxY && this.getMaxY() > minY
                && this.getMinZ() < maxZ && this.getMaxZ() > minZ;
    }
    
    public boolean overlaps(@NotNull BoundingCylinder other) {
        double closestX = clamp(other.getCenterX(), this.getMinX(), this.getMaxX());
        double closestZ = clamp(other.getCenterZ(), this.getMinZ(), this.getMaxZ());
        double distanceX = other.getCenterX() - closestX;
        double distanceZ = other.getCenterZ() - closestZ;
        return (distanceX * distanceX + distanceZ * distanceZ) < (other.getRadius() * other.getRadius());
    }
    
    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
    
    @Override
    public boolean contains(Vector vector) {
        return this.contains(vector.getX(), vector.getY(), vector.getZ());
    }
}