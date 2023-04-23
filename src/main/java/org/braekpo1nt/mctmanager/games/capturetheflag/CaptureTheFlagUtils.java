package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CaptureTheFlagUtils {
    
    /**
     * Generates all combinations of size 2 of the given teams in the form of {@link MatchPairing}s 
     * @param teamNames The teams to generate the combinations of
     * @return A list of at least two MatchPairing objects representing all combinations of size 2
     * of the given list of team names
     * @throws IndexOutOfBoundsException if teamNames.size() is 1 or less
     */
    public static @NotNull List<MatchPairing> generateMatchPairings(@NotNull List<String> teamNames) {
        List<MatchPairing> combinations = new ArrayList<>();
        for (int i = 0; i < teamNames.size(); i++) {
            for (int j = i + 1; j < teamNames.size(); j++) {
                String northTeam = teamNames.get(i);
                String southTeam = teamNames.get(j);
                MatchPairing pairing = new MatchPairing(northTeam, southTeam);
                combinations.add(pairing);
            }
        }
        return combinations;
    }
    
    public static @NotNull List<List<MatchPairing>> generateRoundMatchPairings(@NotNull List<MatchPairing> matchPairings) {
        List<List<MatchPairing>> roundMatchPairingLists = new ArrayList<>();
        return roundMatchPairingLists;
    }
    
}
