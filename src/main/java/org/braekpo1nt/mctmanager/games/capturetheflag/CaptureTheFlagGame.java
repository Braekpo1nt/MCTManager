package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CaptureTheFlagGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private List<Player> participants;
    /**
     * The roster of players. Each element is a pair of two team names to fight
     * against each other in capture the flag
     */
    private List<List<String>> roster;
    private int round = 0;
    
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        round = 1;
        this.roster = generateRoster(newParticipants);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        Bukkit.getLogger().info("Started Capture the Flag");
    }
    
    /**
     * Returns a new roster using the participants' teams
     * @param newParticipants The participants to generate a roster for
     * @return A list containing unique pairs of teams for the roster
     */
    public List<List<String>> generateRoster(List<Player> newParticipants) {
        List<String> teamNames = getTeamNames(newParticipants);
        List<List<String>> newRoster = new ArrayList<>();
        int n = teamNames.size();
        // Generate all possible combinations of indices
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                List<String> combination = new ArrayList<>(2);
                combination.add(teamNames.get(i));
                combination.add(teamNames.get(j));
                newRoster.add(combination);
            }
        }
        return newRoster;
    }
    
    /**
     * Gets a list of all the team names of the participants
     * @param newParticipants The list of participants to get the team names of
     * @return A list of all unique team names which the participants belong to.
     */
    private List<String> getTeamNames(List<Player> newParticipants) {
        List<String> teamNames = new ArrayList<>();
        for (Player participant : newParticipants) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (!teamNames.contains(teamName)){
                teamNames.add(teamName);
            }
        }
        return teamNames;
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        
    }
    
    @Override
    public void stop() {
        Bukkit.getLogger().info("Stopped Capture the Flag");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
}
