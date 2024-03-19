package org.braekpo1nt.mctmanager.display.geometry.rectangle;

import org.bukkit.util.Vector;

public class XYRectangle implements Rectangle {
    private final Vector min;
    private final Vector max;
    public XYRectangle(double x1, double y1, double z1, double x2, double y2, double z2) {
        if (x1 - x2 != 0.0
                && y1-y2 != 0.0
                && z1 - z2 != 0.0) {
            throw new IllegalArgumentException("(%s, %s, %s) and (%s, %s, %s) are identical or are not along one of the 3 cartesian planes.");
        }
        this.min = new Vector(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.min(z1, z2)
        );
        this.max = new Vector(
                Math.max(x1, x2),
                Math.max(y1, y2),
                Math.max(z1, z2)
        );
    }
}
