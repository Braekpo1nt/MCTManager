package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RoundManager {
    
    /**
     * The schedule handled by this round. Each element of the outer list
     * is a round. 
     */
    private @NotNull List<List<MatchPairing>> schedule;
    /**
     * the played {@link MatchPairing}s this game. Useful for when new teams join, we can re-generate matches
     * and remove already played ones. In the future, also could be useful for tracking wins, but would
     * need to be a map, and would need to be tracked by round. 
     */
    private final List<MatchPairing> played;
    /**
     * How many rounds have been played. Useful for outputting the current round to the players. 
     * equivalent to currentRoundIndex, unless a new team joins mid-game then they get out of sync
     * and this is more accurate for user output
     */
    private int playedRounds = 0;
    private int maxRounds;
    
    /**
     * The index of the current round
     */
    private int currentRoundIndex = 0;
    private @NotNull List<MatchPairing> currentRound;
    
    /**
     * Used only for checking if a given teamId is contained in this {@link RoundManager} or not
     * @see #containsTeamId(String) 
     */
    private final @NotNull Set<String> containedTeamIds;
    
    /**
     * @param teamId the teamId to check
     * @param round the round to check
     * @return the opposite teamId of the given teamId in the given round (if they are present), null if the given team is on-deck
     */
    public static @Nullable String getOppositeTeam(String teamId, List<MatchPairing> round) {
        MatchPairing matchPairing = getMatchPairing(teamId, round);
        if (matchPairing == null) {
            return null;
        }
        return matchPairing.oppositeTeam(teamId);
    }
    
    public static MatchPairing getMatchPairing(String teamId, List<MatchPairing> round) {
        for (MatchPairing matchPairing : round) {
            if (matchPairing.containsTeam(teamId)) {
                return matchPairing;
            }
        }
        return null;
    }
    
    private static void logSchedule(List<List<MatchPairing>> schedule) {
        for (int i = 0; i < schedule.size(); i++) {
            List<MatchPairing> round = schedule.get(i);
            Main.logger().info(String.format("Round %d:", i));
            for (MatchPairing matchPairing : round) {
                Main.logger().info(String.format("- %s", matchPairing));
            }
        }
    }
    
    /**
     * Initialize the RoundManager to handle rounds for a game
     * @param teamIds the teamIds of the teams in the round (must have at least 2 entries)
     * @param numOfArenas the number of arenas (must be greater than 0)
     */
    public RoundManager(@NotNull List<@NotNull String> teamIds, int numOfArenas) {
        Preconditions.checkArgument(teamIds.size() >= 2, "There must be at least two teamIds (got %s)", teamIds.size());
        Preconditions.checkArgument(numOfArenas > 0, "there must be at least 1 arena");
        this.containedTeamIds = new HashSet<>(teamIds);
        Preconditions.checkArgument(this.containedTeamIds.size() == teamIds.size(), "Duplicate teamId found in teamIds %s", teamIds.toString());
        this.schedule = generateSchedule(teamIds, numOfArenas);
        Preconditions.checkArgument(!schedule.isEmpty(), "Generated rounds were empty, teamIds: %s, numOfArenas: %s", teamIds, numOfArenas);
        currentRound = this.schedule.getFirst();
        played = new ArrayList<>(currentRound);
        playedRounds = 0;
        maxRounds = schedule.size();
        logSchedule(schedule);
    }
    
    /**
     * Regenerates the rounds using the given set of teams. Previously played matches will not be re-added.
     * Handy for when a new team joins mid-game and needs to be mixed into the rounds. 
     * @param teamIds the teamIds of the teams in the round (must have at least 2 entries)
     * @param numOfArenas the number of arenas (must be greater than 0)
     */
    public void regenerateRounds(@NotNull List<@NotNull String> teamIds, int numOfArenas) {
        Preconditions.checkArgument(teamIds.size() >= 2, "There must be at least two teamIds (got %s)", teamIds.size());
        Preconditions.checkArgument(numOfArenas > 0, "there must be at least 1 arena");
        this.containedTeamIds.addAll(teamIds);
        Preconditions.checkArgument(this.containedTeamIds.size() == teamIds.size(), "Duplicate teamId found in teamIds %s", teamIds.toString());
        this.schedule = generateSchedule(teamIds, numOfArenas, played);
        currentRoundIndex = -1;
        maxRounds = playedRounds + schedule.size() + 1;
        logSchedule(schedule);
    }
    
    /**
     * @return the number of rounds that have been played. Starts at 0. 
     */
    public int getPlayedRounds() {
        return playedRounds;
    }
    
    /**
     * @return the total number of rounds that will be played during the game
     */
    public int getMaxRounds() {
        return maxRounds;
    }
    
    /**
     * @param teamId the teamId to check for
     * @return true if the given teamId is contained in this {@link RoundManager}, false otherwise
     */
    public boolean containsTeamId(String teamId) {
        return containedTeamIds.contains(teamId);
    }
    
    /**
     * If this returns true, you can safely run {@link #nextRound()} to cycle to the next round, then {@link #getCurrentRound()} to get the list of match pairings for the round you just iterated to.
     * @return true if there is at least one un-played round next, false if there are no more rounds
     */
    public boolean hasNextRound() {
        return currentRoundIndex + 1 < schedule.size();
    }
    
    /**
     * Cycle to the next round. Don't run this without first checking {@link #hasNextRound()}. After iterating the round, you can get the new list of MatchPairings from {@link #getCurrentRound()}. 
     */
    public void nextRound() {
        currentRoundIndex++;
        playedRounds++;
        currentRound = schedule.get(currentRoundIndex);
        played.addAll(currentRound);
    }
    
    /**
     * @return the list of {@link MatchPairing}s for the current round.
     * @throws ArrayIndexOutOfBoundsException if you ran {@link #nextRound()} after {@link #hasNextRound()} returned false. 
     */
    public @NotNull List<MatchPairing> getCurrentRound() {
        return currentRound;
    }
    
    public static List<List<MatchPairing>> generateSchedule(@NotNull List<String> teamIds, int numOfArenas) {
        return generateSchedule(teamIds, numOfArenas, Collections.emptyList());
    }
    
    
    /**
     * Generates a schedule for the teams to face off.
     * 
     * @param teamIds the teamIds to generate the schedule for
     * @param numOfArenas the number of arenas (each arena fits 2 teams)
     * @return A list where each entry is the {@code numOfArenas} {@link MatchPairing}s for each round. The size
     * of the returned list will be the number of rounds. 
     */
    public static List<List<MatchPairing>> generateSchedule(@NotNull List<String> teamIds, 
                                                            int numOfArenas, 
                                                            @NotNull List<MatchPairing> exclude) {
        List<MatchPairing> matchPairings = generateAllMatchPairings(teamIds, exclude);
        
        List<List<MatchPairing>> schedule = new ArrayList<>();
        
        // Step 2: Loop while there are pairs left to schedule
        while (!matchPairings.isEmpty()) {
            List<MatchPairing> roundMatches = new ArrayList<>();
            Set<String> usedTeams = new HashSet<>();
            
            // Step 3: Fill up the round with matches, ensuring no team plays twice
            for (int i = 0; i < matchPairings.size(); i++) {
                MatchPairing pair = matchPairings.get(i);
                String team1 = pair.northTeam();
                String team2 = pair.southTeam();
                
                if (!usedTeams.contains(team1) && !usedTeams.contains(team2)) {
                    roundMatches.add(pair);
                    usedTeams.add(team1);
                    usedTeams.add(team2);
                    matchPairings.remove(i);
                    i--; // Adjust index after removal
                    
                    if (roundMatches.size() == numOfArenas) {
                        break;
                    }
                }
            }
            
            schedule.add(roundMatches);
        }
        
        return schedule;
    }
    
    /**
     * @param teamIds the teams to generate the round-robin for
     * @return all possible round-robin match-ups
     */
    public static @NotNull List<MatchPairing> generateRoundRobin(@NotNull List<String> teamIds) {
        List<String> teams = teamIds.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
        if (teamIds.size() % 2 != 0) {
            teams.add("%BYE%");
        }
        
        int numTeams = teams.size();
        int numRounds = numTeams - 1;
        int halfSize = numTeams / 2;
        List<MatchPairing> allMatches = new ArrayList<>();
        
        for (int roundNum = 0; roundNum < numRounds; roundNum++) {
            for (int i = 0; i < halfSize; i++) {
                String home = teams.get(i);
                String away = teams.get(numTeams - 1 - i);
                allMatches.add(new MatchPairing(home, away));
            }
            // rotate teams
            String last = teams.removeLast();
            teams.add(1, last);
        }
        return allMatches;
    }
    
    public static List<List<MatchPairing>> distributeMatches(List<MatchPairing> allMatches, int maxMatchesPerRound) {
        List<List<MatchPairing>> rounds = new ArrayList<>();
        List<MatchPairing> matches = new ArrayList<>(allMatches);
        
        while (!matches.isEmpty()) {
            List<MatchPairing> currentRound = new ArrayList<>();
            Set<String> teamsInRound = new HashSet<>();
            
            for (int i = 0; i < maxMatchesPerRound; i++) {
                Iterator<MatchPairing> iterator = matches.iterator();
                while (iterator.hasNext()) {
                    MatchPairing match = iterator.next();
                    String home = match.northTeam();
                    String away = match.southTeam();
                    
                    // check if either team is already playing in the current round
                    if (!teamsInRound.contains(home) && !teamsInRound.contains(away)) {
                        currentRound.add(match);
                        teamsInRound.add(home);
                        teamsInRound.add(away);
                        iterator.remove();
                        break; // move to the next match
                    }
                }
            }
            
            rounds.add(currentRound);
        }
        return rounds;
    }
    
    /**
     * Generating all the {@link MatchPairing}s from the given teamIds, excluding the ones from the given list.
     * @param teamIds the teams to generate all {@link MatchPairing} combos from
     * @param exclude the {@link MatchPairing}s to exclude from the returned list
     * @return all combos of the given teamIds in {@link MatchPairing}s, excluding the {@link MatchPairing}s contained 
     * in the exclude list.
     */
    private static @NotNull List<MatchPairing> generateAllMatchPairings(@NotNull List<String> teamIds, @NotNull List<MatchPairing> exclude) {
        List<MatchPairing> matchPairings = new ArrayList<>();
        List<String> sortedTeamIds = teamIds.stream().sorted().toList();
        
        // Step 1: Generate all unique team pairs
        for (int i = 0; i < sortedTeamIds.size(); i++) {
            for (int j = i + 1; j < sortedTeamIds.size(); j++) {
                String northTeamId = sortedTeamIds.get(i);
                String southTeamId = sortedTeamIds.get(j);
                
                // Check if the pair is in the exclude list
                MatchPairing matchPairing = new MatchPairing(northTeamId, southTeamId);
                if (!exclude.contains(matchPairing)) {
                    matchPairings.add(matchPairing);
                }
            }
        }
        return matchPairings;
    }
}
