package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RoundManager {
    
    /**
     * Used as a stand-in team for odd numbers of teams
     */
    public static final String BYE = "%BYE%";
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
        List<MatchPairing> allMatches = generateRoundRobin(teamIds);
        return distributeMatches(allMatches, numOfArenas);
    }
    
    public static List<List<MatchPairing>> generateSchedule(@NotNull List<String> teamIds, int numOfArenas, List<MatchPairing> exclude) {
        List<MatchPairing> allMatches = generateRoundRobin(teamIds);
        allMatches.removeAll(exclude);
        return distributeMatches(allMatches, numOfArenas);
    }
    
    /**
     * @param teamIds the teams to generate the round-robin for
     * @return all possible round-robin match-ups
     */
    public static @NotNull List<MatchPairing> generateRoundRobin(@NotNull List<String> teamIds) {
        List<String> teams = teamIds.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
        if (teamIds.size() % 2 != 0) {
            teams.add(BYE);
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
    
}
