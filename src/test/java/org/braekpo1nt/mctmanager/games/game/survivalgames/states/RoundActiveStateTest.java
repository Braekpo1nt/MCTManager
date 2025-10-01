package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.mockito.Mockito.mock;

class RoundActiveStateTest {
    @Test
    void test() {
        BorderStage currentBorderStage = BorderStage.builder()
                .size(20)
                .build();
        World world = mock(World.class);
        List<Location> respawnLocations = List.of(
                new Location(world, 0, 0, 0),
                new Location(world, 1, 1, 1),
                new Location(world, 2, 2, 2)
        );
        Set<Integer> usedRespawnIndexes = Set.of(0, 1);
        Location actual = RoundActiveState.staticSelectRespawnLocation(0, 0, currentBorderStage, respawnLocations, usedRespawnIndexes, new Location(world, 3, 3, 3), new Random(42));
        Assertions.assertEquals(new Location(world, 2, 2, 2), actual);
    }
}