package org.braekpo1nt.mctmanager.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class BlockPlacementUtils {
    /**
     * Make a cube of the given material in the given world starting at the given origin and with the given size. 
     * @param world The world to make the cube in
     * @param xOrigin The minimum x origin of the cube
     * @param yOrigin The minimum y origin of the cube
     * @param zOrigin The minimum x origin of the cube
     * @param xSize Must be greater than or equal to 1. The size of the cube in the x direction.
     * @param ySize Must be greater than or equal to 1. The size of the cube in the y direction.
     * @param zSize Must be greater than or equal to 1. The size of the cube in the z direction.
     * @param blockType The type of block to place at each position in the cube
     */
    public static void createCube(World world, int xOrigin, int yOrigin, int zOrigin, int xSize, int ySize, int zSize, Material blockType) {
        if (xSize < 1 || ySize < 1 || zSize < 1) {
            throw new IllegalArgumentException(String.format("xSize, ySize, and zSize must be greater than or equal to 1, but were (%d, %d, %d)", xSize, ySize, zSize));
        }
        int xEnd = xOrigin + xSize - 1;
        int yEnd = yOrigin + ySize - 1;
        int zEnd = zOrigin + zSize - 1;
        
        for (int x = xOrigin; x <= xEnd; x++) {
            for (int y = yOrigin; y <= yEnd; y++) {
                for (int z = zOrigin; z <= zEnd; z++) {
                    Location location = new Location(world, x, y, z);
                    location.getBlock().setType(blockType);
                }
            }
        }
    }
    
    public static void createCube(Location origin, int xSize, int ySize, int zSize, Material blockType) {
        World world = origin.getWorld();
        int xOrigin = origin.getBlockX();
        int yOrigin = origin.getBlockY();
        int zOrigin = origin.getBlockZ();
        createCube(world, xOrigin, yOrigin, zOrigin, xSize, ySize, zSize, blockType);
    }
    
    /**
     * Create a cube in the given bounding box. Doubles will be truncated to ints and passed to {@link BlockPlacementUtils#createCube(World, int, int, int, int, int, int, Material)}
     * @param world the world to place the cube in
     * @param area the bounding box whose min pos will be the origin, and whose x-width, y-height, and z-width will be the size 
     * @param material the type of material to place in the cube
     */
    public static void createCube(World world, BoundingBox area, Material material) {
        int minX = area.getMin().getBlockX();
        int minY = area.getMin().getBlockY();
        int minZ = area.getMin().getBlockZ();
        int maxX = area.getMax().getBlockX();
        int maxY = area.getMax().getBlockY();
        int maxZ = area.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(material);
                }
            }
        }
    }
    
    public static void createCubeReplace(World world, BoundingBox area, Material replace, Material with) {
        int minX = area.getMin().getBlockX();
        int minY = area.getMin().getBlockY();
        int minZ = area.getMin().getBlockZ();
        int maxX = area.getMax().getBlockX();
        int maxY = area.getMax().getBlockY();
        int maxZ = area.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().equals(replace)) {
                        block.setType(with);
                    }
                }
            }
        }
    }
    
    public static void createHollowCube(World world, BoundingBox area, Material material) {
        int minX = area.getMin().getBlockX();
        int minY = area.getMin().getBlockY();
        int minZ = area.getMin().getBlockZ();
        int maxX = area.getMax().getBlockX();
        int maxY = area.getMax().getBlockY();
        int maxZ = area.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (
                            x == minX || x == maxX
                                    || y == minY || y == maxY
                                    || z == minZ || z == maxZ) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setType(material);
                    }
                }
            }
        }
    }
    
    public static void createCubeReplace(World world, int xOrigin, int yOrigin, int zOrigin, int xSize, int ySize, int zSize, Material replace, Material with) {
        int xEnd = xOrigin + xSize - 1;
        int yEnd = yOrigin + ySize - 1;
        int zEnd = zOrigin + zSize - 1;
        
        for (int x = xOrigin; x <= xEnd; x++) {
            for (int y = yOrigin; y <= yEnd; y++) {
                for (int z = zOrigin; z <= zEnd; z++) {
                    Location location = new Location(world, x, y, z);
                    Block block = location.getBlock();
                    if (block.getType().equals(replace)) {
                        location.getBlock().setType(with);
                    }
                }
            }
        }
    }
    
    public static void updateDirection(Location origin, int xSize, int ySize, int zSize) {
        World world = origin.getWorld();
        int xOrigin = origin.getBlockX();
        int yOrigin = origin.getBlockY();
        int zOrigin = origin.getBlockZ();
        updateDirection(world, xOrigin, yOrigin, zOrigin, xSize, ySize, zSize);
    }
    
    private static final BlockFace[] cardinalDirections = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    
    public static void updateDirection(World world, int xOrigin, int yOrigin, int zOrigin, int xSize, int ySize, int zSize) {
        int xEnd = xOrigin + xSize - 1;
        int yEnd = yOrigin + ySize - 1;
        int zEnd = zOrigin + zSize - 1;
        
        for (int x = xOrigin; x <= xEnd; x++) {
            for (int y = yOrigin; y <= yEnd; y++) {
                for (int z = zOrigin; z <= zEnd; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getBlockData() instanceof MultipleFacing multipleFacing) {
                        for (BlockFace direction : cardinalDirections) {
                            if (block.getRelative(direction).isSolid()) {
                                multipleFacing.setFace(direction, true);
                                block.setBlockData(multipleFacing);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void updateDirection(World world, BoundingBox area) {
        int minX = area.getMin().getBlockX();
        int minY = area.getMin().getBlockY();
        int minZ = area.getMin().getBlockZ();
        int maxX = area.getMax().getBlockX();
        int maxY = area.getMax().getBlockY();
        int maxZ = area.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getBlockData() instanceof MultipleFacing multipleFacing) {
                        for (BlockFace direction : cardinalDirections) {
                            if (block.getRelative(direction).isSolid()) {
                                multipleFacing.setFace(direction, true);
                                block.setBlockData(multipleFacing);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * @param world the world to search for blocks in
     * @param box the box to search for blocks in
     * @param types the types of blocks to search for
     * @return all the blocks of the given types which are in the given BoundingBox
     */
    public static List<Block> getBlocks(@NotNull World world, @NotNull BoundingBox box, @NotNull List<Material> types) {
        List<Block> solidBlocks = new ArrayList<>();
        
        for (int x = box.getMin().getBlockX(); x <= box.getMaxX(); x++) {
            for (int y = box.getMin().getBlockY(); y <= box.getMaxY(); y++) {
                for (int z = box.getMin().getBlockZ(); z <= box.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (types.contains(block.getType())) {
                        solidBlocks.add(block);
                    }
                }
            }
        }
        
        return solidBlocks;
    }
    
    public static final List<Material> AIR_BLOCKS = List.of(
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR
    );
    
    /**
     * Gets the first solid block below the given location. If there is no floor all the way to the min height, returns the given location.
     * @param location The location to check below
     * @return the location below the given location that is a solid block. If there are no solid blocks, returns the given location. 
     */
    public static Location getSolidBlockBelow(@NotNull Location location) {
        Location nonAirLocation = location.clone().subtract(0, 1, 0);
        int minHeight = location.getWorld().getMinHeight();
        while (nonAirLocation.getBlockY() > minHeight) {
            Block block = nonAirLocation.getBlock();
            if (!AIR_BLOCKS.contains(block.getType())) {
                return nonAirLocation;
            }
            nonAirLocation = nonAirLocation.subtract(0, 1, 0);
        }
        return location;
    }
    
    /**
     * Gets the first non-solid block above the given location. If there is nothing all the way to the max height, returns the given location.
     * @param location the location to check above
     * @return the location above the given location that is not a solid block. If there is no non-solid blocks, returns the given location. 
     */
    public static Location getNonSolidBlockAbove(@NotNull Location location) {
        Location airLocation = location.clone().add(0, 1, 0);
        int maxHeight = location.getWorld().getMaxHeight();
        while (airLocation.getBlockY() < maxHeight) {
            Block block = airLocation.getBlock();
            if (AIR_BLOCKS.contains(block.getType())) {
                return airLocation;
            }
            airLocation = airLocation.add(0, 1, 0);
        }
        return location;
    }
    
    /**
     * @param location the location to check above.
     * @return the highest solid block directly above the given location. Null if it's all air. Does not check below the given location.
     */
    public static @Nullable Location getTopBlock(@NotNull Location location) {
        int maxHeight = location.getWorld().getMaxHeight();
        int minHeight = Math.max(location.getWorld().getMinHeight(), location.getBlockY());
        Location highestSolidLoc = new Location(location.getWorld(), location.getBlockX(), maxHeight, location.getBlockZ());
        for (double y = maxHeight; y > minHeight; y--) {
            Block block = highestSolidLoc.getBlock();
            if (block.isSolid()) {
                return highestSolidLoc;
            }
            highestSolidLoc.subtract(0, 1, 0);
        }
        return null;
    }
    
    /**
     * This is so that many block drops (such as flags in Capture the Flag) can seem to "land" on the ground without disturbing the environment or floating where no player can reach them. 
     * If the location is in a body of water or some other non-air block, then it will return the lowest location of an empty block above the given location. If the location is an air block, then it will return the lowest location of an empty block below the given location. 
     * @param location the location where the block drop has appeared
     * @return the location to place the block drop. Should be an empty block just above the ground.
     */
    public static Location getBlockDropLocation(@NotNull Location location) {
        if (AIR_BLOCKS.contains(location.getBlock().getType())) {
            return getSolidBlockBelow(location).add(0, 1, 0);
        } else {
            return getNonSolidBlockAbove(location);
        }
    }
    
    /**
     * Places the provided banner type at the given location facing the given direction
     * If the given material's blockData does not implement {@link Rotatable}, then the facing direction
     * will be ignored and the block will simply be placed.
     * @param bannerType the material type of the banner to place
     * @param location the location to place the banner
     * @param facing the direction to face the banner
     */
    public static void placeFlag(Material bannerType, Location location, BlockFace facing) {
        Block flagBlock = location.getBlock();
        flagBlock.setType(bannerType);
        if (flagBlock.getBlockData() instanceof Rotatable flagData) {
            flagData.setRotation(facing);
            flagBlock.setBlockData(flagData);
        }
    }
    
    /**
     * Places the given schematic file in the given world at all the given origins. If there are n origins, n copies of the schematic will be placed, each with their origin at the given values.<br>
     * Use this in favor of {@link #placeSchematic(World, int, int, int, File)} multiple times
     * in a row because this optimizes the multiple placement. 
     * @param world the world to place in
     * @param origins the list of origins. Each schematic copy placed will use one of these as their origin, using their integer block values. 
     * @param file the .schem schematic file to use
     */
    public static void placeSchematic(World world, @NotNull List<Vector> origins, File file) {
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Main.logger().severe("Could not find file " + file);
            return;
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (FileNotFoundException e) {
            Main.logger().log(Level.SEVERE, "Could not find file " + file, e);
            return;
        } catch (IOException e) {
            Main.logger().log(Level.SEVERE, "Exception while reading from file " + file, e);
            return;
        }
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            for (Vector origin : origins) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .ignoreAirBlocks(false)
                        .copyBiomes(true)
                        .copyEntities(true)
                        .to(BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ()))
                        .build();
                Operations.complete(operation);
            }
        } catch (WorldEditException e) {
            Main.logger().log(Level.SEVERE, "Exception while pasting", e);
        }
    }
    
    /**
     * Places the given schematic file in the given world at the given origin
     * @param world the world to place in
     * @param x the origin x
     * @param y the origin y
     * @param z the origin z
     * @param file the .schem schematic file to use
     */
    public static void placeSchematic(World world, int x, int y, int z, File file) {
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Main.logger().severe("Could not find file " + file);
            return;
        }
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (FileNotFoundException e) {
            Main.logger().log(Level.SEVERE, "Could not find file " + file, e);
            return;
        } catch (IOException e) {
            Main.logger().log(Level.SEVERE, "Exception while reading from file " + file, e);
            return;
        }
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .ignoreAirBlocks(false)
                    .copyBiomes(true)
                    .copyEntities(true)
                    .to(BlockVector3.at(x, y, z))
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            Main.logger().log(Level.SEVERE, "Exception while pasting", e);
        }
    }
    
    /**
     * Uses WorldEdit to fill the given list of BoundingBoxes with air. Uses the block-location of each vector.
     * @param world the world
     * @param boxes the list of boxes
     */
    public static void fillWithAir(World world, List<BoundingBox> boxes) {
        // Create an edit session for the WorldEdit world
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            
            for (BoundingBox box : boxes) {
                BlockVector3 min = BlockVector3.at(
                        box.getMin().getBlockX(), 
                        box.getMin().getBlockY(), 
                        box.getMin().getBlockZ());
                BlockVector3 max = BlockVector3.at(
                        box.getMax().getBlockX(),
                        box.getMax().getBlockY(),
                        box.getMax().getBlockZ());
                CuboidRegion region = new CuboidRegion(min, max);
                
                // Fill the region with air blocks
                editSession.setBlocks(region, Objects.requireNonNull(BlockTypes.AIR).getDefaultState());
            }
            
            // Flush the edit session to apply changes
            editSession.commit();
        } catch (MaxChangedBlocksException e) {
            Main.logger().log(Level.SEVERE, "error occurred filling with air blocks", e);
        }
    }
    
}
