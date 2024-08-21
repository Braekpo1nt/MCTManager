package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class CaptureTheFlagUtilsTest {
    
    private final int NUMBER_OF_ARENAS = 4;
    
    // generateMatchPairings
    @Test
    @DisplayName("A list of 7 team names returns the right match pairings")
    void largeListTest() {
        List<String> teamIds = Arrays.asList("A", "B", "C", "D", "E", "F", "G");
        List<MatchPairing> actualMatchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        
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
    void twoTeamIds() {
        List<String> teamIds = Arrays.asList("North", "South");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        Assertions.assertEquals(1, matchPairings.size());
        Assertions.assertEquals("North", matchPairings.get(0).northTeam());
        Assertions.assertEquals("South", matchPairings.get(0).southTeam());
    }
    
    @Test
    @DisplayName("3 team names generates 3 match pairings, in the right order")
    void threeTeamIds() {
        List<String> teamIds = Arrays.asList("One", "Two", "Three");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        Assertions.assertEquals(3, matchPairings.size());
        Assertions.assertEquals("One", matchPairings.get(0).northTeam());
        Assertions.assertEquals("Two", matchPairings.get(0).southTeam());
        Assertions.assertEquals("One", matchPairings.get(1).northTeam());
        Assertions.assertEquals("Three", matchPairings.get(1).southTeam());
        Assertions.assertEquals("Two", matchPairings.get(2).northTeam());
        Assertions.assertEquals("Three", matchPairings.get(2).southTeam());
    }
    
    // containsEitherTeam
    @Test
    @DisplayName("containsEitherTeam can detect when a list of MatchPairings contains the north team, the south team, or neither team in a given match pairing")
    void containsNorthTeam() {
        List<MatchPairing> matchPairings = Arrays.asList(
                new MatchPairing("A", "B"),
                new MatchPairing("A", "C"),
                new MatchPairing("B", "C")
        );
        Assertions.assertTrue(CaptureTheFlagUtils.containsEitherTeam(matchPairings, new MatchPairing("C", "D")));
        Assertions.assertTrue(CaptureTheFlagUtils.containsEitherTeam(matchPairings, new MatchPairing("D", "B")));
        Assertions.assertTrue(CaptureTheFlagUtils.containsEitherTeam(matchPairings, new MatchPairing("A", "B")));
        Assertions.assertFalse(CaptureTheFlagUtils.containsEitherTeam(matchPairings, new MatchPairing("D", "E")));
    }
    
    
    // generateRounds
    
    @Test
    @DisplayName("The same team isn't in two different matches within the same round")
    void duplicateTeamInRound() {
        List<MatchPairing> matchPairings = Arrays.asList(
                new MatchPairing("A", "B"),
                new MatchPairing("A", "C"),
                new MatchPairing("B", "C")
        );
        List<List<MatchPairing>> roundMatchPairingLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings, NUMBER_OF_ARENAS);
        Assertions.assertEquals(3, roundMatchPairingLists.size());
    
        Assertions.assertEquals(1, roundMatchPairingLists.get(0).size());
        Assertions.assertEquals(roundMatchPairingLists.get(0).get(0), new MatchPairing("A", "B"));
        Assertions.assertEquals(1, roundMatchPairingLists.get(1).size());
        Assertions.assertEquals(roundMatchPairingLists.get(1).get(0), new MatchPairing("A", "C"));
        Assertions.assertEquals(1, roundMatchPairingLists.get(2).size());
        Assertions.assertEquals(roundMatchPairingLists.get(2).get(0), new MatchPairing("B", "C"));
    }
    
    @Test
    @DisplayName("1 match pairing makes 1 round with 1 match")
    void singleMatchPairing() {
        List<String> teamIds = Arrays.asList("A", "B");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        List<List<MatchPairing>> roundMatchPairingLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings, NUMBER_OF_ARENAS);
        Assertions.assertEquals(1, roundMatchPairingLists.size());
        Assertions.assertEquals(1, roundMatchPairingLists.get(0).size());
    }
    
    @Test
    @DisplayName("3 teams and their match pairings makes 3 match pairing lists")
    void threeTeamsTest() {
        List<String> teamIds = Arrays.asList("A", "B", "C");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        List<List<MatchPairing>> roundLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings, NUMBER_OF_ARENAS);
        
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
        List<String> teamIds = Arrays.asList("A", "B", "C", "D");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        List<List<MatchPairing>> roundLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings, NUMBER_OF_ARENAS);
        
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
    
    @Test
    @DisplayName("5 teams generates 7 matches")
    void fiveTeamsTest() {
        List<String> teamIds = Arrays.asList("A", "B", "C", "D", "E");
        List<MatchPairing> matchPairings = CaptureTheFlagUtils.generateMatchPairings(teamIds);
        List<List<MatchPairing>> roundLists = CaptureTheFlagUtils.generateRoundMatchPairings(matchPairings, NUMBER_OF_ARENAS);
        
        Assertions.assertEquals(7, roundLists.size());
    }
}