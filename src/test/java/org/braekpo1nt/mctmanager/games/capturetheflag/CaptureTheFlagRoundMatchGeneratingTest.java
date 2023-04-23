package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.braekpo1nt.mctmanager.MainTestBase;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

class CaptureTheFlagRoundMatchGeneratingTest extends MainTestBase {
    
    private CaptureTheFlagGame captureTheFlagGame;
    private final List<MatchPairing> MATCH_PAIRINGS = Arrays.asList(
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
    
    @BeforeEach
    void setup() {
        GameManager gameManager = mock(GameManager.class);
        captureTheFlagGame = new CaptureTheFlagGame(plugin, gameManager);
    }
    
    // generateMatchPairings
    @Test
    @DisplayName("2 team names generate one match pairing")
    void twoTeamNames() {
        List<String> teamNames = Arrays.asList("North", "South");
        List<MatchPairing> matchPairings = CaptureTheFlagGame.generateMatchPairings(teamNames);
        Assertions.assertEquals(1, matchPairings.size());
        Assertions.assertEquals("North", matchPairings.get(0).northTeam());
        Assertions.assertEquals("South", matchPairings.get(0).southTeam());
    }
    
    @Test
    @DisplayName("3 team names generates 3 match pairings, in the right order")
    void threeTeamNames() {
        List<String> teamNames = Arrays.asList("One", "Two", "Three");
        List<MatchPairing> matchPairings = CaptureTheFlagGame.generateMatchPairings(teamNames);
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
        List<MatchPairing> matchPairings = Arrays.asList(
                new MatchPairing("A", "B"),
                new MatchPairing("A", "C"),
                new MatchPairing("B", "C")
        );
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(3, rounds.size());
        Assertions.assertEquals(1, rounds.get(0).getMatches().size());
        Assertions.assertEquals(new MatchPairing("A", "B"), rounds.get(0).getMatches().get(0).getMatchPairing());
        Assertions.assertEquals(new MatchPairing("A", "C"), rounds.get(1).getMatches().get(0).getMatchPairing());
        Assertions.assertEquals(new MatchPairing("B", "C"), rounds.get(2).getMatches().get(0).getMatchPairing());
    }
    
    @Test
    @DisplayName("1 match pairing makes 1 round with 1 match")
    void singleMatchPairing() {
        List<MatchPairing> matchPairings = MATCH_PAIRINGS.subList(0,1);
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(1, rounds.size());
        Assertions.assertEquals(1, rounds.get(0).getMatches().size());
    }
    
    @Test
    @DisplayName("4 match pairings make exactly 1 round with 4 matches")
    void equalNumberOfMatchesAndArenas() {
        List<MatchPairing> matchPairings = MATCH_PAIRINGS.subList(0,4);
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(1, rounds.size());
        Assertions.assertEquals(4, rounds.get(0).getMatches().size());
    }
    
    @Test
    @DisplayName("5 match pairings should produce 2 rounds, first one with 4 matches and last one with 1 match")
    void justEnoughToMakeTwoMatches() {
        List<MatchPairing> matchPairings = MATCH_PAIRINGS.subList(0,5);
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(2, rounds.size());
        Assertions.assertEquals(4, rounds.get(0).getMatches().size());
        Assertions.assertEquals(1, rounds.get(1).getMatches().size());
    }
    
    @Test
    @DisplayName("9 match pairings should produce 3 rounds, all but the last with 4 matches, and the last one with 1 match")
    void nineMatchPairings() {
        List<MatchPairing> matchPairings = MATCH_PAIRINGS.subList(0,9);
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(3, rounds.size());
        Assertions.assertEquals(4, rounds.get(0).getMatches().size());
        Assertions.assertEquals(4, rounds.get(1).getMatches().size());
        Assertions.assertEquals(1, rounds.get(2).getMatches().size());
    }
    
    @Test
    @DisplayName("16 match pairings should produce 4 rounds, all with 4 matches")
    void sixteenMatchPairings() {
        List<MatchPairing> matchPairings = MATCH_PAIRINGS.subList(0,16);
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(4, rounds.size());
        Assertions.assertEquals(4, rounds.get(0).getMatches().size());
        Assertions.assertEquals(4, rounds.get(1).getMatches().size());
        Assertions.assertEquals(4, rounds.get(2).getMatches().size());
        Assertions.assertEquals(4, rounds.get(3).getMatches().size());
    }
    
    @Test
    @DisplayName("17 match pairings should produce 5 rounds, all with 4 matches except the last one with 1 match")
    void seventeenMatchPairings() {
        List<MatchPairing> matchPairings = MATCH_PAIRINGS.subList(0, 17);
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(5, rounds.size());
        Assertions.assertEquals(4, rounds.get(0).getMatches().size());
        Assertions.assertEquals(4, rounds.get(1).getMatches().size());
        Assertions.assertEquals(4, rounds.get(2).getMatches().size());
        Assertions.assertEquals(4, rounds.get(3).getMatches().size());
        Assertions.assertEquals(1, rounds.get(4).getMatches().size());
    }
}