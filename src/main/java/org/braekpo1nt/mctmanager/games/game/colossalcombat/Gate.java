package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

@Data
@Builder
public class Gate {
    private Location spawn;
    private BoundingBox clearArea;
    private BoundingBox placeArea;
    private BoundingBox stone;
    private BoundingBox antiSuffocationArea;
    private BoundingBox flagGoal;
}
