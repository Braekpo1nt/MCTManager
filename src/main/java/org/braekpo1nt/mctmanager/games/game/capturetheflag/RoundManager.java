package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import java.util.*;

/**
 * - Handles the creation and progression of rounds
 * - Keeps track of who has fought who in the matches
 * - Minimizes consecutive rounds spent on deck for each team
 * - dynamically handles round creation, enabling teams leaving and joining mid-game
 */
public class RoundManager {
    
    private final CaptureTheFlagGame game;
    /**
     * The teams who have to fight
     */
    private List<String> teams = new ArrayList<>();
    /**
     * The number of rounds that have been played already in this game
     */
    private int playedRounds = 0;
    /**
     * The total number of rounds a team has spent on-deck for this game
     */
    private Map<String, Integer> roundsSpentOnDeck = new HashMap<>();
    /**
     * The MatchPairings that have been played in this game already. 
     * Keeps track of MatchPairings with teams that have left in case they come back.
     */
    private Set<MatchPairing> playedMatchPairings = new HashSet<>();
    /**
     * A list of teams that each team has yet to fight. 
     * Keeps track of teams that have left in case they come back. 
     */
    private Map<String, List<String>> teamsToFight = new HashMap<>();
    private final int numOfArenas;
    
    public RoundManager(CaptureTheFlagGame game, int numOfArenas) {
        this.game = game;
        this.numOfArenas = numOfArenas;
    }
    
    /**
     * Initializes internal variables, kicks off the first round, and begins the progression of rounds.
     */
    public void start(List<String> newTeams) {
        this.teams = new ArrayList<>(newTeams.size());
        roundsSpentOnDeck = new HashMap<>(newTeams.size());
        teamsToFight = new HashMap<>(newTeams.size());
        playedMatchPairings = new HashSet<>();
        for (String team : newTeams) {
            teams.add(team);
            roundsSpentOnDeck.put(team, 0);
            List<String> enemyTeams = new ArrayList<>(newTeams);
            enemyTeams.remove(team);
            teamsToFight.put(team, enemyTeams);
        }
        startNextRound();
    }
    
    private void startNextRound() {
        List<MatchPairing> roundMatchPairings = generateNextRoundMatchPairings();
        List<String> participantTeams = getTeamsFromMatchPairings(roundMatchPairings);
        List<String> onDeckTeams = new ArrayList<>(teams.size() - participantTeams.size());
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
            List<String> northTeamsToFight = teamsToFight.get(roundMP.northTeam());
            northTeamsToFight.remove(roundMP.southTeam());
            List<String> southTeamsToFight = teamsToFight.get(roundMP.southTeam());
            southTeamsToFight.remove(roundMP.northTeam());
        }
        game.startNextRound(participantTeams, roundMatchPairings);
    }
    
    /**
     * @param matchPairings the matchPairings to extract the teams from
     * @return a list of unique teams from the given matchPairings
     */
    private List<String> getTeamsFromMatchPairings(List<MatchPairing> matchPairings) {
        Set<String> teams = new HashSet<>(matchPairings.size()*2);
        for (MatchPairing matchPairing : matchPairings) {
            teams.add(matchPairing.northTeam());
            teams.add(matchPairing.southTeam());
        }
        return teams.stream().toList();
    }
    
    /**
     * Generates the MatchPairings for the next round, prioritizing players who have the most teams to fight, with the most rounds spent on-deck as a tie-breaker. This ensures that teams will not spend more consecutive rounds on-deck than they have to. 
     * @return the MatchPairings for the next round
     */
    private List<MatchPairing> generateNextRoundMatchPairings() {
        List<String> sortedTeams = teams.stream().sorted(Comparator.<String, Integer>
                        comparing(team -> teamsToFight.get(team).size(), Comparator.reverseOrder())
                .thenComparing(roundsSpentOnDeck::get, Comparator.reverseOrder())
        ).toList();
        return generateMatchPairings(sortedTeams, playedMatchPairings, numOfArenas);
    }
    
    /**
     * Generates anywhere from zero to numOfArenas MatchPairings using the given teams. It is assumed that teams are sorted by highest-priority-first. 
     * <p>
     * It will ensure that no MatchPairings will be generated that are in the set of playedMatchPairings.
     * @return zero to numOfArenas MatchPairings for the given teams, assuming the provided playedMatchPairings.
     */
    private List<MatchPairing> generateMatchPairings(
            List<String> sortedTeams, Set<MatchPairing> playedMatchPairings, int numOfArenas) {
        List<MatchPairing> result = new ArrayList<>();
        Set<String> teamsUsed = new HashSet<>();
    
        for (int i = 0; i < sortedTeams.size() - 1 && result.size() < numOfArenas; i++) {
            String team1 = sortedTeams.get(i);
            if (!teamsUsed.contains(team1)) {
                for (int j = i + 1; j < sortedTeams.size(); j++) {
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
    
    public int getPlayedRounds() {
        return playedRounds;
    }
    
    public int getMaxRounds() {
        return calculateRounds(teams.size(), numOfArenas); 
    }
    
    private int calculateRounds(int numOfTeams, int numOfArenas) {
        return ((int) Math.ceil((numOfTeams * (numOfTeams - 1) / 2.0) / numOfArenas));
    }
}
