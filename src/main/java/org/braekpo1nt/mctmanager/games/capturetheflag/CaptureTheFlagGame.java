package org.braekpo1nt.mctmanager.games.capturetheflag;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class CaptureTheFlagGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final World captureTheFlagWorld;
    private Location spawnObservatory;
    private List<List<Location>> battleSpawnLocations;
    private List<List<Location>> flagLocations;
    private List<Player> participants;
    /**
     * The roster of players. Each element is a pair of two team names to fight
     * against each other in capture the flag
     */
    private List<List<String>> roster;
    private int round = 0;
    private List<UUID> livingPlayers;
    private List<UUID> deadPlayers;
    private Map<UUID, Integer> killCounts;
    
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld mvCaptureTheFlagWorld = worldManager.getMVWorld("FT");
        this.captureTheFlagWorld = mvCaptureTheFlagWorld.getCBWorld();
        initializeLocations();
    }
    
    /**
     * Starts a new Capture the Flag game with the provided participants.
     * Assumes that the provided list of participants collectively belong
     * to at least 2 teams, and at most 8 teams. 
     * @param newParticipants
     */
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        round = 0;
        this.roster = generateRoster(newParticipants);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startNextRound();
        Bukkit.getLogger().info("Started Capture the Flag");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        teleportParticipantToSpawnObservatory(participant);
    }
    
    private void initializeParticipantForRound(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
    }
    
    
    private void startNextRound() {
        this.round++;
        this.livingPlayers = new ArrayList<>();
        this.deadPlayers = new ArrayList<>();
        this.killCounts = new HashMap<>();
        for (Player participant : participants){
            initializeParticipantForRound(participant);
        }
        startClassSelectionPeriod();
        Bukkit.getLogger().info("Starting round " + round);
    }
    
    /**
     * Returns a new roster using the participants' teams
     * @param newParticipants The participants to generate a roster for
     * @return A list containing unique pairs of teams for the roster
     */
    public List<List<String>> generateRoster(List<Player> newParticipants) {
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
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
    
    private void teleportParticipantToSpawnObservatory(Player participant) {
        
    }
    
    private void initializeLocations() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        spawnObservatory = anchorManager.getAnchorLocation("capture-the-flag");
        this.battleSpawnLocations = new ArrayList<>();
        //NorthWest
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, -15, -16, -1043), // North
                new Location(captureTheFlagWorld, -15, -16, -1003))); // South
    
        //NorthEast
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, 15, -16, -1043), // North
                new Location(captureTheFlagWorld, 15, -16, -1003))); // South
    
        //SouthWest
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, -15, -16, -997), // North
                new Location(captureTheFlagWorld, -15, -16, -957))); // South
    
        //SouthEast
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, 15, -16, -997), // North
                new Location(captureTheFlagWorld, 15, -16, -957))); // South
        
        
        this.flagLocations = new ArrayList<>();
        //NorthWest
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, -6, -13, -1040), // North
                new Location(captureTheFlagWorld, -24, -13, -1006))); // South
    
        //NorthEast
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, 24, -13, -1040), // North
                new Location(captureTheFlagWorld, 6, -13, -1006))); // South
    
        //SouthWest
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, -6, -13, -994), // North
                new Location(captureTheFlagWorld, -24, -13, -960))); // South
    
        //SouthEast
        battleSpawnLocations.add(Arrays.asList(
                new Location(captureTheFlagWorld, 24, -13, -994), // North
                new Location(captureTheFlagWorld, 6, -13, -960))); // South
    }
}
