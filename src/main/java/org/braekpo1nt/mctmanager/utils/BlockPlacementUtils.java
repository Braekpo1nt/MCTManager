package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

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
    
}
