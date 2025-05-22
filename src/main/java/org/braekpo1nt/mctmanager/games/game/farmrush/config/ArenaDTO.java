package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All locations are absolute. These are the details of the first arena. When other arenas are created,
 * an offset determined by the x-width of the first arena will be added to all coordinates.
 * Define this first arena as if it's the only one, with coordinates relative to the world
 * (not relative to the arena's origin). e.g. if you set spawn to (1,2,3), players
 * will spawn at (1,2,3) in the first arena.
 */
@Data
class ArenaDTO implements Validatable {
    /**
     * The placement origin of the schematic file. Where to tell WorldEdit to place the schematic.
     */
    private Vector schematicOrigin;
    /**
     * The bounding box of the arena (not to be confused with the {@link #schematicOrigin} which might be outside
     * the bounds of the arena depending on how the structure was created)
     * This will be used to delete the structure after the game is over
     */
    private BoundingBox bounds;
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
        validator.notNull(schematicOrigin, "schematicOrigin");
        validator.notNull(barn, "barn");
        validator.validate(barn.getVolume() >= 1, "barn must have a volume of at least 1.0");
        validator.notNull(barnDoor, "barnDoor");
        validator.notNull(spawn, "spawn");
        validator.notNull(starterChest, "starterChest");
        validator.notNull(delivery, "delivery");
        
        validator.notNull(bounds, "bounds");
        // make sure everything is contained within the bounds
        validator.validate(bounds.contains(barn), "barn must be within the size");
        validator.validate(bounds.contains(barnDoor), "barnDoor must be within the size");
        validator.validate(bounds.contains(spawn.toVector()), "spawn must be within the size");
        validator.validate(bounds.contains(starterChest), "starterChest must be within the size");
        validator.validate(bounds.contains(delivery), "delivery must be within the size");
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
                .schematicOrigin(this.schematicOrigin)
                .bounds(this.bounds)
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
