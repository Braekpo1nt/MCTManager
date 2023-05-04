package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class BlockPlacementUtils {
    public static void createCube(World world, int xOrigin, int yOrigin, int zOrigin, int xSize, int ySize, int zSize, Material blockType) {
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
    
    public static void updateDirection(World world, int xOrigin, int yOrigin, int zOrigin, int xSize, int ySize, int zSize) {
        for (int x = xOrigin; x < xOrigin + xSize; x++) {
            for (int y = yOrigin; y < yOrigin + ySize; y++) {
                for (int z = zOrigin; z < zOrigin + zSize; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getBlockData() instanceof Directional directional) {
                        boolean north = false;
                        boolean east = false;
                        boolean south = false;
                        boolean west = false;
                        if (block.getRelative(BlockFace.NORTH).getType() == directional.getMaterial() && block.getRelative(BlockFace.NORTH).getBlockData() instanceof Directional northBlockData) {
                            north = northBlockData.getFacing() == BlockFace.SOUTH;
                        }
                        if (block.getRelative(BlockFace.EAST).getType() == directional.getMaterial() && block.getRelative(BlockFace.EAST).getBlockData() instanceof Directional eastBlockData) {
                            east = eastBlockData.getFacing() == BlockFace.WEST;
                        }
                        if (block.getRelative(BlockFace.SOUTH).getType() == directional.getMaterial() && block.getRelative(BlockFace.SOUTH).getBlockData() instanceof Directional southBlockData) {
                            south = southBlockData.getFacing() == BlockFace.NORTH;
                        }
                        if (block.getRelative(BlockFace.WEST).getType() == directional.getMaterial() && block.getRelative(BlockFace.WEST).getBlockData() instanceof Directional westBlockData) {
                            west = westBlockData.getFacing() == BlockFace.EAST;
                        }
                        directional.setFacing(north ? BlockFace.NORTH : south ? BlockFace.SOUTH : east ? BlockFace.EAST : west ? BlockFace.WEST : directional.getFacing());
                        block.setBlockData(directional);
                    }
                }
            }
        }
    }
    
    
}
