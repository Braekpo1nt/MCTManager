package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

/**
 * Represents an individual arena for Capture the Flag
 * @param northSpawn The spawn location at the north of the arena
 * @param southSpawn The spawn location at the south of the arena
 * @param northFlag The flag spawn/goal location for the north of the arena
 * @param southFlag The flag spawn/goal location for the south of the arena
 * @param northBarrier The origin location for the glass barrier for the north of the arena
 * @param southBarrier The origin location for the glass barrier for the south of the arena
 * @param barrierSize The size of the barrier to be placed
 * @param boundingBox The bounding box of the arena
 */
public record Arena(Location northSpawn, Location southSpawn, Location northFlag, Location southFlag,
                    Location northBarrier, Location southBarrier, BarrierSize barrierSize, BoundingBox boundingBox) {
    /**
     * 
     * @param xSize Must be 1 or more. The size in the x direction.
     * @param ySize Must be 1 or more. The size in the y direction.
     * @param zSize Must be 1 or more. The size in the z direction.
     */
    public record BarrierSize(int xSize, int ySize, int zSize) {
    }
}