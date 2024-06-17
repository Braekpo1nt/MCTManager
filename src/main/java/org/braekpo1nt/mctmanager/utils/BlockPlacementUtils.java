package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Rotatable;
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
        Location nonAirLocation = location.subtract(0, 1, 0);
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
        Location airLocation = location.add(0, 1, 0);
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
}
