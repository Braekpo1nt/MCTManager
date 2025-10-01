package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.mockito.Mockito.mock;

class RoundActiveStateTest {
    
    static World world;
    Random random = new Random(42);
    BorderStage currentBorderStage = BorderStage.builder()
            .size(20)
            .build();
    static List<Location> respawnLocations;
    
    @BeforeAll
    static void setup() {
        world = mock(World.class);
        respawnLocations = List.of(
                new Location(world, 0, 0, 0),
                new Location(world, 1, 1, 1),
                new Location(world, 2, 2, 2)
        );
    }
    
    @Test
    void onlyLastOption() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 1);
        Location actual = RoundActiveState.staticSelectRespawnLocation(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertEquals(new Location(world, 2, 2, 2), actual);
    }
    
    @Test
    void onlyMiddleOption() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 2);
        Location actual = RoundActiveState.staticSelectRespawnLocation(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertEquals(new Location(world, 1, 1, 1), actual);
    }
    
    @Test
    void firstAndLastOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(1);
        Location actual = RoundActiveState.staticSelectRespawnLocation(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertNotEquals(new Location(world, 1, 1, 1), actual);
        Assertions.assertEquals(new Location(world, 2, 2, 2), actual);
    }
    
    @Test
    void firstTwoOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(2);
        Location actual = RoundActiveState.staticSelectRespawnLocation(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertNotEquals(new Location(world, 2, 2, 2), actual);
        Assertions.assertEquals(new Location(world, 1, 1, 1), actual);
    }
    
    @Test
    void noOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 1, 2);
        Location actual = RoundActiveState.staticSelectRespawnLocation(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertEquals(new Location(world, 2, 2, 2), actual);
    }
    
    @Test
    void outOfRange() {
        Set<Integer> usedRespawnIndexes = Set.of();
        // make it too far away from the respawns
        int centerCoord = currentBorderStage.getSize() + 5;
        Location actual = RoundActiveState.staticSelectRespawnLocation(centerCoord, centerCoord, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertEquals(new Location(world, 3, 3, 3), actual);
    }
    
    @Test
    void outOfRange_NoOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 1, 2);
        Location actual = RoundActiveState.staticSelectRespawnLocation(25, 25, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), random);
        Assertions.assertEquals(new Location(world, 3, 3, 3), actual);
    }
}