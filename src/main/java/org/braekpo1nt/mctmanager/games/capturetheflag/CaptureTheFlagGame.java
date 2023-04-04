package org.braekpo1nt.mctmanager.games.capturetheflag;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CaptureTheFlagGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final String title = ChatColor.BLUE+"Capture the Flag";
    private final World captureTheFlagWorld;
    private Location spawnObservatory;
    private List<List<Location>> battleSpawnLocations;
    private List<List<Location>> flagLocations;
    private List<Player> participants;
    /**
     * a list of round paring groups from the participants.
     * A "round pairing group" is a list of 1-4 "round pairings". They represent the "round pairings" that will fight each round (there can only be a max of 4 because there are 4 arenas)
     * A "round pairing" is a list of two team names, which will have to fight each other.
     * Each index corresponds to a round.
     */
    private List<List<List<String>>> allRoundPairingGroups;
    /**
     * Contains the 1-4 "round pairings" that will fight in the current round. See {@link CaptureTheFlagGame#allRoundPairingGroups}.
     * Each index corresponds to a pair of teams to fight in their own arena. 
     */
    private List<List<String>> currentRoundParingGroup;
    private int currentRound = 0;
    private int maxRounds = 0;
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
        currentRound = 0;
        this.allRoundPairingGroups = generateRoundPairingGroups(newParticipants);
        maxRounds = allRoundPairingGroups.size();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startNextRound();
        Bukkit.getLogger().info("Started Capture the Flag");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        teleportParticipantToSpawnObservatory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        resetHealthAndHunger(participant);
        clearStatusEffects(participant);
        initializeFastBoard(participant);
    }
    
    private void initializeParticipantForRound(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        livingPlayers.add(participantUniqueId);
        killCounts.put(participantUniqueId, 0);
        teleportParticipantToStartingPosition(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        resetHealthAndHunger(participant);
        initializeFastBoard(participant);
    }
    
    
    private void startNextRound() {
        this.currentRound++;
        this.livingPlayers = new ArrayList<>();
        this.deadPlayers = new ArrayList<>();
        this.killCounts = new HashMap<>();
        for (Player participant : participants){
            initializeParticipantForRound(participant);
        }
        startClassSelectionPeriod();
        Bukkit.getLogger().info("Starting round " + currentRound);
    }
    
    /**
     * Create a list of "round paring groups" from the participants' teams.
     * See {@link CaptureTheFlagGame#allRoundPairingGroups}
     * @param newParticipants The participants whose teams will be used to create the pairings
     * @return A new "round pairing groups" list. See {@link CaptureTheFlagGame#allRoundPairingGroups}
     */
    public List<List<List<String>>> generateRoundPairingGroups(List<Player> newParticipants) {
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
        List<List<String>> allPairings = generateAllPairings(teamNames);
        // A list of round pairing groups, each of which is a group of 1-4 round pairings
        List<List<List<String>>> newRoundPairingGroups = new ArrayList<>();
        int pairingIndex = 0;
        while (pairingIndex < allPairings.size()) {
            // A list of 1-4 round pairings
            List<List<String>> singleRoundPairingGroup = new ArrayList<>();
            int j = 0;
            while (j < 4) {
                singleRoundPairingGroup.add(allPairings.get(pairingIndex));
                pairingIndex++;
                j++;
            }
            newRoundPairingGroups.add(singleRoundPairingGroup);
        }
        return newRoundPairingGroups;
    }
    
    @NotNull
    private static List<List<String>> generateAllPairings(List<String> teamNames) {
        List<List<String>> pairings = new ArrayList<>();
        int n = teamNames.size();
        // Generate all possible combinations of indices
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                List<String> combination = new ArrayList<>(2);
                combination.add(teamNames.get(i));
                combination.add(teamNames.get(j));
                pairings.add(combination);
            }
        }
        return pairings;
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
        participant.teleport(spawnObservatory);
    }
    
    private void teleportParticipantToStartingPosition(Player participant) {
        
    }
    
    private void resetHealthAndHunger(Player participant) {
        participant.setHealth(participant.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        participant.setFoodLevel(20);
        participant.setSaturation(5);
    }
    
    private void clearStatusEffects(Player participant) {
        for (PotionEffect effect : participant.getActivePotionEffects()) {
            participant.removePotionEffect(effect.getType());
        }
    }
    
    private void initializeFastBoard(Player participant) {
        int killCount = killCounts.get(participant.getUniqueId());
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                ChatColor.RED+"Kills: "+killCount,
                "",
                "Round time:",
                "3:00",
                "",
                "Round: " + currentRound + "/" + maxRounds
        );
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
