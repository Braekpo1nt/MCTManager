package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Capture the flag games are broken down into the following hierarchy:
 * - The entire game: Contains multiple rounds. Kicks off the rounds, and only ends when all rounds are over
 * - Rounds: A round of the game, contains multiple matches. Kicks off the matches, and only ends when all matches are done.
 * - Matches: a match of two teams in a specific arena. Handles kills, points, and respawns within that specific arena with those two teams and nothing else. Tells the round when it's over. 
 */
public class CaptureTheFlagGame implements MCTGame, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.gameManager = gameManager;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
        List<MatchPairing> matchPairings = generateMatchPairings(teamNames);
        List<CaptureTheFlagMatch> matches = generateMatches(matchPairings);
    }
    
    /**
     * Generates all combinations of size 2 of the given teams in the form of {@link MatchPairing}s 
     * @param teamNames The teams to generate the combinations of
     * @return A list of MatchPairing objects representing all combinations of size 2
     * of the given list of team names
     */
    public List<MatchPairing> generateMatchPairings(List<String> teamNames) {
        List<MatchPairing> combinations = new ArrayList<>();
        for (int i = 0; i < teamNames.size(); i++) {
            for (int j = i + 1; j < teamNames.size(); j++) {
                String northTeam = teamNames.get(i);
                String southTeam = teamNames.get(j);
                MatchPairing pairing = new MatchPairing(northTeam, southTeam);
                combinations.add(pairing);
            }
        }
        return combinations;
    }
    
    private List<CaptureTheFlagMatch> generateMatches(List<MatchPairing> matchPairings) {
        List<CaptureTheFlagMatch> matches = new ArrayList<>(matchPairings.size());
        for (MatchPairing matchPairing : matchPairings) {
            CaptureTheFlagMatch newMatch = new CaptureTheFlagMatch();
            matches.add(newMatch);
        }
    }
    
    
    
    @Override
    public void stop() {
        
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
}
