package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

class CaptureTheFlagUtilsTest {
    
    // generateMatchPairings
    @Test
    @DisplayName("A list of 7 team names returns the right match pairings")
    void largeListTest() {
        List<String> teamNames = Arrays.asList("A", "B", "C", "D", "E", "F", "G");
        List<MatchPairing> actualMatchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        
        List<MatchPairing> expectedMatchPairings = Arrays.asList(
                new MatchPairing("A", "B"),
                new MatchPairing("A", "C"),
                new MatchPairing("A", "D"),
                new MatchPairing("A", "E"),
                new MatchPairing("A", "F"),
                new MatchPairing("A", "G"),
                new MatchPairing("B", "C"),
                new MatchPairing("B", "D"),
                new MatchPairing("B", "E"),
                new MatchPairing("B", "F"),
                new MatchPairing("B", "G"),
                new MatchPairing("C", "D"),
                new MatchPairing("C", "E"),
                new MatchPairing("C", "F"),
                new MatchPairing("C", "G"),
                new MatchPairing("D", "E"),
                new MatchPairing("D", "F"),
                new MatchPairing("D", "G"),
                new MatchPairing("E", "F"),
                new MatchPairing("E", "G"),
                new MatchPairing("F", "G")
        );
        Assertions.assertEquals(expectedMatchPairings, actualMatchPairings);
    }
    
    @Test
    @DisplayName("2 team names generate one match pairing")
    void twoTeamNames() {
        List<String> teamNames = Arrays.asList("North", "South");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        Assertions.assertEquals(1, matchPairings.size());
        Assertions.assertEquals("North", matchPairings.get(0).northTeam());
        Assertions.assertEquals("South", matchPairings.get(0).southTeam());
    }
    
    @Test
    @DisplayName("3 team names generates 3 match pairings, in the right order")
    void threeTeamNames() {
        List<String> teamNames = Arrays.asList("One", "Two", "Three");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        Assertions.assertEquals(3, matchPairings.size());
        Assertions.assertEquals("One", matchPairings.get(0).northTeam());
        Assertions.assertEquals("Two", matchPairings.get(0).southTeam());
        Assertions.assertEquals("One", matchPairings.get(1).northTeam());
        Assertions.assertEquals("Three", matchPairings.get(1).southTeam());
        Assertions.assertEquals("Two", matchPairings.get(2).northTeam());
        Assertions.assertEquals("Three", matchPairings.get(2).southTeam());
    }
    
    // generateRounds
    
    @Test
    @DisplayName("The same team isn't in two different matches within the same round")
    void duplicateTeamInRound() {
        List<String> teamNames = Arrays.asList("A", "B", "C");
        List<MatchPairing> matchPairings = Arrays.asList(
                new MatchPairing("A", "B"),
                new MatchPairing("A", "C"),
                new MatchPairing("B", "C")
        );
        List<List<MatchPairing>> roundMatchPairingLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings);
        Assertions.assertEquals(3, roundMatchPairingLists.size());
        
        for (List<MatchPairing> matchPairingList : roundMatchPairingLists) {
            for (String teamName : teamNames) {
                int count = 0;
                for (MatchPairing matchPairing : matchPairingList) {
                    if (matchPairing.containsTeam(teamName)) {
                        count++;
                    }
                }
                Assertions.assertTrue(count <= 1);
            }
        }
    }
    
    @Test
    @DisplayName("1 match pairing makes 1 round with 1 match")
    void singleMatchPairing() {
        List<String> teamNames = Arrays.asList("A", "B");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        List<List<MatchPairing>> roundMatchPairingLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings);
        Assertions.assertEquals(1, roundMatchPairingLists.size());
        Assertions.assertEquals(1, roundMatchPairingLists.get(0).size());
    }
    
    @Test
    @DisplayName("3 teams and their match pairings makes 3 match pairing lists")
    void threeTeamsTest() {
        List<String> teamNames = Arrays.asList("A", "B", "C");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        List<List<MatchPairing>> roundLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings);
        
        Assertions.assertEquals(3, roundLists.size());
        
        Assertions.assertEquals(1, roundLists.get(0).size());
        Assertions.assertTrue(roundLists.get(0).contains(new MatchPairing("A", "B")));
        
        Assertions.assertEquals(1, roundLists.get(1).size());
        Assertions.assertTrue(roundLists.get(1).contains(new MatchPairing("A", "C")));
        
        Assertions.assertEquals(1, roundLists.get(2).size());
        Assertions.assertTrue(roundLists.get(2).contains(new MatchPairing("B", "C")));
    }
    
    @Test
    @DisplayName("3 teams and their match pairings makes 3 match pairing lists")
    void fourTeamsTest() {
        List<String> teamNames = Arrays.asList("A", "B", "C", "D");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        List<List<MatchPairing>> roundLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings);
        
        Assertions.assertEquals(3, roundLists.size());
        
        Assertions.assertEquals(2, roundLists.get(0).size());
        Assertions.assertTrue(roundLists.get(0).contains(new MatchPairing("A", "B")));
        Assertions.assertTrue(roundLists.get(0).contains(new MatchPairing("C", "D")));
    
        Assertions.assertEquals(2, roundLists.get(1).size());
        Assertions.assertTrue(roundLists.get(1).contains(new MatchPairing("A", "C")));
        Assertions.assertTrue(roundLists.get(1).contains(new MatchPairing("B", "D")));
    
        Assertions.assertEquals(2, roundLists.get(2).size());
        Assertions.assertTrue(roundLists.get(2).contains(new MatchPairing("A", "D")));
        Assertions.assertTrue(roundLists.get(2).contains(new MatchPairing("B", "C")));
        
        
        
    }
}