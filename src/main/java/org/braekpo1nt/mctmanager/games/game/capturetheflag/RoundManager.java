package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    
    /**
     * The index of the current round
     */
    private int currentRoundIndex = 0;
    private @NotNull List<MatchPairing> currentRound;
    
    /**
     * Used only for checking if a given teamId is contained in this {@link RoundManager} or not
     * @see #containsTeamId(String) 
     */
    private @NotNull Set<String> teamIds;
    
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
    
    /**
     * Initialize the RoundManager to handle rounds for a game
     * @param teamIds the teamIds of the teams in the round (must have at least 2 entries)
     * @param numOfArenas the number of arenas (must be greater than 0)
     */
    public RoundManager(@NotNull List<@NotNull String> teamIds, int numOfArenas) {
        Preconditions.checkArgument(teamIds.size() >= 2, "There must be at least two teamIds (got %s)", teamIds.size());
        Preconditions.checkArgument(numOfArenas > 0, "there must be at least 1 arena");
        this.teamIds = new HashSet<>(teamIds);
        Preconditions.checkArgument(this.teamIds.size() == teamIds.size(), "Duplicate teamId found in teamIds %s", teamIds.toString());
        this.schedule = generateSchedule(teamIds, numOfArenas);
        Preconditions.checkArgument(!schedule.isEmpty(), "Generated rounds were empty, teamIds: %s, numOfArenas: %s", teamIds, numOfArenas);
        currentRound = this.schedule.getFirst();
        played = new ArrayList<>(currentRound);
        int i = 1;
        for (List<MatchPairing> round : schedule) {
            Main.logger().info(String.format("Round %d:", i));
            for (MatchPairing matchPairing : round) {
                Main.logger().info(String.format("--%s", matchPairing));
            }
            i++;
        }
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
        this.teamIds = new HashSet<>(teamIds);
        Preconditions.checkArgument(this.teamIds.size() == teamIds.size(), "Duplicate teamId found in teamIds %s", teamIds.toString());
        this.schedule = generateSchedule(teamIds, numOfArenas, played);
        currentRoundIndex = -1;
        Preconditions.checkArgument(!schedule.isEmpty(), "Generated rounds were empty, teamIds: %s, numOfArenas: %s", teamIds, numOfArenas);
        int i = 1;
        for (List<MatchPairing> round : schedule) {
            Main.logger().info(String.format("Round %d:", i));
            for (MatchPairing matchPairing : round) {
                Main.logger().info(String.format("--%s", matchPairing));
            }
            i++;
        }
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
        return playedRounds + schedule.size();
    }
    
    /**
     * @param teamId the teamId to check for
     * @return true if the given teamId is contained in this {@link RoundManager}, false otherwise
     */
    public boolean containsTeamId(String teamId) {
        return teamIds.contains(teamId);
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
     * Generates a schedule for the teams to face off. This was generated by ChatGPT then modified slightly to
     * fit the application. 
     * @param teamIds the teamIds to generate the schedule for
     * @param numOfArenas the number of arenas (each arena fits 2 teams)
     * @return A list where each entry is the {@code numOfArenas} {@link MatchPairing}s for each round. The size
     * of the returned list will be the number of rounds. 
     */
    public static List<List<MatchPairing>> generateSchedule(@NotNull List<String> teamIds, 
                                                            int numOfArenas, 
                                                            @NotNull List<MatchPairing> exclude) {
        int numOfTeams = teamIds.size();
        List<List<MatchPairing>> schedule = new ArrayList<>();
        int numOfRounds = (numOfTeams % 2 == 0) ? numOfTeams - 1 : numOfTeams;
        
        // Clone the teams list so as not to modify the original
        List<String> rotatingTeams = new ArrayList<>(teamIds);
        
        for (int round = 0; round < numOfRounds; round++) {
            List<MatchPairing> roundMatches = new ArrayList<>();
            
            for (int i = 0; i < numOfTeams / 2; i++) {
                int index = numOfTeams - 1 - i;
                MatchPairing match = new MatchPairing(rotatingTeams.get(i), rotatingTeams.get(index));
                if (!exclude.contains(match)) {
                    roundMatches.add(match);
                }
            }
            
            // Rotate teams, keeping the first one fixed
            rotatingTeams.add(1, rotatingTeams.removeLast());
            
            // Split matches into groups for each arena
            for (int i = 0; i < roundMatches.size(); i += numOfArenas) {
                int end = Math.min(i + numOfArenas, roundMatches.size());
                schedule.add(new ArrayList<>(roundMatches.subList(i, end)));
            }
        }
        
        return schedule;
    }
}
