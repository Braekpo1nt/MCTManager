package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class Wedge {
    private final BoundingBox boundingBox;
    
    public Wedge(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public boolean isInside(Player participant) {
        Location playerLocation = participant.getLocation();
        return boundingBox.contains(playerLocation.toVector());
    }
}
