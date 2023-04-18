package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

class CaptureTheFlagGameTest {
    
    private CaptureTheFlagGame captureTheFlagGame;
    
    @BeforeEach
    void setup() {
        Main plugin = mock(Main.class);
        GameManager gameManager = mock(GameManager.class);
        captureTheFlagGame = new CaptureTheFlagGame(plugin, gameManager);
    }
    
    @Test
    void oneMatchGeneratesSingleRound() {
        List<MatchPairing> matchPairings = Arrays.asList(
                new MatchPairing("North", "South")
        );
        List<CaptureTheFlagRound> rounds = captureTheFlagGame.generateRounds(matchPairings);
        Assertions.assertEquals(1, rounds.size());
    }
}