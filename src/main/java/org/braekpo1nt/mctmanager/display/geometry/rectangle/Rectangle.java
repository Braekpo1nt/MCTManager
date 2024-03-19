package org.braekpo1nt.mctmanager.display.geometry.rectangle;

public interface Rectangle {
    
    static Rectangle of(double x1, double y1, double z1, double x2, double y2, double z2) {
        if (x1 == x2
                && y1 == y2
                && z1 == z2) {
            throw new IllegalArgumentException("(%s, %s, %s) and (%s, %s, %s) are identical or are not along one of the 3 cartesian planes.");
        }
        if (x1 == x2) {
            return new YZRectangle(x1, y1, z1, x2, y2, z2);
        }
        
        if (y1 == y2) {
            return new XZRectangle(x1, y1, z1, x2, y2, z2);
        }
        
        if (z1 == z2) {
            return new XYRectangle(x1, y1, z1, x2, y2, z2);
        }
        
        throw new IllegalArgumentException("(%s, %s, %s) and (%s, %s, %s) do not define a rectangle on one of the 3 cartesian planes");
    }
}
