package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class CTFRoundGen1 {
    
    @Test
    void threeTeamsFirstMatch() {
        List<String> teams = List.of("a", "b", "c");
        Map<String, OnDeckRounds> onDeckRounds = new HashMap<>();
        for (String team : teams) {
            onDeckRounds.put(team, new OnDeckRounds(0, -1));
        }
        List<MatchPairing> playedMatchPairings = new ArrayList<>();
        int numOfArenas = 4;
        List<MatchPairing> firstMatchPairings = chooseNextMatchPairings(onDeckRounds, playedMatchPairings, numOfArenas);
        Assertions.assertEquals(List.of(new MatchPairing("a", "b")), firstMatchPairings);
    
        playedMatchPairings.addAll(firstMatchPairings);
        OnDeckRounds c = onDeckRounds.get("c");
        c.incrementRoundsSpentOnDeck();
        List<MatchPairing> secondMatchPairings = chooseNextMatchPairings(onDeckRounds, playedMatchPairings, numOfArenas);
        Assertions.assertEquals(List.of(new MatchPairing("a", "c")), secondMatchPairings);
    }
    
    @Test
    void threeTeamsSecondMatch() {
        List<String> teams = List.of("a", "b", "c");
        Map<String, OnDeckRounds> onDeckRounds = new HashMap<>();
        for (String team : teams) {
            onDeckRounds.put(team, new OnDeckRounds(0, -1));
        }
        List<MatchPairing> playedMatchPairings = new ArrayList<>();
        int numOfArenas = 4;
        List<MatchPairing> nextMatchPairings = chooseNextMatchPairings(onDeckRounds, playedMatchPairings, numOfArenas);
        Assertions.assertEquals(List.of(new MatchPairing("a", "b")), nextMatchPairings);
    }
    
    /**
     * Chooses the MatchPairings from the unPlayedMatchPairings for the next round (based on the number of arenas)
     * prioritizing teams which have been on-deck the longest
     * @return the match pairings that the next round should have (size will match the number of arenas in the config)
     */
    private List<MatchPairing> chooseNextMatchPairings(Map<String, OnDeckRounds> onDeckRounds, List<MatchPairing> playedMatchPairings, int numOfArenas) {
        List<String> sortedTeams = onDeckRounds.entrySet()
                .stream()
                .sorted(Comparator.comparing((Map.Entry<String, OnDeckRounds> entry) -> entry.getValue().getRoundsSpentOnDeck()).reversed()
                        .thenComparing(entry -> entry.getValue().getLastPlayedRound()))
                .map(Map.Entry::getKey)
                .toList();
        List<MatchPairing> newMatchPairings = new ArrayList<>(numOfArenas);
        List<String> chosenTeams = new ArrayList<>(sortedTeams.size());
        for (int i = 0; i < Math.min(numOfArenas, sortedTeams.size()); i++) {
            String teamA = sortedTeams.get(i);
            if (!chosenTeams.contains(teamA)) {
                chosenTeams.add(teamA);
                for (int j = i+1; j < sortedTeams.size(); j++) {
                    String teamB = sortedTeams.get(j);
                    MatchPairing newMatchPairing = new MatchPairing(teamA, teamB);
                    if (!listContainsMatchPairing(playedMatchPairings, newMatchPairing) 
                            && !listContainsMatchPairing(newMatchPairings, newMatchPairing)
                            && !chosenTeams.contains(teamB)) {
                        newMatchPairings.add(newMatchPairing);
                        chosenTeams.add(teamB);
                        break;
                    }
                }
            }
        }
        return newMatchPairings;
    }
    
    /**
     * @param matchPairings the list to check if it contains matchPairing
     * @param matchPairing the MatchPairing to check if matchPairings contains
     * @return true if matchPairings contains a MatchPairing (agnostic of which team is north or south)
     */
    private boolean listContainsMatchPairing(List<MatchPairing> matchPairings, MatchPairing matchPairing) {
        for (MatchPairing eachMatchPairing : matchPairings) {
            if (eachMatchPairing.containsTeam(matchPairing.northTeam()) && eachMatchPairing.containsTeam(matchPairing.southTeam())) {
                return true;
            }
        }
        return false;
    }
    
}