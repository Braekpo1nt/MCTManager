package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class Arena {
    private @NotNull World world;
    private @NotNull Vector schematicOrigin;
    private @NotNull BoundingBox bounds;
    private @NotNull BoundingBox barn;
    private @NotNull BoundingBox barnDoor;
    private @NotNull Location spawn;
    private @NotNull Location starterChest;
    private @NotNull BlockFace starterChestBlockFace;
    private @NotNull Location delivery;
    private @NotNull BlockFace deliveryBlockFace;
    
    /**
     * Create a new {@link Arena} from this arena using the given offset, where
     * all Locations and BoundingBoxes are translated by the given offset. <br>
     * This operation does not affect this {@link Arena}'s data.
     * @param offset the offset to add to all the coordinates of this {@link Arena}
     * @return a new {@link Arena} offset from the given
     */
    public Arena offset(Vector offset) {
        return Arena.builder()
                .world(world)
                .schematicOrigin(new Vector(
                        schematicOrigin.getX() + offset.getX(),
                        schematicOrigin.getY() + offset.getY(),
                        schematicOrigin.getZ() + offset.getZ()
                ))
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
                .spawn(new Location(
                        spawn.getWorld(),
                        spawn.getX() + offset.getX(),
                        spawn.getY() + offset.getY(),
                        spawn.getZ() + offset.getZ(),
                        spawn.getYaw(),
                        spawn.getPitch()
                ))
                .starterChest(new Location(
                        starterChest.getWorld(),
                        starterChest.getX() + offset.getX(),
                        starterChest.getY() + offset.getY(),
                        starterChest.getZ() + offset.getZ()
                ))
                .starterChestBlockFace(starterChestBlockFace)
                .delivery(new Location(
                        delivery.getWorld(),
                        delivery.getX() + offset.getX(),
                        delivery.getY() + offset.getY(),
                        delivery.getZ() + offset.getZ()
                ))
                .deliveryBlockFace(deliveryBlockFace)
                .build();
    }
    
    /**
     * closes the barn door for this {@link Arena}. Replaces {@link Material#AIR} with the assigned barnDoorMaterial in
     * the {@link #barnDoor} box.
     */
    public void closeBarnDoor() {
        BlockPlacementUtils.createCubeReplace(world, barnDoor, Material.AIR, Material.GLASS);
    }
    
    /**
     * opens the barn door for this {@link Arena}. Replaces barnDoorMaterial with {@link Material#AIR} in the
     * {@link #barnDoor} box.
     */
    public void openBarnDoor() {
        BlockPlacementUtils.createCubeReplace(world, barnDoor, Material.GLASS, Material.AIR);
    }
}
