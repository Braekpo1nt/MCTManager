package org.braekpo1nt.mctmanager.games.parkourpathway;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public record CheckPoint(int yValue, BoundingBox boundingBox, Location respawn) {
}
