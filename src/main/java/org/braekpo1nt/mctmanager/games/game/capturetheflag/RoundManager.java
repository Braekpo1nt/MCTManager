package org.braekpo1nt.mctmanager.games.game.capturetheflag;

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
        Main.logger().info(String.format("Schedule (%d):", schedule.size()));
        for (int i = 0; i < schedule.size(); i++) {
            List<MatchPairing> round = schedule.get(i);
            Main.logger().info(String.format("Round %d:", i+1));
            for (MatchPairing matchPairing : round) {
                Main.logger().info(String.format("- %s", matchPairing));
            }
        }
    }
    
    /**
     * Initialize the RoundManager to handle rounds for a game
     * @param teamIds the teamIds of the teams in the round
     * @param numOfArenas the number of arenas
     */
    public RoundManager(@NotNull Collection<@NotNull String> teamIds, int numOfArenas) {
        this.containedTeamIds = new HashSet<>(teamIds);
        this.schedule = generateSchedule(teamIds, numOfArenas);
        if (schedule.isEmpty()) {
            Main.logger().info(String.format("Generated rounds were empty, teamIds: %s, numOfArenas: %s", teamIds, numOfArenas));
        }
        currentRound = this.schedule.getFirst();
        played = new ArrayList<>(currentRound);
        playedRounds = 0;
        maxRounds = schedule.size();
        logSchedule(schedule);
    }
    
    /**
     * Regenerates the rounds using the given set of teams. Previously played matches will not be re-added.
     * Handy for when a new team joins mid-game and needs to be mixed into the rounds. 
     * @param teamIds the teamIds of the teams in the round
     * @param numOfArenas the number of arenas (must be greater than 0)
     */
    public void regenerateRounds(@NotNull Collection<@NotNull String> teamIds, int numOfArenas) {
        Set<String> uniqueTeamIds = new HashSet<>(teamIds);
        Main.logf("regenerating rounds for %s", uniqueTeamIds.toString());
        if (uniqueTeamIds.size() != teamIds.size()) {
            Main.logger().severe(String.format("Duplicate teamId found in teamIds %s", teamIds));
        }
        this.containedTeamIds.clear();
        this.containedTeamIds.addAll(uniqueTeamIds);
        this.schedule = generateSchedule(teamIds, numOfArenas, played);
        if (schedule.isEmpty()) {
            Main.logger().info(String.format("Generated rounds were empty, teamIds: %s, numOfArenas: %s", teamIds, numOfArenas));
        }
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
     * @deprecated in favor of checking if the team exists in the {@link CaptureTheFlagGame#getTeams()}
     */
    @Deprecated
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
    
    public static List<List<MatchPairing>> generateSchedule(@NotNull Collection<String> teamIds, int numOfArenas) {
        List<MatchPairing> allMatches = generateRoundRobin(teamIds);
        return distributeMatches(allMatches, numOfArenas);
    }
    
    public static List<List<MatchPairing>> generateSchedule(@NotNull Collection<String> teamIds, int numOfArenas, List<MatchPairing> exclude) {
        List<MatchPairing> allMatches = generateRoundRobin(teamIds);
        allMatches.removeAll(exclude);
        return distributeMatches(allMatches, numOfArenas);
    }
    
    /**
     * @param teamIds the teams to generate the round-robin bracket for
     * @return all possible round-robin match-ups. If the number of teamIds is odd, this will include
     * a some {@link MatchPairing}s where one of the teams is {@link #BYE}. This is intended, so
     * that when you generate the schedule, no one team waits too many rounds in a row on-deck.
     */
    public static @NotNull List<MatchPairing> generateRoundRobin(@NotNull Collection<String> teamIds) {
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
    
    /**
     * This takes the raw generation of {@link MatchPairing}s and returns a schedule. Note that you should
     * include the pairs with {@link #BYE} as one of the teams, to make sure no individual team is
     * on-deck for too many rounds in a row. 
     * @param allMatches a list of all possible {@link MatchPairing}s to generate a schedule for. 
     *                   Must include {@link #BYE} pairs to keep fair distribution 
     *                   (i.e., this prevents a team being on deck for 4 rounds in a row)
     * @param numOfArenas the maximum pairs per round, as determined by the number of arenas available. If this is less than 1, an empty list will be returned.
     * @return a list of rounds, represented as lists of {@link MatchPairing} for the bracket. Will not include {@link MatchPairing}s which contain {@link #BYE}, as they are considered on-deck.
     */
    public static List<List<MatchPairing>> distributeMatches(List<MatchPairing> allMatches, int numOfArenas) {
        List<List<MatchPairing>> rounds = new ArrayList<>();
        List<MatchPairing> matches = new ArrayList<>(allMatches);
        if (numOfArenas < 1) {
            return Collections.emptyList();
        }
        
        while (!matches.isEmpty()) {
            List<MatchPairing> currentRound = new ArrayList<>();
            Set<String> teamsInRound = new HashSet<>();
            
            for (int i = 0; i < numOfArenas; i++) {
                Iterator<MatchPairing> iterator = matches.iterator();
                while (iterator.hasNext()) {
                    MatchPairing match = iterator.next();
                    String home = match.northTeam();
                    String away = match.southTeam();
                    
                    // check if either team is already playing in the current round
                    if (!teamsInRound.contains(home) && !teamsInRound.contains(away)) {
                        if (!BYE.equals(home) && !BYE.equals(away)) {
                            currentRound.add(match);
                        }
                        teamsInRound.add(home);
                        teamsInRound.add(away);
                        iterator.remove();
                        break; // move to the next match
                    }
                }
            }
            
            if (!currentRound.isEmpty()) {
                rounds.add(currentRound);
            }
        }
        return rounds;
    }
    
}
