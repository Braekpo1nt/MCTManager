package org.braekpo1nt.mctmanager.games.parkourpathway;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class CheckPoint {
    private final int yValue;
    private final BoundingBox boundingBox;
    private final Location respawn;

    public CheckPoint(int yValue, BoundingBox boundingBox, Location respawn) {
        this.yValue = yValue;
        this.boundingBox = boundingBox;
        this.respawn = respawn;
    }


    public Location getRespawn() {
        return respawn;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int getyValue() {
        return yValue;
    }
}
