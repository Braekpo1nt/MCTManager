package org.braekpo1nt.mctmanager.games.capturetheflag;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class CaptureTheFlagGame implements MCTGame, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ClassPickerManager classPickerManager;
    private boolean gameActive = false;
    private boolean roundHasStarted = false;
    private final String title = ChatColor.BLUE+"Capture the Flag";
    private final World captureTheFlagWorld;
    private final Location spawnObservatory;
    private List<Arena> arenas;
    private List<Player> participants;
    /**
     * a list of lists of TeamPairings. Each element is a list of 1-4 TeamPairings.
     * Each index corresponds to a round.
     */
    private List<List<MatchPairing>> allRoundTeamPairings;
    /**
     * Contains the 1-4 TeamPairings for the current round. 
     * See {@link CaptureTheFlagGame#allRoundTeamPairings}.
     * Each index corresponds to a pair of teams to fight in their own arena. 
     */
    private List<MatchPairing> currentRoundTeamParings;
    private int currentRound = 0;
    private int maxRounds = 0;
    private List<UUID> livingPlayers;
    private List<UUID> deadPlayers;
    private Map<UUID, Integer> killCounts;
    private int classSelectionCountdownTaskIt;
    private int startNextRoundTimerTaskId;
    private int currentRoundTimerTaskId;
    
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
     * @param newParticipants The new participants list
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
        closeGlassBarriers();
        killCounts = new HashMap<>();
        gameActive = true;
        startStartNextRoundTimer();
        Bukkit.getLogger().info("Started Capture the Flag");
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        openGlassBarriers();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        classPickerManager.stopClassPicking(participants);
        gameActive = false;
        roundHasStarted = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopped Capture the Flag");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameActive) {
            return;
        }
        if (!roundHasStarted) {
            return;
        }
        Player killed = event.getPlayer();
        if (!participants.contains(killed)) {
            return;
        }
        Component deathMessage = event.deathMessage();
        event.setCancelled(true);
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed);
        }
        if (allPlayersAreDead()) {
            endCurrentRound();
        }
    }
    
    private boolean allPlayersAreDead() {
        return livingPlayers.isEmpty();
    }
    
    
    private void onParticipantDeath(Player killed) {
        UUID killedUniqueId = killed.getUniqueId();
        switchPlayerFromLivingToDead(killedUniqueId);
        resetHealthAndHunger(killed);
        killed.getInventory().clear();
        new BukkitRunnable() {
            @Override
            public void run() {
                teleportParticipantToSpawnObservatory(killed);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    private void onParticipantGetKill(Player killed) {
        Player killer = killed.getKiller();
        if (!participants.contains(killer)) {
            return;
        }
        addKill(killer.getUniqueId());
        gameManager.awardPointsToPlayer(killer, 20);
    }
    
    private void switchPlayerFromLivingToDead(UUID killedUniqueId) {
        livingPlayers.remove(killedUniqueId);
        livingPlayers.add(killedUniqueId);
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
        resetHealthAndHunger(participant);
    }
    
    private void resetParticipantForRoundEnd(Player participant) {
        participant.getInventory().clear();
        teleportParticipantToSpawnObservatory(participant);
        killCounts.put(participant.getUniqueId(), 0);
        resetHealthAndHunger(participant);
        participant.setGameMode(GameMode.ADVENTURE);
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
    
    private void startStartNextRoundTimer() {
        this.startNextRoundTimerTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    startNextRound();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : participants){
                    updateNextRoundFastBoardTimer(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startCurrentRoundTimer() {
        this.currentRoundTimerTaskId = new BukkitRunnable() {
            int count = 3*60; //3 minutes
            @Override
            public void run() {
                if (count <= 0) {
                    endCurrentRound();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : participants){
                    updateCurrentRoundFastBoardTimer(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startNextRound() {
        this.currentRound++;
        this.livingPlayers = new ArrayList<>();
        this.deadPlayers = new ArrayList<>();
        currentRoundTeamParings = allRoundTeamPairings.get(currentRound-1);
        closeGlassBarriers();
        for (Player participant : participants){
            initializeParticipantForRound(participant);
        }
        teleportTeamPairingsToArenas();
        roundHasStarted = true;
        startClassSelectionPeriod();
        Bukkit.getLogger().info("Starting round " + currentRound);
    }
    
    private void endCurrentRound() {
        livingPlayers.clear();
        deadPlayers.clear();
        openGlassBarriers();
        for(Player participant : participants) {
            resetParticipantForRoundEnd(participant);
        }
        roundHasStarted = false;
        Bukkit.getLogger().info("Ending round " + currentRound);
        if (!hasNextRound()) {
            stop();
        }
        startStartNextRoundTimer();
    }
    
    /**
     * Checks if there is a next round in the game
     * @return True if the current round is not the last round
     */
    private boolean hasNextRound() {
        return currentRound < maxRounds;
    }
    
    private void closeGlassBarriers() {
        for (Arena arena : arenas) {
            BlockPlacementUtils.createCube(arena.northBarrier(), 5, 4, 1, Material.GLASS_PANE);
            BlockPlacementUtils.createCube(arena.southBarrier(), 5, 4, 1, Material.GLASS_PANE);
        }
    }
    
    private void openGlassBarriers() {
        for (Arena arena : arenas) {
            BlockPlacementUtils.createCube(arena.northBarrier(), 5, 4, 1, Material.AIR);
            BlockPlacementUtils.createCube(arena.southBarrier(), 5, 4, 1, Material.AIR);
        }
    }
    
    private void startClassSelectionPeriod() {
        this.classPickerManager.resetClassPickerTracker();
        messageAllParticipants(Component.text("Choose your class"));
        classPickerManager.startClassPicking(participants);
        this.classSelectionCountdownTaskIt = new BukkitRunnable() {
            private int count = 20;
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllParticipants(Component.text("Class selection is over"));
                } else {
                    for (Player participant : participants) {
                        String timeString = TimeStringUtils.getTimeString(count);
                        updateClassSelectionFastBoardTimer(participant, timeString);
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
        classPickerManager.stopClassPicking(participants);
        messageAllParticipants(Component.text("Capture the flag!"));
        openGlassBarriers();
        startCurrentRoundTimer();
    }
    
    /**
     * See {@link CaptureTheFlagGame#allRoundTeamPairings}
     * @param newParticipants The participants whose teams will be used to create the pairings
     * @return A new list of lists of 1-4 TeamPairings. See {@link CaptureTheFlagGame#allRoundTeamPairings}
     */
    public List<List<MatchPairing>> generateAllRoundTeamPairings(List<Player> newParticipants) {
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
        List<MatchPairing> matchPairings = generateAllPairings(teamNames);
        // A list of lists of 1-4 TeamPairings
        List<List<MatchPairing>> newAllRoundTeamPairings = new ArrayList<>();
        int pairingIndex = 0;
        while (pairingIndex < matchPairings.size()) {
            // A list of 1-4 TeamPairings
            List<MatchPairing> singleRoundPairingGroup = new ArrayList<>();
            int j = 0;
            while (j < 4 && pairingIndex < matchPairings.size()) {
                singleRoundPairingGroup.add(matchPairings.get(pairingIndex));
                pairingIndex++;
                j++;
            }
            newAllRoundTeamPairings.add(singleRoundPairingGroup);
        }
        return newAllRoundTeamPairings;
    }
    
    private static List<MatchPairing> generateAllPairings(List<String> teamNames) {
        List<MatchPairing> matchPairings = new ArrayList<>();
        int n = teamNames.size();
        // Generate all possible combinations of indices
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                String teamA = teamNames.get(i);
                String teamB = teamNames.get(j);
                MatchPairing matchPairing = new MatchPairing(teamA, teamB);
                matchPairings.add(matchPairing);
            }
        }
        return matchPairings;
    }
    
    private void teleportParticipantToSpawnObservatory(Player participant) {
        Bukkit.getLogger().info("teleporting " + participant.getName() + " to spawn observatory");
        participant.teleport(spawnObservatory);
    }
    
    private void teleportTeamPairingsToArenas() {
        for (int i = 0; i < currentRoundTeamParings.size(); i++) {
            MatchPairing matchPairing = currentRoundTeamParings.get(i);
            Arena arena = arenas.get(i);
            teleportTeamPairingToArena(matchPairing, arena);
        }
    }
    
    /**
     * Teleports all participants whose teams are in the given pairing to their
     * respective spawn positions in the given arena
     * @param matchPairing The TeamPairing to teleport to the arena
     * @param arena The arena to teleport the TeamPairing to
     */
    private void teleportTeamPairingToArena(MatchPairing matchPairing, Arena arena) {
        for (Player participant : participants) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (matchPairing.northTeam().equals(teamName)) {
                participant.teleport(arena.northSpawn());
            }
            if (matchPairing.southTeam().equals(teamName)) {
                participant.teleport(arena.southSpawn());
            }
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(classSelectionCountdownTaskIt);
        Bukkit.getScheduler().cancelTask(startNextRoundTimerTaskId);
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
    
    private void updateClassSelectionFastBoardTimer(Player participant, String timerString) {
        int killCount = killCounts.getOrDefault(participant.getUniqueId(), 0);
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
    
    private void updateNextRoundFastBoardTimer(Player participant, String timerString) {
        int killCount = killCounts.getOrDefault(participant.getUniqueId(), 0);
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                ChatColor.RED+"Kills: "+killCount,
                "",
                "Next round:",
                timerString,
                "",
                "Round: " + currentRound + "/" + maxRounds
        );
    }
    
    private void updateCurrentRoundFastBoardTimer(Player participant, String timerString) {
        int killCount = killCounts.getOrDefault(participant.getUniqueId(), 0);
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                ChatColor.RED+"Kills: "+killCount,
                "",
                "Time:",
                timerString,
                "",
                "Round: " + currentRound + "/" + maxRounds
        );
    }
    
    private void initializeFastBoard(Player participant) {
        int killCount = killCounts.getOrDefault(participant.getUniqueId(), 0);
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
    
    private void addKill(UUID killerUniqueId) {
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
        gameManager.getFastBoardManager().updateLine(
                killerUniqueId,
                2,
                ChatColor.RED+"Kills: " + newKillCount
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
                new Location(captureTheFlagWorld, -15, -16, -1043), // North spawn
                new Location(captureTheFlagWorld, -15, -16, -1003), // South spawn
                new Location(captureTheFlagWorld, -6, -13, -1040), // North flag 
                new Location(captureTheFlagWorld, -24, -13, -1006), // South flag 
                new Location(captureTheFlagWorld, -17, -16, -1042), // North barrier
                new Location(captureTheFlagWorld, -17, -16, -1004) // South barrier 
                
        ));
        //NorthEast
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -1043), // North spawn
                new Location(captureTheFlagWorld, 15, -16, -1003), // South spawn
                new Location(captureTheFlagWorld, 24, -13, -1040), // North flag 
                new Location(captureTheFlagWorld, 6, -13, -1006), // South flag 
                new Location(captureTheFlagWorld, 13, -16, -1042), // North barrier
                new Location(captureTheFlagWorld, 13, -16, -1004) // South barrier 
        ));
        //SouthWest
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, -15, -16, -997), // North spawn
                new Location(captureTheFlagWorld, -15, -16, -957), // South spawn
                new Location(captureTheFlagWorld, -6, -13, -994), // North flag 
                new Location(captureTheFlagWorld, -24, -13, -960), // South flag 
                new Location(captureTheFlagWorld, -17, -16, -996), // North barrier
                new Location(captureTheFlagWorld, -17, -16, -958) // South barrier 
        ));
        //SouthEast
        arenas.add(new Arena(
                new Location(captureTheFlagWorld, 15, -16, -997), // North spawn
                new Location(captureTheFlagWorld, 15, -16, -957), // South spawn
                new Location(captureTheFlagWorld, 24, -13, -994), // North flag 
                new Location(captureTheFlagWorld, 6, -13, -960), // South flag 
                new Location(captureTheFlagWorld, 13, -16, -996), // North barrier
                new Location(captureTheFlagWorld, 13, -16, -958) // South barrier 
        ));
    }
}
