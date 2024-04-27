package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
}
