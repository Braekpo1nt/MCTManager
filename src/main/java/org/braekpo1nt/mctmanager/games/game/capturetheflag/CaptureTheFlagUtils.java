package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
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
    
    public static @NotNull List<List<MatchPairing>> generateRoundMatchPairings(@NotNull List<MatchPairing> matchPairings, int maxRoundPairingsSize) {
        List<List<MatchPairing>> roundPairingsList = new ArrayList<>();
        List<MatchPairing> unassignedPairings = new ArrayList<>(matchPairings);
        while (unassignedPairings.size() > 0) {
            List<MatchPairing> newRoundPairings = new ArrayList<>();
            Iterator<MatchPairing> unassignedIterator = unassignedPairings.iterator();
            while (unassignedIterator.hasNext() && newRoundPairings.size() < maxRoundPairingsSize) {
                MatchPairing unassignedPairing = unassignedIterator.next();
                if (!containsEitherTeam(newRoundPairings, unassignedPairing)) {
                    newRoundPairings.add(unassignedPairing);
                    unassignedIterator.remove();
                }
            }
            roundPairingsList.add(newRoundPairings);
        }
        return roundPairingsList;
    }
    
    /**
     * Check if any of the MatchPairings in the given list contain either of the teams in the given single MatchPairing.
     * @param matchPairings The list that may or may not contain either team from checkMatchPairing
     * @param checkMatchPairing The match pairing whose teams we are checking for within matchPairings
     * @return True if any of the MatchPairings in matchPairings contain either of the teams in checkMatchPairing
     */
    public static boolean containsEitherTeam(List<MatchPairing> matchPairings, MatchPairing checkMatchPairing) {
        for (MatchPairing matchPairing : matchPairings) {
            if (matchPairing.containsTeam(checkMatchPairing.northTeam()) || matchPairing.containsTeam(checkMatchPairing.southTeam())) {
                return true;
            }
        }
        return false;
    }
    
}
