package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiPredicate;

public class CTFRoundGen2 {
    
    class MockCTFGame extends CaptureTheFlagGame {
        final int numOfArenas;
        List<String> teams;
        CTFRound currentRound;
        RoundManager roundManager;
        /**
         * Set in the constructor for testing purposes. The game simulation will stop after pauseRounds rounds are played, so you can check the state of the game in between rounds. If this value is -1, then all rounds will be played as calculated without restriction.
         */
        int pauseRounds;
        
        /**
         * @param numOfArenas the number of arenas there are
         */
        MockCTFGame(int numOfArenas) {
            super(null, null);
            this.numOfArenas = numOfArenas;
        }
        
        @Override
        public void stop() {
        }
        
        public void start(String... newTeams) {
            start(-1, newTeams);
        }
        
        public void start(int pauseAfterRounds, String... newTeams) {
            this.pauseRounds = pauseAfterRounds;
            teams = new ArrayList<>(newTeams.length);
            for (String team : newTeams) {
                teams.add(team);
            }
//            System.out.println("Start game");
            roundManager = new RoundManager(this, numOfArenas);
            roundManager.start(List.of(newTeams));
        }
        
        public void onTeamJoin(String team) {
            roundManager.onTeamJoin(team);
        }
        
        public void onTeamQuit(String team) {
            roundManager.onTeamQuit(team);
        }
        
        public void resume(int pauseAfterRounds) {
            this.pauseRounds = pauseAfterRounds;
            roundManager.setPlayedRounds(roundManager.getPlayedRounds() - 1); // roundIsOver increases this, but it's already increased, so decrease for consistency
            roundIsOver();
        }
        
        @Override
        public void roundIsOver() {
            if (pauseRounds >= 0) {
                int playedRounds = roundManager.getPlayedRounds();
                if (playedRounds >= pauseRounds) {
                    return;
                }
            }
            roundManager.roundIsOver();
        }
        
        @Override
        public void startNextRound(List<String> participantTeams, List<MatchPairing> roundMatchPairings) {
            currentRound = new CTFRound(this);
            List<String> onDeckTeams = new ArrayList<>(teams);
            onDeckTeams.removeAll(participantTeams);
            currentRound.start(participantTeams, roundMatchPairings, onDeckTeams);
        }
    }
    
    class CTFRound {
        final MockCTFGame ctfGame;
        List<String> teams;
        CTFRound(MockCTFGame ctfGame) {
            this.ctfGame = ctfGame;
        }
        
        public void start(List<String> newTeams, List<MatchPairing> matchPairings, List<String> onDeckTeams) {
            teams = new ArrayList<>(newTeams);
            System.out.printf("Start round with:%s; On-Deck:%s%n", matchPairings, onDeckTeams);
            stop();
        }
        
        public void stop() {
//            System.out.println("Stop round");
            ctfGame.roundIsOver();
        }
    }
    
    @Test
    void teams_3_rounds_1() {
        MockCTFGame ctf = new MockCTFGame(4);
        ctf.start(1, "a", "b", "c");
        
        Assertions.assertEquals(1, ctf.roundManager.getPlayedRounds());
        Assertions.assertEquals(Set.of(
                new MatchPairing("a", "b")
        ), ctf.roundManager.getPlayedMatchPairings());
        Assertions.assertEquals(Map.of(
                "a", 0,
                "b", 0,
                "c", 1
                ), ctf.roundManager.getRoundsSpentOnDeck());
    }
    
    @Test
    void teams_3_rounds_all() {
        MockCTFGame ctf = new MockCTFGame(4);
        ctf.start("a", "b", "c");
        
        Assertions.assertEquals(3, ctf.roundManager.getPlayedRounds());
        assertSetsAreEqual(
                Set.of(
                    new MatchPairing("a", "b"),
                    new MatchPairing("a", "c"),
                    new MatchPairing("b", "c")
                ), 
                ctf.roundManager.getPlayedMatchPairings(), 
                this::matchPairingEquivalent
        );
        Assertions.assertEquals(Map.of(
                "a", 1,
                "b", 1,
                "c", 1
        ), ctf.roundManager.getRoundsSpentOnDeck());
    }
    
    @Test
    void teams_7_arenas_2() {
        MockCTFGame ctf = new MockCTFGame(2);
        ctf.start("black", "grey", "red", "yellow", "blue", "green", "pink");
        System.out.printf("Longest On-Deck Streak: %s%n", ctf.roundManager.longestOnDeckStreak);
        System.out.printf("Total on-deck rounds: %s%n", ctf.roundManager.totalOnDeckRounds);
        Assertions.assertEquals(11, ctf.roundManager.getPlayedRounds());
    }
    
    @Test
    void pause_resume() {
        MockCTFGame ctf = new MockCTFGame(2);
        ctf.start(6, "black", "grey", "red", "yellow", "blue", "green", "pink");
        Assertions.assertEquals(6, ctf.roundManager.getPlayedRounds());
        ctf.resume(8);
        Assertions.assertEquals(8, ctf.roundManager.getPlayedRounds());
        ctf.resume(-1);
        Assertions.assertEquals(11, ctf.roundManager.getPlayedRounds());
    }
    
    @Test
    void teams_7_join() {
        MockCTFGame ctf = new MockCTFGame(2);
        ctf.start(6, "black", "grey", "red", "yellow", "blue", "green", "pink");
        ctf.onTeamJoin("orange");
        ctf.resume(-1);
        Set<MatchPairing> expectedPlayedMatchPairings = createAllMatchPairings("black", "grey", "red", "yellow", "blue", "green", "pink", "orange");
    
        Assertions.assertEquals(14, ctf.roundManager.getPlayedRounds());
        assertSetsAreEqual(expectedPlayedMatchPairings, ctf.roundManager.getPlayedMatchPairings(), this::matchPairingEquivalent);
    }
    
    @Test
    void teams_7_leave_join() {
        MockCTFGame ctf = new MockCTFGame(2);
        String[] teams = {"black", "grey", "red", "yellow", "blue", "green", "pink"};
        ctf.start(3, teams);
        ctf.onTeamQuit("black");
        ctf.resume(5);
        ctf.onTeamJoin("black");
        ctf.resume(-1);
        Set<MatchPairing> expectedPlayedMatchPairings = createAllMatchPairings(teams);
    
        Assertions.assertEquals(11, ctf.roundManager.getPlayedRounds());
        assertSetsAreEqual(expectedPlayedMatchPairings, ctf.roundManager.getPlayedMatchPairings(), this::matchPairingEquivalent);
    }
    
    @Test
    void teams_7_leave_join_late() {
        MockCTFGame ctf = new MockCTFGame(2);
        String[] teams = {"black", "grey", "red", "yellow", "blue", "green", "pink"};
        ctf.start(3, teams);
        ctf.onTeamQuit("black");
        ctf.resume(5);
        ctf.onTeamJoin("black");
        ctf.resume(-1);
        Set<MatchPairing> expectedPlayedMatchPairings = createAllMatchPairings(teams);
        
        Assertions.assertEquals(11, ctf.roundManager.getPlayedRounds());
        assertSetsAreEqual(expectedPlayedMatchPairings, ctf.roundManager.getPlayedMatchPairings(), this::matchPairingEquivalent);
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
        List<MatchPairing> generated = RoundManager.generateMatchPairings(List.of("a", "b", "c"), Collections.emptySet(), 4);
        Assertions.assertEquals(List.of(
                new MatchPairing("a", "b")
        ), generated);
    }
    
    Set<MatchPairing> createAllMatchPairings(String... teams) {
        Set<MatchPairing> matchPairings = new HashSet<>();
        for (int i = 0; i < teams.length; i++) {
            String team1 = teams[i];
            for (int j = i+1; j < teams.length; j++) {
                String team2 = teams[j];
                matchPairings.add(new MatchPairing(team1, team2));
            }
        }
        return matchPairings;
    }
}
