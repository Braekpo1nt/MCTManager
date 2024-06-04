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
    
    // reporting
    Map<String, Integer> longestOnDeckStreak;
    Map<String, Integer> onDeckStreak;
    List<String> lastOnDeck;
    Map<String, Integer> totalOnDeckRounds;
    // reporting
    
    public RoundManager(CaptureTheFlagGame game, int numOfArenas) {
        this.game = game;
        this.numOfArenas = numOfArenas;
    }
    
    /**
     * Initializes internal variables, kicks off the first round, and begins the progression of rounds.
     * Call this before {@link RoundManager#startNextRound()}
     */
    public void start(List<String> newTeams) {
        this.teams = new ArrayList<>(newTeams.size());
        roundsSpentOnDeck = new HashMap<>(newTeams.size());
        teamsToFight = new HashMap<>(newTeams.size());
        playedMatchPairings = new HashSet<>();
        // reporting
        longestOnDeckStreak = new HashMap<>();
        onDeckStreak = new HashMap<>();
        lastOnDeck = new ArrayList<>();
        totalOnDeckRounds = new HashMap<>();
        // reporting
        for (String team : newTeams) {
            teams.add(team);
            roundsSpentOnDeck.put(team, 0);
            List<String> enemyTeams = new ArrayList<>(newTeams);
            enemyTeams.remove(team);
            teamsToFight.put(team, enemyTeams);
            // reporting
            longestOnDeckStreak.put(team, 0);
            onDeckStreak.put(team, 0);
            totalOnDeckRounds.put(team, 0);
            // reporting
        }
        startNextRound();
    }
    
    /**
     * Call this after {@link RoundManager#start(List)}.
     */
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
    
    public void onTeamQuit(String team) {
        teams.remove(team);
    }
    
    public void onTeamJoin(String team) {
        if (teamExistsInGame(team)) {
            return;
        }
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
    }
    
    public boolean teamExistsInGame(String team) {
        return teams.contains(team);
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
    
    public void roundIsOver() {
        playedRounds++;
        if (thereAreRoundsLeft()) {
            startNextRound();
            return;
        }
        game.stop();
    }
    
    /**
     * @return true if 
     */
    private boolean thereAreRoundsLeft() {
        for (String team : teams) {
            List<String> toFight = teamsToFight.get(team);
            if (!toFight.isEmpty()) {
                for (String enemyTeam : toFight) {
                    // if the enemy team is online and in the game
                    if (teams.contains(enemyTeam)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
    protected static List<MatchPairing> generateMatchPairings(
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
    
    /**
     * @return The number of rounds that have been played already in this game
     */
    public int getPlayedRounds() {
        return playedRounds;
    }
    
    /**
     * @return the maximum number of rounds played
     */
    public int getMaxRounds() {
        return getPlayedRounds() + calculateRoundsLeft(teamsToFight, numOfArenas) + 1;
    }
    
    public static int calculateRoundsLeft(Map<String, List<String>> teamsToFight, int numOfArenas) {
        Set<Set<String>> matches = createMatches(teamsToFight);
        int roundsLeft = 0;
        while (!matches.isEmpty()) {
            Iterator<Set<String>> iterator = matches.iterator();
            Set<String> roundTeams = new HashSet<>();
            while (iterator.hasNext() && roundTeams.size() < numOfArenas*2) {
                Set<String> matchPair = iterator.next();
                if (roundTeamsContainsNoTeamsFromPair(roundTeams, matchPair)) {
                    roundTeams.addAll(matchPair);
                    iterator.remove();
                }
            }
            roundsLeft++;
        }
        return roundsLeft;
    }
    
    private static boolean roundTeamsContainsNoTeamsFromPair(Set<String> roundTeams, Set<String> pair) {
        for (String team : pair) {
            if (roundTeams.contains(team)) {
                return false;
            }
        }
        return true;
    }
    
    public static Set<Set<String>> createMatches(Map<String, List<String>> teamsToFight) {
        Set<Set<String>> pairs = new HashSet<>();
        
        for (Map.Entry<String, List<String>> entry : teamsToFight.entrySet()) {
            String team = entry.getKey();
            List<String> opponents = entry.getValue();
            
            for (String opponent : opponents) {
                Set<String> pair = Set.of(team, opponent);
                pairs.add(pair);
            }
        }
        
        return pairs;
    }
    
    //testing
    Set<MatchPairing> getPlayedMatchPairings() {
        return playedMatchPairings;
    }
    
    Map<String, Integer> getRoundsSpentOnDeck() {
        return roundsSpentOnDeck;
    }
}
