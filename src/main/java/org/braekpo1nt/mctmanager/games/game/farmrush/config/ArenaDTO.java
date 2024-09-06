package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * All coordinate locations will be assuming an origin of {@code (0, 0, 0)}.
 */
@Data
class ArenaDTO {
    /**
     * the dimensions (or size) of the arena. Assuming {@code (0, 0, 0)} is the origin.
     */
    private Size size;
    /**
     * the dimensions of the barn area. Used for keeping players in that area under
     * certain circumstances. 
     */
    private BoundingBox barn;
    /**
     * the dimensions of the barn door. Used to place and remove glass at the start of the game.
     */
    private BoundingBox barnDoor;
    /**
     * where the players spawn.
     */
    private Vector spawn;
    /**
     * the location of the starter chest, to be filled with starter items.
     */
    private Vector starterChest;
    /**
     * The direction that the starter chest should face. Defaults to {@link BlockFace#NORTH}
     */
    private @Nullable BlockFace starterChestBlockFace;
    /**
     * The location of the delivery barrel
     */
    private Vector delivery;
    /**
     * The direction that the starter chest should face. Defaults to {@link BlockFace#NORTH}
     */
    private @Nullable BlockFace deliveryBlockFace;
    
    @Data
    static class Size {
        int sizeX;
        int sizeY;
        int sizeZ;
    }
    
    public Arena toArena(World world) {
        return Arena.builder()
                .bounds(new BoundingBox(0, 0, 0, size.getSizeX(), size.getSizeY(), size.getSizeZ()))
                .barn(this.barn)
                .barnDoor(this.barnDoor)
                .spawn(this.spawn.toLocation(world))
                .starterChest(this.starterChest.toLocation(world))
                .starterChestBlockFace(this.starterChestBlockFace != null ? this.starterChestBlockFace : BlockFace.NORTH)
                .delivery(this.delivery.toLocation(world))
                .deliveryBlockFace(this.deliveryBlockFace != null ? this.deliveryBlockFace : BlockFace.NORTH)
                .build();
    }
}
