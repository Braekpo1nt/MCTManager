package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public record CheckPoint(double yValue, BoundingBox boundingBox, Location respawn) {
}
