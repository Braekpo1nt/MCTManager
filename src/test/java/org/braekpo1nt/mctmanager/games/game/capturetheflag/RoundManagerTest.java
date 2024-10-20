package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.braekpo1nt.mctmanager.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.logging.Level;

public class RoundManagerTest {
    
    private List<String> teams;
    
    @BeforeEach
    void setUp() {
        teams = List.of("A", "B", "C", "D", "E", "F", "G", "H");
        Main.logger().setLevel(Level.OFF);
    }
    
    @Test
    void roundRobin8Teams5Arenas() {
        List<MatchPairing> allMatches = RoundManager.generateRoundRobin(teams);
        Assertions.assertEquals(28, allMatches.size());
        List<List<MatchPairing>> schedule = RoundManager.distributeMatches(allMatches, 5);
        Assertions.assertEquals(7, schedule.size());
    }
    
    @Test
    void roundRobin8Teams4Arenas() {
        List<MatchPairing> allMatches = RoundManager.generateRoundRobin(teams);
        Assertions.assertEquals(28, allMatches.size());
        List<List<MatchPairing>> schedule = RoundManager.distributeMatches(allMatches, 4);
        Assertions.assertEquals(7, schedule.size());
    }
    
    @Test
    void roundRobin7Teams5Arenas() {
        List<MatchPairing> allMatches = RoundManager.generateRoundRobin(List.of("A", "B", "C", "D", "E", "F", "G"));
        Assertions.assertEquals(28, allMatches.size());
        List<List<MatchPairing>> schedule5 = RoundManager.distributeMatches(allMatches, 5);
        Assertions.assertEquals(7, schedule5.size());
    }
    
    @Test
    void roundRobin7Teams4Arenas() {
        List<MatchPairing> allMatches = RoundManager.generateRoundRobin(List.of("A", "B", "C", "D", "E", "F", "G"));
        Assertions.assertEquals(28, allMatches.size());
        List<List<MatchPairing>> schedule5 = RoundManager.distributeMatches(allMatches, 4);
        Assertions.assertEquals(7, schedule5.size());
    }
    
    @Test
    void test16Teams5Arenas() {
        List<String> largeTeams = List.of(
                "A", "B", "C", "D", "E", "F", "G", "H",  
                         "I", "J", "K", "L", "M", "N", "O", "P");
        
        List<List<MatchPairing>> fiveArenasSchedule = RoundManager.distributeMatches(RoundManager.generateRoundRobin(largeTeams), 5);
        int expectedMatches = (largeTeams.size() * (largeTeams.size() - 1) / 2);
        int actualMatches =  fiveArenasSchedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches);
        Assertions.assertEquals(24, fiveArenasSchedule.size());
        
    }
    
    @Test
    void test16Teams4Arenas() {
        List<String> largeTeams = List.of(
                "A", "B", "C", "D", "E", "F", "G", "H",
                "I", "J", "K", "L", "M", "N", "O", "P");
        
        List<List<MatchPairing>> fiveArenasSchedule = RoundManager.distributeMatches(RoundManager.generateRoundRobin(largeTeams), 4);
        int expectedMatches = (largeTeams.size() * (largeTeams.size() - 1) / 2);
        int actualMatches =  fiveArenasSchedule.stream().mapToInt(List::size).sum();
        Assertions.assertEquals(expectedMatches, actualMatches);
        Assertions.assertEquals(30, fiveArenasSchedule.size());
        
    }
    
    @Test
    void test16Teams4ArenasExcludeFirst() {
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
        
        List<List<MatchPairing>> schedule = RoundManager.distributeMatches(RoundManager.generateRoundRobin(tenTeams), 5);
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
                new MatchPairing("A", "B"),
                new MatchPairing("C", "D"),
                new MatchPairing("E", "F")
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
                Assertions.assertTrue(teamsInRound.add(match.northTeam()), "" + match.northTeam() + " is double-booked in the round.");
                Assertions.assertTrue(teamsInRound.add(match.southTeam()), "" + match.southTeam() + " is double-booked in the round.");
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
                new MatchPairing("A", "B"),
                new MatchPairing("C", "D"),
                new MatchPairing("E", "F"),
                new MatchPairing("A", "C"),
                new MatchPairing("B", "D"),
                new MatchPairing("E", "G"),
                new MatchPairing("A", "D"),
                new MatchPairing("B", "C"),
                new MatchPairing("F", "G")
        );
        
        // create a schedule that simulates what should be generated after Team H joins after the first round
        List<List<MatchPairing>> schedulePostJoin = RoundManager.generateSchedule(teams, arenas, exclude);
        List<List<MatchPairing>> fullSchedule = new ArrayList<>(schedulePostJoin.size() + 1);
        // round 1
        fullSchedule.add(List.of(
                new MatchPairing("A", "B"),
                new MatchPairing("C", "D"),
                new MatchPairing("E", "F")
        ));
        // round 2
        fullSchedule.add(List.of(
                new MatchPairing("A", "C"),
                new MatchPairing("B", "D"),
                new MatchPairing("E", "G")
        ));
        // round 3
        fullSchedule.add(List.of(
                new MatchPairing("A", "D"),
                new MatchPairing("B", "C"),
                new MatchPairing("F", "G")
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
                Assertions.assertTrue(teamsInRound.add(match.northTeam()), "" + match.northTeam() + " is double-booked in the round.");
                Assertions.assertTrue(teamsInRound.add(match.southTeam()), "" + match.southTeam() + " is double-booked in the round.");
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
        List<String> oddTeams = List.of("A", "B", "C", "D", "E", "F", "G");
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
        List<String> minTeams = List.of("A", "B");
        int arenas = 1;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(minTeams, arenas);
        
        // Validate that there is exactly one match
        Assertions.assertEquals(1, schedule.size(), "There should be exactly one round with two teams.");
        Assertions.assertEquals(1, schedule.getFirst().size(), "There should be exactly one match in the round.");
        Assertions.assertEquals("A", schedule.getFirst().getFirst().northTeam(), "First team should be Team A.");
        Assertions.assertEquals("B", schedule.getFirst().getFirst().southTeam(), "Second team should be Team B.");
    }
    
    @Test
    void testNoDoubleBookingInRounds() {
        int arenas = 4;
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(teams, arenas);
        
        // Validate that no team is scheduled more than once in the same round
        for (List<MatchPairing> round : schedule) {
            Set<String> teamsInRound = new HashSet<>();
            for (MatchPairing match : round) {
                Assertions.assertTrue(teamsInRound.add(match.northTeam()), "" + match.northTeam() + " is double-booked in the round.");
                Assertions.assertTrue(teamsInRound.add(match.southTeam()), "" + match.southTeam() + " is double-booked in the round.");
            }
        }
    }
    
    @Test
    void testExclude() {
        List<String> teams = List.of("A", "B", "C", "D", "E", "F", "G");
        List<MatchPairing> exclude = List.of(
                new MatchPairing(RoundManager.BYE, "A"),
                new MatchPairing("B", "G"),
                new MatchPairing("C", "F"),
                new MatchPairing("D", "E")
        );
        List<List<MatchPairing>> schedule = RoundManager.generateSchedule(teams, 4, exclude);
        Assertions.assertEquals(List.of(
                new MatchPairing("G", "A"),
//                new MatchPairing(RoundManager.BYE, "F"),
                new MatchPairing("E", "B"),
                new MatchPairing("D", "C")
        ), schedule.getFirst());
    }
    
    @Test
    void testNewTeamJoiningDuringRound1() {
        List<String> teams7 = List.of("A", "B", "C", "D", "E", "F", "G");
        int arenas = 4;
        RoundManager roundManager = new RoundManager(teams7, arenas);
        List<MatchPairing> expectedRoundOneMatches = List.of(
//                new MatchPairing("A", RoundManager.BYE),
                new MatchPairing("B", "G"),
                new MatchPairing("C", "F"),
                new MatchPairing("D", "E")
        );
        
        Assertions.assertEquals(expectedRoundOneMatches, roundManager.getCurrentRound());
        
        // Now we add a new team, "H" and regenerate the rounds
        List<String> teams8 = List.of("A", "B", "C", "D", "E", "F", "G", "H");
        roundManager.regenerateRounds(teams8, arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(expectedRoundOneMatches, roundManager.getCurrentRound());
        
        // Now we cycle to the next round
        roundManager.nextRound();
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "H"),
                new MatchPairing("E", "B"),
                new MatchPairing("D", "C"),
                new MatchPairing("G", "F")
        ), roundManager.getCurrentRound());
        Assertions.assertEquals(2, roundManager.getPlayedRounds() + 1);
    }
    
    @Test
    void testNewTeamJoiningDuringRound3() {
        List<String> teams7 = List.of("A", "B", "C", "D", "E", "F", "G");
        int arenas = 4;
        RoundManager roundManager = new RoundManager(teams7, arenas);
        // now we're in round 1
        Assertions.assertEquals(List.of(
//                new MatchPairing("A", RoundManager.BYE),
                new MatchPairing("B", "G"),
                new MatchPairing("C", "F"),
                new MatchPairing("D", "E")
        ), roundManager.getCurrentRound());
        
        roundManager.nextRound();
        // now we're in round 2
        Assertions.assertEquals(List.of(
                new MatchPairing("G", "A"),
//                new MatchPairing("F", RoundManager.BYE),
                new MatchPairing("E", "B"),
                new MatchPairing("D", "C")
        ), roundManager.getCurrentRound());
        
        roundManager.nextRound();
        // now we're in round 3
        Assertions.assertEquals(3, roundManager.getPlayedRounds() + 1);
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "F"),
                new MatchPairing("G", "E"),
//                new MatchPairing(RoundManager.BYE, "D"),
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        
        // Now we add a new team, "H" and regenerate the rounds
        List<String> teams8 = List.of("A", "B", "C", "D", "E", "F", "G", "H");
        roundManager.regenerateRounds(teams8, arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "F"),
                new MatchPairing("G", "E"),
//                new MatchPairing(RoundManager.BYE, "D"),
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        
        roundManager.nextRound();
        // now we're in round 4
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "H"),
                new MatchPairing("D", "F"),
                new MatchPairing("C", "G")
        ), roundManager.getCurrentRound());
        Assertions.assertEquals(4, roundManager.getPlayedRounds() + 1);
    }
    
    @Test
    void test2Teams1Joining() {
        int arenas = 5;
        RoundManager roundManager = new RoundManager(List.of("A", "B"), arenas);
        // now we're in round 1
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "B")
        ), roundManager.getCurrentRound());
        
        // Now we add a new team, "C" and regenerate the rounds
        roundManager.regenerateRounds(List.of("A", "B", "C"), arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "B")
        ), roundManager.getCurrentRound());
        
        roundManager.nextRound();
        // now we're in round 2
        Assertions.assertEquals(List.of(
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        Assertions.assertEquals(2, roundManager.getPlayedRounds() + 1);
        
        roundManager.nextRound();
        // now we're in round 3
        Assertions.assertEquals(List.of(
                new MatchPairing("C", "A")
        ), roundManager.getCurrentRound());
        
        Assertions.assertFalse(roundManager.hasNextRound(), "round manager should not have more rounds");
    }
    
    @Test
    void test3Teams1OnDeckTeamQuits() {
        int arenas = 5;
        RoundManager roundManager = new RoundManager(List.of("A", "B", "C"), arenas);
        // now we're in round 1
        Assertions.assertEquals(List.of(
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        
        // Now team "A" quits
        roundManager.regenerateRounds(List.of("B", "C"), arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(List.of(
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        
        Assertions.assertFalse(roundManager.hasNextRound(), "there shouldn't be any more rounds");
    }
    
    @Test
    void test3Teams1ParticipantTeamQuits() {
        int arenas = 5;
        RoundManager roundManager = new RoundManager(List.of("A", "B", "C"), arenas);
        // now we're in round 1
        Assertions.assertEquals(List.of(
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        
        // Now team "B" quits
        roundManager.regenerateRounds(List.of("A", "C"), arenas);
        // Assert that the current round hasn't changed
        Assertions.assertEquals(List.of(
                new MatchPairing("B", "C")
        ), roundManager.getCurrentRound());
        
        roundManager.nextRound();
        // now we're in round 2
        Assertions.assertEquals(List.of(
                new MatchPairing("A", "C")
        ), roundManager.getCurrentRound());
        
        Assertions.assertFalse(roundManager.hasNextRound(), "there shouldn't be any more rounds");
    }
}
