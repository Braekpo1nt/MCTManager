package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Data
@Builder
public class Arena {
    private BoundingBox bounds;
    private BoundingBox barn;
    private BoundingBox barnDoor;
    private Location spawn;
    private Location starterChest;
    private Location delivery;
    private BlockFace deliveryBlockFace;
    
    /**
     * Create a new {@link Arena} from this arena using the given offset, where
     * all Locations and BoundingBoxes are translated by the given offset. <br>
     * This operation does not affect this {@link Arena}'s data.
     * @param offset the offset to add to all the coordinates of this {@link Arena}
     * @return a new {@link Arena} offset from the given 
     */
    public Arena offset(Vector offset) {
        return Arena.builder()
                .bounds(new BoundingBox(
                        bounds.getMinX() + offset.getX(), 
                        bounds.getMinY() + offset.getY(), 
                        bounds.getMinZ() + offset.getZ(), 
                        bounds.getMaxX() + offset.getX(), 
                        bounds.getMaxY() + offset.getY(), 
                        bounds.getMaxZ() + offset.getZ()))
                .barn(new BoundingBox(
                        barn.getMinX() + offset.getX(),
                        barn.getMinY() + offset.getY(),
                        barn.getMinZ() + offset.getZ(),
                        barn.getMaxX() + offset.getX(),
                        barn.getMaxY() + offset.getY(),
                        barn.getMaxZ() + offset.getZ()))
                .barnDoor(new BoundingBox(
                        barnDoor.getMinX() + offset.getX(),
                        barnDoor.getMinY() + offset.getY(),
                        barnDoor.getMinZ() + offset.getZ(),
                        barnDoor.getMaxX() + offset.getX(),
                        barnDoor.getMaxY() + offset.getY(),
                        barnDoor.getMaxZ() + offset.getZ()))
                .spawn(spawn.add(offset))
                .starterChest(starterChest.add(offset))
                .delivery(delivery.add(offset))
                .deliveryBlockFace(deliveryBlockFace)
                .build();
    }
}
