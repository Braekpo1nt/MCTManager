package org.braekpo1nt.mctmanager.games.capturetheflag;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.*;

public class CaptureTheFlagGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ClassPickerManager classPickerManager;
    private boolean gameActive = false;
    private final String title = ChatColor.BLUE+"Capture the Flag";
    private final World captureTheFlagWorld;
    private Location spawnObservatory;
    private List<Arena> arenas;
    private List<Player> participants;
    /**
     * a list of lists of TeamPairings. Each element is a list of 1-4 TeamPairings.
     * Each index corresponds to a round.
     */
    private List<List<TeamPairing>> allRoundTeamPairings;
    /**
     * Contains the 1-4 TeamPairings for the current round. 
     * See {@link CaptureTheFlagGame#allRoundTeamPairings}.
     * Each index corresponds to a pair of teams to fight in their own arena. 
     */
    private List<TeamPairing> currentRoundTeamParings;
    private int currentRound = 0;
    private int maxRounds = 0;
    private List<UUID> livingPlayers;
    private List<UUID> deadPlayers;
    private Map<UUID, Integer> killCounts;
    private int classSelectionCountdownTaskIt;
    
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.classPickerManager = new ClassPickerManager(plugin, gameManager);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld mvCaptureTheFlagWorld = worldManager.getMVWorld("FT");
        this.captureTheFlagWorld = mvCaptureTheFlagWorld.getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        spawnObservatory = anchorManager.getAnchorLocation("capture-the-flag");
        initializeArenas();
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
        this.allRoundTeamPairings = generateAllRoundTeamPairings(newParticipants);
        maxRounds = allRoundTeamPairings.size();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setUpTeamOptions();
        startNextRound();
        gameActive = true;
        Bukkit.getLogger().info("Started Capture the Flag");
    }
    
    @Override
    public void stop() {
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        this.classPickerManager.resetClassPickerTracker();
        cancelAllTasks();
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopped Capture the Flag");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        teleportParticipantToSpawnObservatory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        resetHealthAndHunger(participant);
        clearStatusEffects(participant);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    private void initializeParticipantForRound(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        livingPlayers.add(participantUniqueId);
        killCounts.put(participantUniqueId, 0);
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
        this.classPickerManager.resetClassPickerTracker();
        currentRoundTeamParings = allRoundTeamPairings.get(currentRound-1);
        for (Player participant : participants){
            initializeParticipantForRound(participant);
        }
        teleportTeamPairingsToArenas();
        startClassSelectionPeriod();
        Bukkit.getLogger().info("Starting round " + currentRound);
    }
    
    private void startClassSelectionPeriod() {
        messageAllParticipants(Component.text("Choose your class"));
        for (Player participant : participants) {
            classPickerManager.showClassPickerGui(participant);
        }
        this.classSelectionCountdownTaskIt = new BukkitRunnable() {
            private int count = 20;
            
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllParticipants(Component.text("Class selection is over"));
                } else {
                    for (Player participant : participants) {
                        String timeString = getTimeString(count);
                        updateClassSelectionTimer(participant, timeString);
                    }
                }
                if (count <= 0) {
                    startCaptureTheFlagRound();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startCaptureTheFlagRound() {
        classPickerManager.assignClassesToParticipantsWithoutClasses(participants);
    }
    
    /**
     * Returns the given seconds as a string representing time in the format
     * MM:ss (or minutes:seconds)
     * @param timeSeconds The time in seconds
     * @return Time string MM:ss
     */
    private String getTimeString(long timeSeconds) {
        Duration duration = Duration.ofSeconds(timeSeconds);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private void updateClassSelectionTimer(Player participant, String timerString) {
        int killCount = killCounts.get(participant.getUniqueId());
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                ChatColor.RED+"Kills: "+killCount,
                "",
                "Class selection:",
                timerString,
                "",
                "Round: " + currentRound + "/" + maxRounds
        );
    }
    
    /**
     * See {@link CaptureTheFlagGame#allRoundTeamPairings}
     * @param newParticipants The participants whose teams will be used to create the pairings
     * @return A new list of lists of 1-4 TeamPairings. See {@link CaptureTheFlagGame#allRoundTeamPairings}
     */
    public List<List<TeamPairing>> generateAllRoundTeamPairings(List<Player> newParticipants) {
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
        List<TeamPairing> teamPairings = generateAllPairings(teamNames);
        // A list of lists of 1-4 TeamPairings
        List<List<TeamPairing>> newAllRoundTeamPairings = new ArrayList<>();
        int pairingIndex = 0;
        while (pairingIndex < teamPairings.size()) {
            // A list of 1-4 TeamPairings
            List<TeamPairing> singleRoundPairingGroup = new ArrayList<>();
            int j = 0;
            while (j < 4 && pairingIndex < teamPairings.size()) {
                singleRoundPairingGroup.add(teamPairings.get(pairingIndex));
                pairingIndex++;
                j++;
            }
            newAllRoundTeamPairings.add(singleRoundPairingGroup);
        }
        return newAllRoundTeamPairings;
    }
    
    private static List<TeamPairing> generateAllPairings(List<String> teamNames) {
        List<TeamPairing> teamPairings = new ArrayList<>();
        int n = teamNames.size();
        // Generate all possible combinations of indices
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                String teamA = teamNames.get(i);
                String teamB = teamNames.get(j);
                TeamPairing teamPairing = new TeamPairing(teamA, teamB);
                teamPairings.add(teamPairing);
            }
        }
        return teamPairings;
    }
    
    private void teleportParticipantToSpawnObservatory(Player participant) {
        participant.teleport(spawnObservatory);
    }
    
    private void teleportTeamPairingsToArenas() {
        for (int i = 0; i < currentRoundTeamParings.size(); i++) {
            TeamPairing teamPairing = currentRoundTeamParings.get(i);
            Arena arena = arenas.get(i);
            teleportTeamPairingToArena(teamPairing, arena);
        }
    }
    
    /**
     * Teleports all participants whose teams are in the given pairing to their
     * respective spawn positions in the given arena
     * @param teamPairing The TeamPairing to teleport to the arena
     * @param arena The arena to teleport the TeamPairing to
     */
    private void teleportTeamPairingToArena(TeamPairing teamPairing, Arena arena) {
        for (Player participant : participants) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (teamPairing.getNorthTeam().equals(teamName)) {
                participant.teleport(arena.getNorthSpawn());
            }
            if (teamPairing.getSouthTeam().equals(teamName)) {
                participant.teleport(arena.getSouthSpawn());
            }
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(classSelectionCountdownTaskIt);
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
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    private void setUpTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private void initializeArenas() {
        this.arenas = new ArrayList<>(4);
        //NorthWest
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, -15, -16, -1043), // North
                new Location(captureTheFlagWorld, -15, -16, -1003), // South
                new Location(captureTheFlagWorld, -6, -13, -1040), // North
                new Location(captureTheFlagWorld, -24, -13, -1006) // South
        ));
        //NorthEast
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -1043), // North
                new Location(captureTheFlagWorld, 15, -16, -1003), // South
                new Location(captureTheFlagWorld, 24, -13, -1040), // North
                new Location(captureTheFlagWorld, 6, -13, -1006) // South
        ));
        //SouthWest
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, -15, -16, -997), // North
                new Location(captureTheFlagWorld, -15, -16, -957), // South
                new Location(captureTheFlagWorld, -6, -13, -994), // North
                new Location(captureTheFlagWorld, -24, -13, -960) // South
        ));
        //SouthEast
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -997), // North
                new Location(captureTheFlagWorld, 15, -16, -957), // South
                new Location(captureTheFlagWorld, 24, -13, -994), // North
                new Location(captureTheFlagWorld, 6, -13, -960) // South
        ));
    }
}
