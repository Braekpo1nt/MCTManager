package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.*;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagStorageUtil;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Capture the flag games are broken down into the following hierarchy:
 * - The entire game: Contains multiple rounds. Kicks off the rounds, and only ends when all rounds are over
 * - Rounds: A round of the game, contains multiple matches. Kicks off the matches, and only ends when all matches are done.
 * - Matches: a match of two teams in a specific arena. Handles kills, points, and respawns within that specific arena with those two teams and nothing else. Tells the round when it's over. 
 */
public class CaptureTheFlagGame implements MCTGame, Configurable, Listener, Headerable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final CaptureTheFlagStorageUtil storageUtil;
    /**
     * The number of rounds that have been played
     */
    private int playedRounds = 0;
    private List<MatchPairing> unPlayedMatchPairings;
    private int maxRounds;
    private CaptureTheFlagRound currentRound;
    private Map<String, OnDeckRounds> onDeckRounds = new HashMap<>();
    private List<MatchPairing> playedMatchPairings = new ArrayList<>();
    private final String title = ChatColor.BLUE+"Capture the Flag";
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private boolean gameActive = false;
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new CaptureTheFlagStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.CAPTURE_THE_FLAG;
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        participants = new ArrayList<>();
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        List<String> teamNames = gameManager.getTeamNames(newParticipants);
        unPlayedMatchPairings = CaptureTheFlagUtils.generateMatchPairings(teamNames);
        maxRounds = unPlayedMatchPairings.size() / storageUtil.getArenas().size();
        playedRounds = 0;
        gameActive = true;
        startNextRound();
        Bukkit.getLogger().info("Starting Capture the Flag");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        sidebar.addPlayer(participant);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(storageUtil.getSpawnObservatory());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        if (currentRound != null && currentRound.isActive()) {
            currentRound.stop();
        }
        gameActive = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Capture the Flag");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        sidebar.removePlayer(participant.getUniqueId());
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        initializeParticipant(participant);
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round %d/%d", playedRounds, maxRounds))
        );
        if (currentRound != null) {
            currentRound.onParticipantJoin(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (currentRound != null) {
            currentRound.onParticipantJoin(participant);
        }
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    /**
     * Tells the game that the current round is over. If there are no rounds left, ends the game. If there are rounds left, starts the next round.
     */
    public void roundIsOver() {
        if (allMatchPairingsArePlayed()) {
            stop();
            return;
        }
        startNextRound();
    }

    /**
     * @return true if there are no more un-played match pairings
     */
    private boolean allMatchPairingsArePlayed() {
        return unPlayedMatchPairings.isEmpty();
    }
    
    private void startNextRound() {
        List<MatchPairing> roundMatchPairings = chooseNextMatchPairings();
        currentRound = new CaptureTheFlagRound(this, plugin, gameManager, storageUtil, roundMatchPairings, sidebar, adminSidebar);
        playedRounds++;
        List<Player> roundParticipants = new ArrayList<>();
        List<Player> onDeckParticipants = new ArrayList<>();
        for (Player participant : participants) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamName);
            String oppositeTeam = currentRound.getOppositeTeam(teamName);
            if (oppositeTeam != null) {
                roundParticipants.add(participant);
                Component oppositeTeamDisplayName = gameManager.getFormattedTeamDisplayName(oppositeTeam);
                participant.sendMessage(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" is competing against "))
                        .append(oppositeTeamDisplayName)
                        .append(Component.text(" this round.")));
            } else {
                onDeckParticipants.add(participant);
                int participantsNextRoundIndex = getNextRoundNumber();
                if (participantsNextRoundIndex <= 0) {
                    participant.sendMessage(Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" has no more rounds. They've competed against every team.")));
                } else {
                    participant.sendMessage(Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" is not competing in this round. Their next round is "))
                            .append(Component.text(participantsNextRoundIndex)));
                }
            }
        }
        currentRound.start(roundParticipants, onDeckParticipants);
        String round = String.format("Round %d/%d", playedRounds, maxRounds);
        sidebar.updateLine("round", round);
        adminSidebar.updateLine("round", round);
    }
    
    /**
     * Chooses the MatchPairings from the unPlayedMatchPairings for the next round (based on the number of arenas)
     * prioritizing teams which have been on-deck the longest
     * @return the match pairings that the next round should have (size will match the number of arenas in the config)
     */
    private List<MatchPairing> chooseNextMatchPairings() {
        List<String> sortedTeams = onDeckRounds.entrySet()
                .stream()
                .sorted(Comparator.comparing((Map.Entry<String, OnDeckRounds> entry) -> entry.getValue().roundsSpentOnDeck()).reversed()
                        .thenComparing(entry -> entry.getValue().lastPlayedRound()))
                .map(Map.Entry::getKey)
                .toList();
        int numOfArenas = storageUtil.getArenas().size();
        List<MatchPairing> newMatchPairings = new ArrayList<>(numOfArenas);
        for (int i = 0; i < Math.min(numOfArenas, sortedTeams.size()); i++) {
            String teamA = sortedTeams.get(i);
            for (int j = i+1; j < sortedTeams.size(); j++) {
                String teamB = sortedTeams.get(j);
                MatchPairing newMatchPairing = new MatchPairing(teamA, teamB);
                if (!listContainsMatchPairing(playedMatchPairings, newMatchPairing) && !listContainsMatchPairing(newMatchPairings, newMatchPairing)) {
                    newMatchPairings.add(newMatchPairing);
                }
            }
        }
        return newMatchPairings;
    }
    
    /**
     * @param matchPairings the list to check if it contains matchPairing
     * @param matchPairing the MatchPairing to check if matchPairings contains
     * @return true if matchPairings contains a MatchPairing (agnostic of which team is north or south)
     */
    private boolean listContainsMatchPairing(List<MatchPairing> matchPairings, MatchPairing matchPairing) {
        for (MatchPairing eachMatchPairing : matchPairings) {
            if (eachMatchPairing.containsTeam(matchPairing.northTeam()) && eachMatchPairing.containsTeam(matchPairing.southTeam())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the number of the next round if there is a next round.
     * @return The index of the next round, 0 if there are no more next rounds.
     */
    private int getNextRoundNumber() {
        int nextRound = playedRounds + 2;
        return nextRound > maxRounds ? 0 : nextRound;
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (currentRound == null) {
            return;
        }
        if (currentRound.isAliveInMatch(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("round", ""),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("enemy", ""),
                new KeyLine("round", ""),
                new KeyLine("timer", ""),
                new KeyLine("kills", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    @Override
    public void updateTeamScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    @Override
    public void updatePersonalScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    /**
     * Messages all the participants of the game (whether they're in a match or not)
     * @param message The message to send
     */
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    // Testing methods
    /**
     * Returns a copy of the list of participants. Not the actual list, modifying the return value
     * of this function does not modify the actual list of participants.
     * @return A copy of the list of participants
     */
    public List<Player> getParticipants() {
        return new ArrayList<>(participants);
    }
    
    public @Nullable CaptureTheFlagRound getCurrentRound() {
        return currentRound;
    }
}
