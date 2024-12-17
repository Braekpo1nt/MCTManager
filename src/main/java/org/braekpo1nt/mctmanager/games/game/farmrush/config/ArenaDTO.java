package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All coordinate locations will be assuming an origin of {@code (0, 0, 0)}.
 * All locations and bounding boxes must be within the bounding box formed by the (0, 0, 0) and the size.
 */
@Data
class ArenaDTO implements Validatable {
    /**
     * the dimensions (or size) of the arena. Assuming {@code (0, 0, 0)} is the origin.
     */
    private Size size;
    /**
     * the dimensions of the barn area. Used for keeping players in that area under
     * certain circumstances. Must have a volume of at least 1.0.
     */
    private BoundingBox barn;
    /**
     * the dimensions of the barn door. Used to place and remove glass at the start of the game.
     */
    private BoundingBox barnDoor;
    /**
     * where the players spawn.
     */
    private LocationDTO spawn;
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
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(size, "size");
        validator.notNull(barn, "barn");
        validator.validate(barn.getVolume() >= 1, "barn must have a volume of at least 1.0");
        validator.notNull(barnDoor, "barnDoor");
        validator.notNull(spawn, "spawn");
        validator.notNull(starterChest, "starterChest");
        validator.notNull(delivery, "delivery");
        
        // make sure everything is contained within the bounding box formed by the origin and the size
        BoundingBox arenaBounds = new BoundingBox(
                0, 0, 0, 
                size.getSizeX(), size.getSizeY(), size.getSizeZ());
        validator.validate(arenaBounds.contains(barn), "barn must be within the size");
        validator.validate(arenaBounds.contains(barnDoor), "barnDoor must be within the size");
        validator.validate(arenaBounds.contains(spawn.toVector()), "spawn must be within the size");
        validator.validate(arenaBounds.contains(starterChest), "starterChest must be within the size");
        validator.validate(arenaBounds.contains(delivery), "delivery must be within the size");
    }
    
    @Data
    static class Size {
        int sizeX;
        int sizeY;
        int sizeZ;
    }
    
    public Arena toArena(World world) {
        return Arena.builder()
                .world(world)
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
