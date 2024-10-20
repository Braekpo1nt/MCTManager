package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.braekpo1nt.mctmanager.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class RoundManagerTest {
    
    private List<String> teams;
    
    @BeforeEach
    void setUp() {
        teams = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G", "Team H");
        Main.logger().setLevel(Level.OFF);
    }
    
    @Test
    void roundRobin() {
        List<MatchPairing> allMatches = RoundManager.generateRoundRobin(teams);
        Assertions.assertEquals(28, allMatches.size());
        List<List<MatchPairing>> schedule = RoundManager.distributeMatches(allMatches, 5);
        Assertions.assertEquals(7, schedule.size());
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
        
        List<List<MatchPairing>> fourArenasScheduleExclude = RoundManager.generateSchedule(largeTeams, 4, exclude);
        int expectedMatches = (largeTeams.size() * (largeTeams.size() - 1) / 2) - exclude.size();
        int actualMatches =  fourArenasScheduleExclude.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches);
        Assertions.assertEquals(29, fourArenasScheduleExclude.size());
        
        List<List<MatchPairing>> fiveArenasSchedule = RoundManager.generateSchedule(largeTeams, 5);
        expectedMatches = (largeTeams.size() * (largeTeams.size() - 1) / 2);
        actualMatches =  fiveArenasSchedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches);
        Assertions.assertEquals(24, fiveArenasSchedule.size());
        
    }
    
    @Test
    void tenTeamsFiveArenas() {
        List<String> tenTeams = List.of(
                "The Councel", 
                "Aquaholics", 
                "Red Rangers", 
                "Purple Paladins", 
                "Blue Bedtimers", 
                "Lime Cacti", 
                "Green Spartans", 
                "Orange Oni's", 
                "Pink Penguins", 
                "Just the Builders");
        
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(tenTeams, 5);
        int expectedMatches = 45;
        int actualMatches =  schedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches);
        Assertions.assertEquals(9, schedule.size());
        Assertions.assertEquals(5, schedule.get(0).size());
        Assertions.assertEquals(5, schedule.get(1).size());
        Assertions.assertEquals(5, schedule.get(2).size());
        Assertions.assertEquals(5, schedule.get(3).size());
        Assertions.assertEquals(5, schedule.get(4).size());
        Assertions.assertEquals(5, schedule.get(5).size());
        Assertions.assertEquals(5, schedule.get(6).size());
        Assertions.assertEquals(5, schedule.get(7).size());
        Assertions.assertEquals(5, schedule.get(8).size());
    }
    
    @Test
    void testExcludeOneRound() {
        int arenas = 4;
        
        // what the first round would have been if Team H hadn't joined yet
        List<MatchPairing> exclude = List.of(
                new MatchPairing("Team A", "Team B"),
                new MatchPairing("Team C", "Team D"),
                new MatchPairing("Team E", "Team F")
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
    void testExcludeThreeRounds() {
        int arenas = 4;
        
        // what the first 3 rounds would have been if Team H hadn't joined yet
        List<MatchPairing> exclude = List.of(
                new MatchPairing("Team A", "Team B"),
                new MatchPairing("Team C", "Team D"),
                new MatchPairing("Team E", "Team F"),
                new MatchPairing("Team A", "Team C"),
                new MatchPairing("Team B", "Team D"),
                new MatchPairing("Team E", "Team G"),
                new MatchPairing("Team A", "Team D"),
                new MatchPairing("Team B", "Team C"),
                new MatchPairing("Team F", "Team G")
        );
        
        // create a schedule that simulates what should be generated after Team H joins after the first round
        List<List<MatchPairing>> schedulePostJoin = RoundManager.generateSchedule(teams, arenas, exclude);
        List<List<MatchPairing>> fullSchedule = new ArrayList<>(schedulePostJoin.size() + 1);
        // round 1
        fullSchedule.add(List.of(
                new MatchPairing("Team A", "Team B"),
                new MatchPairing("Team C", "Team D"),
                new MatchPairing("Team E", "Team F")
        ));
        // round 2
        fullSchedule.add(List.of(
                new MatchPairing("Team A", "Team C"),
                new MatchPairing("Team B", "Team D"),
                new MatchPairing("Team E", "Team G")
        ));
        // round 3
        fullSchedule.add(List.of(
                new MatchPairing("Team A", "Team D"),
                new MatchPairing("Team B", "Team C"),
                new MatchPairing("Team F", "Team G")
        ));
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
        List<MatchPairing> matchPairings = schedule.stream().flatMap(List::stream).toList();
        Set<MatchPairing> uniqueMatchPairings = new HashSet<>(matchPairings);
        Assertions.assertEquals(uniqueMatchPairings.size(), matchPairings.size(), "Each team should play every other team exactly once");
        
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
        List<MatchPairing> matchPairings = schedule.stream().flatMap(List::stream).toList();
        Set<MatchPairing> uniqueMatchPairings = new HashSet<>(matchPairings);
        Assertions.assertEquals(uniqueMatchPairings.size(), matchPairings.size(), "Each team should play every other team exactly once");
        
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
        List<MatchPairing> matchPairings = schedule.stream().flatMap(List::stream).toList();
        Set<MatchPairing> uniqueMatchPairings = new HashSet<>(matchPairings);
        Assertions.assertEquals(uniqueMatchPairings.size(), matchPairings.size(), "Each team should play every other team exactly once");
        
        // Validate the number of rounds
        int expectedRounds = teams.size() * (teams.size() - 1) / 2;
        Assertions.assertEquals(expectedRounds, schedule.size(), "There should be as many rounds as total matches when only one arena is available.");
    }
    
    @Test
    void testGenerateScheduleWithMinTeams() {
        List<String> minTeams = List.of("Team A", "Team B");
        int arenas = 1;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(minTeams, arenas);
        
        // Validate that there is exactly one match
        Assertions.assertEquals(1, schedule.size(), "There should be exactly one round with two teams.");
        Assertions.assertEquals(1, schedule.getFirst().size(), "There should be exactly one match in the round.");
        Assertions.assertEquals("Team A", schedule.getFirst().getFirst().northTeam(), "First team should be Team A.");
        Assertions.assertEquals("Team B", schedule.getFirst().getFirst().southTeam(), "Second team should be Team B.");
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
    
    @Test
    void testNewTeamJoiningDuringRound1() {
        List<String> teams7 = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G");
        int arenas = 4;
        RoundManager roundManager = new RoundManager(teams7, arenas);
        
        Assertions.assertEquals(List.of(
                new MatchPairing("Team A", "Team B"),
                new MatchPairing("Team C", "Team D"),
                new MatchPairing("Team E", "Team F")
        ), roundManager.getCurrentRound());
        
        // Now we add a new team, "Team H" and regenerate the rounds
        List<String> teams8 = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G", "Team H");
        roundManager.regenerateRounds(teams8, arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(List.of(
                new MatchPairing("Team A", "Team B"),
                new MatchPairing("Team C", "Team D"),
                new MatchPairing("Team E", "Team F")
        ), roundManager.getCurrentRound());
        
        // Now we cycle to the next round
        roundManager.nextRound();
        Assertions.assertEquals(List.of(
                new MatchPairing("Team A", "Team C"),
                new MatchPairing("Team B", "Team D"),
                new MatchPairing("Team E", "Team G"),
                new MatchPairing("Team F", "Team H")
        ), roundManager.getCurrentRound());
        Assertions.assertEquals(2, roundManager.getPlayedRounds() + 1);
    }
    
    @Test
    void testNewTeamJoiningDuringRound3() {
        List<String> teams7 = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G");
        int arenas = 4;
        RoundManager roundManager = new RoundManager(teams7, arenas);
        
        roundManager.nextRound();
        roundManager.nextRound();
        Assertions.assertEquals(3, roundManager.getPlayedRounds() + 1);
        // now we're in round 3
        
        // Now we add a new team, "Team H" and regenerate the rounds
        List<String> teams8 = List.of("Team A", "Team B", "Team C", "Team D", "Team E", "Team F", "Team G", "Team H");
        roundManager.regenerateRounds(teams8, arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(List.of(
                new MatchPairing("Team A", "Team D"),
                new MatchPairing("Team B", "Team C"),
                new MatchPairing("Team F", "Team G")
        ), roundManager.getCurrentRound());
        
        roundManager.nextRound();
        Assertions.assertEquals(List.of(
                new MatchPairing("Team A", "Team E"),
                new MatchPairing("Team B", "Team F"),
                new MatchPairing("Team C", "Team G"),
                new MatchPairing("Team D", "Team H")
        ), roundManager.getCurrentRound());
        Assertions.assertEquals(4, roundManager.getPlayedRounds() + 1);
    }
}
