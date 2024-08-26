package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoundManagerTest {
    
    private List<String> teams;
    
    @BeforeEach
    void setUp() {
        teams = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G", "Team H");
    }
    
    @Test
    void testLargeTeamCount() {
        List<String> largeTeams = List.of(
                "A", "B", "C", "D", "E", "F", "G", "H",  
                         "I", "J", "K", "L", "M", "N", "O", "P");
        List<MatchPairing> exclude = List.of(
                new MatchPairing("A", "P"),
                new MatchPairing("B", "O"),
                new MatchPairing("C", "N"),
                new MatchPairing("D", "M")
        );
        int numOfArenas = 4;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(largeTeams, numOfArenas, exclude);
        
        int expectedMatches = (largeTeams.size() * (largeTeams.size() - 1) / 2) - exclude.size();
        int actualMatches =  schedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches);
    }
    
    @Test
    void testExclude() {
        int arenas = 4;
        
        // what the first round would have been if Team H hadn't joined yet
        List<MatchPairing> exclude = List.of(
                new MatchPairing("Team A", "Team G"),
                new MatchPairing("Team B", "Team F"),
                new MatchPairing("Team C", "Team E")
        );
        
        // create a schedule that simulates what should be generated after Team H joins after the first round
        List<List<MatchPairing>> schedulePostJoin = RoundManager.generateSchedule(teams, arenas, exclude);
        List<List<MatchPairing>> fullSchedule = new ArrayList<>(schedulePostJoin.size() + 1);
        fullSchedule.add(exclude);
        fullSchedule.addAll(schedulePostJoin);
        
        int expectedPostJoinMatches = (teams.size() * (teams.size() - 1) / 2) - exclude.size();
        int actualPostJoinMatches = schedulePostJoin.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedPostJoinMatches, actualPostJoinMatches, "Each team should play every other team exactly once, minus the exclude.");
        
        // Validate that all teams play exactly once against each other
        int expectedMatches = teams.size() * (teams.size() - 1) / 2;
        int actualMatches = fullSchedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches, "Each team should play every other team exactly once.");
        
        // Validate that no team is scheduled more than once in the same round
        for (List<MatchPairing> round : fullSchedule) {
            Set<String> teamsInRound = new HashSet<>();
            for (MatchPairing match : round) {
                Assertions.assertTrue(teamsInRound.add(match.northTeam()), "Team " + match.northTeam() + " is double-booked in the round.");
                Assertions.assertTrue(teamsInRound.add(match.southTeam()), "Team " + match.southTeam() + " is double-booked in the round.");
            }
        }
        
        // Validate that no MatchPairing is a duplicate
        List<MatchPairing> flagMatchPairings = fullSchedule.stream().flatMap(List::stream).toList();
        Set<MatchPairing> matchPairingSet = new HashSet<>(flagMatchPairings);
        Assertions.assertEquals(flagMatchPairings.size(), matchPairingSet.size());
    }
    
    @Test
    void testGenerateScheduleWithEvenTeams() {
        int arenas = 4;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(teams, arenas);
        
        // Validate that all teams play exactly once against each other
        int expectedMatches = teams.size() * (teams.size() - 1) / 2;
        int actualMatches = schedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches, "Each team should play every other team exactly once.");
        
        // Validate the number of rounds
        int expectedRounds = teams.size() - 1;
        Assertions.assertEquals(expectedRounds, schedule.size(), "There should be the correct number of rounds.");
        
        // Validate the number of arenas used
        for (List<MatchPairing> round : schedule) {
            Assertions.assertTrue(round.size() <= arenas, "No round should exceed the number of available arenas.");
        }
    }
    
    @Test
    void testGenerateScheduleWithOddNumberOfTeams() {
        List<String> oddTeams = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G");
        int arenas = 4;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(oddTeams, arenas);
        
        // Validate that all teams play exactly once against each other
        int expectedMatches = oddTeams.size() * (oddTeams.size() - 1) / 2;
        int actualMatches = schedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches, "Each team should play every other team exactly once.");
        
        // Validate the number of rounds
        int expectedRounds = oddTeams.size();
        Assertions.assertEquals(expectedRounds, schedule.size(), "There should be the correct number of rounds.");
        
        // Validate the number of arenas used
        for (List<MatchPairing> round : schedule) {
            Assertions.assertTrue(round.size() <= arenas, "No round should exceed the number of available arenas.");
        }
    }
    
    @Test
    void testGenerateScheduleWithOneArena() {
        int arenas = 1;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(teams, arenas);
        
        // Validate that all teams play exactly once against each other
        int expectedMatches = teams.size() * (teams.size() - 1) / 2;
        int actualMatches = schedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches, "Each team should play every other team exactly once.");
        
        // Validate the number of rounds
        int expectedRounds = expectedMatches;
        Assertions.assertEquals(expectedRounds, schedule.size(), "There should be as many rounds as total matches when only one arena is available.");
    }
    
    @Test
    void testGenerateScheduleWithMinTeams() {
        List<String> minTeams = List.of("Team A", "Team B");
        int arenas = 1;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(minTeams, arenas);
        
        // Validate that there is exactly one match
        Assertions.assertEquals(1, schedule.size(), "There should be exactly one round with two teams.");
        Assertions.assertEquals(1, schedule.get(0).size(), "There should be exactly one match in the round.");
        Assertions.assertEquals("Team A", schedule.get(0).get(0).northTeam(), "First team should be Team A.");
        Assertions.assertEquals("Team B", schedule.get(0).get(0).southTeam(), "Second team should be Team B.");
    }
    
    @Test
    void testNoDoubleBookingInRounds() {
        int arenas = 4;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(teams, arenas);
        
        // Validate that no team is scheduled more than once in the same round
        for (List<MatchPairing> round : schedule) {
            Set<String> teamsInRound = new HashSet<>();
            for (MatchPairing match : round) {
                Assertions.assertTrue(teamsInRound.add(match.northTeam()), "Team " + match.northTeam() + " is double-booked in the round.");
                Assertions.assertTrue(teamsInRound.add(match.southTeam()), "Team " + match.southTeam() + " is double-booked in the round.");
            }
        }
    }
    
}
