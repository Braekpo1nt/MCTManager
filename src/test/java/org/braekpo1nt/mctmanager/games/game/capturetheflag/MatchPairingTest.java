package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class MatchPairingTest {
    
    MatchPairing northSouth = new MatchPairing("n", "s");
    MatchPairing southNorth = new MatchPairing("s", "n");
    MatchPairing other = new MatchPairing("a", "b");
    
    
    @Test
    void testEquals() {
        // order doesn't matter
        Assertions.assertEquals(northSouth, southNorth);
        Assertions.assertEquals(southNorth, northSouth);
        
        // object reference doesn't matter
        Assertions.assertEquals(new MatchPairing("n", "s"), northSouth);
        Assertions.assertEquals(new MatchPairing("s", "n"), northSouth);
        Assertions.assertEquals(new MatchPairing("n", "s"), southNorth);
        Assertions.assertEquals(new MatchPairing("s", "n"), southNorth);
        
        // partial match not successful
        Assertions.assertNotEquals(new MatchPairing("n", "b"), northSouth);
        Assertions.assertNotEquals(new MatchPairing("s", "b"), northSouth);
        Assertions.assertNotEquals(new MatchPairing("a", "n"), northSouth);
        Assertions.assertNotEquals(new MatchPairing("a", "s"), northSouth);
    }
    
    @Test
    void testMap() {
        Map<MatchPairing, Integer> singleMap = Map.of(northSouth, 1);
        Assertions.assertEquals(1, singleMap.get(southNorth));
        Assertions.assertEquals(1, singleMap.get(new MatchPairing("n", "s")));
        Assertions.assertEquals(1, singleMap.get(new MatchPairing("s", "n")));
        Assertions.assertNull(singleMap.get(other));
    
        Map<MatchPairing, Integer> aMap = new HashMap<>();
        aMap.put(northSouth, 1);
        aMap.put(southNorth, 2);
        Assertions.assertEquals(2, aMap.get(northSouth));
        Assertions.assertEquals(2, aMap.get(southNorth));
        Assertions.assertEquals(2, aMap.get(new MatchPairing("s", "n")));
        Assertions.assertEquals(2, aMap.get(new MatchPairing("n", "s")));
        Assertions.assertNull(aMap.get(other));
        
    }
    
}