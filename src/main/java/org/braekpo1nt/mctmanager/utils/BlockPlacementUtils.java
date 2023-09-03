package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.util.BoundingBox;

public class BlockPlacementUtils {
    /**
     * Make a cube of the given material in the given world roundStarting at the given origin and with the given size. 
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
     * @param box the bounding box whose min pos will be the origin, and whose x-width, y-height, and z-width will be the size 
     * @param blockType the type of material to place in the cube
     */
    public static void createCube(World world, BoundingBox box, Material blockType) {
        int xOrigin = (int) box.getMinX();
        int yOrigin = (int) box.getMinY();
        int zOrigin = (int) box.getMinX();
        // calculate size
        int xSize = (int) box.getWidthX();
        int ySize = (int) box.getHeight();
        int zSize = (int) box.getWidthZ();
        createCube(world, xOrigin, yOrigin, zOrigin, xSize, ySize, zSize, blockType);
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
    
    
}
