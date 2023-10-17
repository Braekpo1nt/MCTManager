package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import com.google.common.base.Preconditions;
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
        Map<String, List<String>> teamsToFight;
        final int numOfArenas;
        /**
         * Set in the constructor for testing purposes. The game simulation will stop after pauseRounds rounds are played, so you can check the state of the game in between rounds. If this value is -1, then all rounds will be played as calculated without restriction.
         */
        int pauseRounds;
        // reporting
        Map<String, Integer> longestOnDeckStreak; 
        Map<String, Integer> onDeckStreak;
        List<String> lastOnDeck;
        Map<String, Integer> totalOnDeckRounds; 
        // reporting
        
        /**
         * @param numOfArenas the number of arenas there are
         */
        CTFGame(int numOfArenas) {
            this.numOfArenas = numOfArenas;
        }
        
        public void start(String... newTeams) {
            start(-1, newTeams);
        }
        
        public void start(int pauseAfterRounds, String... newTeams) {
            this.pauseRounds = pauseAfterRounds;
            teams = new ArrayList<>(newTeams.length);
            roundsSpentOnDeck = new HashMap<>();
            teamsToFight = new HashMap<>();
            // reporting
            longestOnDeckStreak = new HashMap<>();
            onDeckStreak = new HashMap<>();
            lastOnDeck = new ArrayList<>();
            totalOnDeckRounds = new HashMap<>();
            // reporting
            for (String team : newTeams) {
                teams.add(team);
                roundsSpentOnDeck.put(team, 0);
                List<String> opposingTeams = new ArrayList<>(List.of(newTeams));
                opposingTeams.remove(team);
                teamsToFight.put(team, opposingTeams);
                // reporting
                longestOnDeckStreak.put(team, 0);
                onDeckStreak.put(team, 0);
                totalOnDeckRounds.put(team, 0);
                // reporting
            }
            playedMatchPairings = new HashSet<>();
//            System.out.println("Start game");
            startNextRound();
        }
        
        public void resume(int pauseAfterRounds) {
            this.pauseRounds = pauseAfterRounds;
            playedRounds--; // roundIsOver increases this, but it's already increased, so decrease for consistency
            roundIsOver();
        }
        
        public void onTeamQuit(String team) {
            Preconditions.checkState(teams.contains(team), "tried to quit a team that was not in the game");
            teams.remove(team);
            System.out.printf("%s quit the game\n", team);
        }
        
        public void onTeamJoin(String team) {
            Preconditions.checkState(!teams.contains(team), "tried to join a team that was already in the game");
            if (teamShouldRejoin(team)) {
                onTeamRejoin(team);
                return;
            }
            teams.add(team);
            int maxRoundsOnDeck = roundsSpentOnDeck.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            roundsSpentOnDeck.put(team, maxRoundsOnDeck); // give them the same sorting priority as the player who has spent the longest time on-deck, to help prevent a string of matches with just "<new team> vs <a single team at a time>"
            List<String> opposingTeams = new ArrayList<>(teamsToFight.keySet());
            teamsToFight.put(team, opposingTeams);
            for (String opposingTeam : opposingTeams) {
                List<String> value = teamsToFight.get(opposingTeam);
                value.add(team);
            }
            // reporting
            longestOnDeckStreak.putIfAbsent(team, 0);
            onDeckStreak.putIfAbsent(team, 0);
            totalOnDeckRounds.putIfAbsent(team, roundsSpentOnDeck.get(team));
            // reporting
            System.out.printf("%s joined the game\n", team);
        }
        
        /**
         * @param team the team
         * @return true if the team was in the game then left previously and should thus rejoin
         */
        private boolean teamShouldRejoin(String team) {
            return teamsToFight.containsKey(team);
        }
    
        private void onTeamRejoin(String team) {
            teams.add(team);
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
            for (MatchPairing roundMP : roundMatchPairings) {
                List<String> northTTF = teamsToFight.get(roundMP.northTeam());
                northTTF.remove(roundMP.southTeam());
                List<String> southTTF = teamsToFight.get(roundMP.southTeam());
                southTTF.remove(roundMP.northTeam());
            }
            // reporting
            for (String team : onDeckTeams) {
                int old = totalOnDeckRounds.get(team);
                totalOnDeckRounds.put(team, old+1);
                if (lastOnDeck.contains(team)) {
                    int streak = onDeckStreak.get(team);
                    streak++;
                    if (streak > longestOnDeckStreak.get(team)) {
                        longestOnDeckStreak.put(team, streak);
                    }
                } else {
                    onDeckStreak.put(team, 1);
                    if (longestOnDeckStreak.get(team) < 1) {
                        longestOnDeckStreak.put(team, 1);
                    }
                }
            }
            for (String team : participantTeams) {
                onDeckStreak.put(team, 0);
            }
            lastOnDeck = new ArrayList<>(onDeckTeams);
            // reporting
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
            List<String> sortedTeams = teams.stream().sorted(Comparator.<String, Integer>
                            comparing(team -> teamsToFight.get(team).size(), Comparator.reverseOrder())
                            .thenComparing(roundsSpentOnDeck::get, Comparator.reverseOrder())
            ).toList();
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
            boolean roundsAreLeft = false;
            for (List<String> value : teamsToFight.values()) {
                if (!value.isEmpty()) {
                    roundsAreLeft = true;
                    break;
                }
            }
            if (pauseRounds >= 0) {
                if (roundsAreLeft) {
                    if (playedRounds >= pauseRounds) {
                        System.out.println("Paused after round " + playedRounds);
                        return false;
                    }
                }
            }
            return roundsAreLeft;
        }
        
        public void stop() {
//            System.out.println("Stop game");
        }
        
        static int calculateRounds(int numOfTeams, int numOfArenas) {
            return ((int) Math.ceil((numOfTeams * (numOfTeams - 1) / 2.0) / numOfArenas));
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
        CTFGame ctf = new CTFGame(4);
        ctf.start(1, "a", "b", "c");
        
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
        
        Assertions.assertEquals(3, ctf.playedRounds);
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
    void teams_7_arenas_2() {
        CTFGame ctf = new CTFGame(2);
        ctf.start("black", "grey", "red", "yellow", "blue", "green", "pink");
        System.out.printf("Longest On-Deck Streak: %s%n", ctf.longestOnDeckStreak);
        System.out.printf("Total on-deck rounds: %s%n", ctf.totalOnDeckRounds);
        Assertions.assertEquals(11, ctf.playedRounds);
    }
    
    @Test
    void pause_resume() {
        CTFGame ctf = new CTFGame(2);
        ctf.start(6, "black", "grey", "red", "yellow", "blue", "green", "pink");
        Assertions.assertEquals(6, ctf.playedRounds);
        ctf.resume(8);
        Assertions.assertEquals(8, ctf.playedRounds);
        ctf.resume(-1);
        Assertions.assertEquals(11, ctf.playedRounds);
    }
    
    @Test
    void teams_7_join() {
        CTFGame ctf = new CTFGame(2);
        ctf.start(6, "black", "grey", "red", "yellow", "blue", "green", "pink");
        System.out.printf("Longest On-Deck Streak: %s%n", ctf.longestOnDeckStreak);
        System.out.printf("Total on-deck rounds: %s%n", ctf.totalOnDeckRounds);
        ctf.onTeamJoin("orange");
        ctf.resume(-1);
        System.out.printf("Longest On-Deck Streak: %s%n", ctf.longestOnDeckStreak);
        System.out.printf("Total on-deck rounds: %s%n", ctf.totalOnDeckRounds);
        Assertions.assertEquals(14, ctf.playedRounds);
    }
    
    @Test
    void teams_7_leave_join() {
        CTFGame ctf = new CTFGame(2);
        ctf.start(3, "black", "grey", "red", "yellow", "blue", "green", "pink");
        ctf.onTeamQuit("black");
        ctf.resume(5);
        ctf.onTeamJoin("black");
        ctf.resume(-1);
        Assertions.assertEquals(11, ctf.playedRounds);
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
