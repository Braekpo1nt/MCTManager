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
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertEquals(2, actualIndex);
    }
    
    @Test
    void onlyMiddleOption() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 2);
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertEquals(1, actualIndex);
    }
    
    @Test
    void firstAndLastOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(1);
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertNotEquals(1, actualIndex);
        Assertions.assertEquals(2, actualIndex);
    }
    
    @Test
    void firstTwoOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(2);
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertNotEquals(2, actualIndex);
        Assertions.assertEquals(1, actualIndex);
    }
    
    @Test
    void noOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 1, 2);
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertEquals(2, actualIndex);
    }
    
    @Test
    void outOfRange() {
        Set<Integer> usedRespawnIndexes = Set.of();
        // make it too far away from the respawns
        int centerCoord = currentBorderStage.getSize() + 5;
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(centerCoord, centerCoord, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertEquals(-1, actualIndex);
    }
    
    @Test
    void outOfRange_NoOptions() {
        Set<Integer> usedRespawnIndexes = Set.of(0, 1, 2);
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(25, 25, currentBorderStage, respawnLocations, usedRespawnIndexes, random);
        Assertions.assertEquals(-1, actualIndex);
    }
    
    @Test
    void oneOptionInRange() {
        List<Location> locations = List.of(
                new Location(world, 0, 0, 0), // used
                new Location(world, 1, 1, 1), // only choice
                new Location(world, 200, 200, 200) // out of range
        );
        Set<Integer> usedRespawnIndexes = Set.of(0);
        int actualIndex = RoundActiveState.selectRespawnLocationIndex(0, 0, currentBorderStage, locations, usedRespawnIndexes, random);
        Assertions.assertEquals(1, actualIndex);
    }
}