package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiPredicate;

public class CTFRoundGen2 {
    
    class CTFGame {
        List<String> teams;
        int playedRounds = 0;
        CTFRound currentRound;
        Map<String, Integer> roundsSpentOnDeck;
        Set<MatchPairing> playedMatchPairings;
        final int numOfArenas;
        /**
         * Set in the constructor for testing purposes. The game simulation will stop after maxRounds rounds are played so you can check the state of the game in between rounds. If this value is -1, then all rounds will be played as calculated without restriction.
         */
        final int maxRounds;
    
        /**
         * @param numOfArenas the number of arenas there are
         * @param maxRounds the maximum number of rounds to play (-1 to play all rounds)
         */
        CTFGame(int numOfArenas, int maxRounds) {
            this.numOfArenas = numOfArenas;
            this.maxRounds = maxRounds;
        }
    
        /**
         * @param numOfArenas the number of arenas there are
         */
        CTFGame(int numOfArenas) {
            this.numOfArenas = numOfArenas;
            // play all rounds
            this.maxRounds = -1;
        }
        
        public void start(String... newTeams) {
            teams = new ArrayList<>(List.of(newTeams));
            roundsSpentOnDeck = new HashMap<>();
            for (String team : teams) {
                roundsSpentOnDeck.put(team, 0);
            }
            playedMatchPairings = new HashSet<>();
            System.out.println("Start game");
            startNextRound();
        }
        
        public void startNextRound() {
            currentRound = new CTFRound(this);
            List<MatchPairing> roundMatchPairings = generateRoundMatchPairings();
            List<String> participantTeams = getTeamsFromMatchPairings(roundMatchPairings);
            List<String> onDeckTeams = new ArrayList<>();
            for (String team : teams) {
                if (!participantTeams.contains(team)) {
                    onDeckTeams.add(team);
                }
            }
            for (String onDeckTeam : onDeckTeams) {
                int oldRoundsSpentOnDeck = roundsSpentOnDeck.get(onDeckTeam);
                roundsSpentOnDeck.put(onDeckTeam, oldRoundsSpentOnDeck + 1);
            }
            playedMatchPairings.addAll(roundMatchPairings);
            currentRound.start(participantTeams, roundMatchPairings, onDeckTeams);
        }
    
        private List<String> getTeamsFromMatchPairings(List<MatchPairing> matchPairings) {
            Set<String> teams = new HashSet<>(matchPairings.size()*2);
            for (MatchPairing matchPairing : matchPairings) {
                teams.add(matchPairing.northTeam());
                teams.add(matchPairing.southTeam());
            }
            return teams.stream().toList();
        }
        
        private List<MatchPairing> generateRoundMatchPairings() {
            List<String> sortedTeams = teams.stream().sorted(Comparator.comparing(team -> roundsSpentOnDeck.get(team)).reversed()).toList();
            return generateMatchPairings(sortedTeams, playedMatchPairings, numOfArenas);
        }
        
        public void roundIsOver() {
            playedRounds++;
            if (thereAreRoundsLeft()) {
                startNextRound();
                return;
            }
            stop();
        }
    
        private boolean thereAreRoundsLeft() {
            if (maxRounds >= 0) {
                return playedRounds < maxRounds;
            }
            return false;
        }
        
        public void stop() {
            System.out.println("Stop game");
        }
        
    }
    
    class CTFRound {
        final CTFGame ctfGame;
        List<String> teams;
        CTFRound(CTFGame ctfGame) {
            this.ctfGame = ctfGame;
        }
        
        public void start(List<String> newTeams, List<MatchPairing> matchPairings, List<String> onDeckTeams) {
            teams = new ArrayList<>(newTeams);
            System.out.printf("Start with Pairs:%s; Deck:%s%n", matchPairings, onDeckTeams);
            stop();
        }
        
        public void stop() {
            System.out.println("Stop round");
            ctfGame.roundIsOver();
        }
    }
    
    @Test
    void teams_3_rounds_1() {
        CTFGame ctf = new CTFGame(4, 1);
        ctf.start("a", "b", "c");
        
        Assertions.assertEquals(1, ctf.playedRounds);
        Assertions.assertEquals(Set.of(
                new MatchPairing("a", "b")
        ), ctf.playedMatchPairings);
        Assertions.assertEquals(Map.of(
                "a", 0,
                "b", 0,
                "c", 1
                ), ctf.roundsSpentOnDeck);
    }
    
    @Test
    void teams_3_rounds_all() {
        CTFGame ctf = new CTFGame(4);
        ctf.start("a", "b", "c");
        
        Assertions.assertEquals(1, ctf.playedRounds);
        assertSetsAreEqual(
                Set.of(
                    new MatchPairing("a", "b"),
                    new MatchPairing("a", "c"),
                    new MatchPairing("b", "c")
                ), 
                ctf.playedMatchPairings, 
                this::matchPairingEquivalent
        );
        Assertions.assertEquals(Map.of(
                "a", 1,
                "b", 1,
                "c", 1
        ), ctf.roundsSpentOnDeck);
    }
    
    @Test
    void testCompareSets() {
        assertSetsAreEqual(
                Set.of(
                        new MatchPairing("a", "b"),
                        new MatchPairing("a", "c"),
                        new MatchPairing("b", "c")
                ),
                Set.of(
                        new MatchPairing("a", "c"),
                        new MatchPairing("a", "b"),
                        new MatchPairing("c", "b")
                ),
                this::matchPairingEquivalent
        );
    }
    
    boolean matchPairingEquivalent(MatchPairing a, MatchPairing b) {
        return a.isEquivalent(b);
    }
    
    <T> void assertSetsAreEqual(Set<T> set1, Set<T> set2, BiPredicate<T, T> equalityFunction) {
        if (set1.size() != set2.size()) {
            Assertions.fail(String.format("Expected: %s but was: %s", set1, set2));
            return;
        }
        
        for (T item1 : set1) {
            boolean found = false;
            
            for (T item2 : set2) {
                if (equalityFunction.test(item1, item2)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                Assertions.fail(String.format("Expected: %s but was: %s", set1, set2));
                return;
            }
        }
        
        Assertions.assertTrue(true);
    }
    
    @Test
    void testGenerateMatchPairings() {
        List<MatchPairing> generated = generateMatchPairings(List.of("a", "b", "c"), Collections.emptySet(), 4);
        Assertions.assertEquals(List.of(
                new MatchPairing("a", "b")
        ), generated);
    }
    
    List<MatchPairing> generateMatchPairings(
            List<String> sortedTeams, Set<MatchPairing> playedMatchPairings, int numOfArenas) {
        List<MatchPairing> result = new ArrayList<>();
        Set<String> teamsUsed = new HashSet<>();
        
        for (int i = 0; i < sortedTeams.size() - 1 && result.size() < numOfArenas; i++) {
            String team1 = sortedTeams.get(i);
            if (!teamsUsed.contains(team1)) {
                for (int j = i + 1; j < sortedTeams.size() && result.size() < numOfArenas; j++) {
                    String team2 = sortedTeams.get(j);
                    if (!teamsUsed.contains(team2)) {
                        MatchPairing newPairing = new MatchPairing(team1, team2);
                        
                        // Check if the new pairing is not equivalent to any in playedMatchPairings
                        boolean isUnique = true;
                        for (MatchPairing playedPairing : playedMatchPairings) {
                            if (newPairing.isEquivalent(playedPairing)) {
                                isUnique = false;
                                break;
                            }
                        }
                        
                        if (isUnique) {
                            result.add(newPairing);
                            teamsUsed.add(team1);
                            teamsUsed.add(team2);
                            break;  // Exit the inner loop after a successful pairing
                        }
                    }
                }
            }
        }
    
        return result;
    }
}
