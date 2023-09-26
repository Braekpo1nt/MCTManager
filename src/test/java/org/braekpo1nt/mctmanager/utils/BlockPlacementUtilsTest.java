package org.braekpo1nt.mctmanager.utils;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlockPlacementUtilsTest {
    
    @BeforeEach
    void setup() {
        MockBukkit.mock();
    }
    
    @AfterEach
    void teardown() {
        MockBukkit.unmock();
    }
    
    @Test
    void createCube() {
        Assertions.assertDoesNotThrow(() -> {
            BlockPlacementUtils.createCube(WorldCreator.name("World").createWorld(), 0, 0, 0, 5, 4, 1, Material.GLASS_PANE);
        });
    }
    
    @Test
    void createCubeNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BlockPlacementUtils.createCube(WorldCreator.name("World").createWorld(), 0, 0, 0, 0, 4, 1, Material.GLASS_PANE);
        });
    }
    
    @Test
    void createCubeBoundingBoxInt() {
        Assertions.assertDoesNotThrow(() -> {
            BlockPlacementUtils.createCube(WorldCreator.name("World").createWorld(), new BoundingBox(0, 0, 0, 1, 2, 3), Material.GLASS_PANE);
        });
    }
    
    @Test
    void createCubeBoundingBoxIntFlat() {
        Assertions.assertDoesNotThrow(() -> {
            BlockPlacementUtils.createCube(WorldCreator.name("World").createWorld(), new BoundingBox(0, 0, 0, 0, 2, 2), Material.GLASS_PANE);
        });
    }
    
    @Test
    void createCubeBoundingBox() {
        BlockPlacementUtils.createCubeReplace(WorldCreator.name("World").createWorld(), new BoundingBox(0, 0, 0, 0, 2, 2), Material.AIR, Material.STONE);
    }
    
    @Test
    void createCubeBoundingBoxIntBackwards() {
        Assertions.assertDoesNotThrow(() -> {
            BlockPlacementUtils.createCube(WorldCreator.name("World").createWorld(), new BoundingBox(1, 2, 3, 0, 0, 0), Material.GLASS_PANE);
        });
    }
    
    @Test
    void createCubeBoundingBoxDouble() {
        Assertions.assertDoesNotThrow(() -> {
            BlockPlacementUtils.createCube(WorldCreator.name("World").createWorld(), new BoundingBox(0.2, 0.5, 0.6, 1.2, 2.5, 3.6), Material.GLASS_PANE);
        });
    }
    
}