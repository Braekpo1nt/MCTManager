package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Wedge {
    private final BoundingBox boundingBox;
    
    public Wedge(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public boolean contains(Vector position) {
        return boundingBox.contains(position);
    }
}
