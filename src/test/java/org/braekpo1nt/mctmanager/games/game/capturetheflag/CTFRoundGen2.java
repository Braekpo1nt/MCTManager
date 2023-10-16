package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class CTFRoundGen2 {
    
    class CTFGame {
        List<String> teams;
        int playedRounds = 0;
        CTFRound currentRound;
        Map<String, Integer> roundsSpentOnDeck;
        Set<MatchPairing> playedMatchPairings;
        /**
         * Stores which teams the players must fight
         */
        Map<String, List<String>> teamsToFight;
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
            List<MatchPairing> matchPairings = new ArrayList<>();
            List<String> sortedTeams = teams.stream().sorted(Comparator.comparing(team -> roundsSpentOnDeck.get(team)).reversed()).toList();
            
            return matchPairings;
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
    void threeTeams() {
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
}
