package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.MainTestBase;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

class CaptureTheFlagGameTest extends MainTestBase {
    
    private CaptureTheFlagGame captureTheFlagGame;
    private final List<MatchPairing> MATCH_PAIRINGS = Arrays.asList(
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South"),
            new MatchPairing("North", "South")
    );
    
    @BeforeEach
    void setup() {
        GameManager gameManager = mock(GameManager.class);
        captureTheFlagGame = new CaptureTheFlagGame(plugin, gameManager);
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