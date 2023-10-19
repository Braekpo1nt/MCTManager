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
    private RoundManager roundManager;
    private CaptureTheFlagRound currentRound;
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
        roundManager = new RoundManager(this, storageUtil.getArenas().size());
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        gameActive = true;
        List<String> teams = gameManager.getTeamNames(participants);
        roundManager.start(teams);
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
                new KeyLine("round", String.format("Round %d/%d", roundManager.getPlayedRounds(), roundManager.getMaxRounds()))
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
        roundManager.roundIsOver();
    }
        
    public void startNextRound(List<String> participantTeams, List<MatchPairing> roundMatchPairings) {
        currentRound = new CaptureTheFlagRound(this, plugin, gameManager, storageUtil, roundMatchPairings, sidebar, adminSidebar);
        List<Player> roundParticipants = new ArrayList<>();
        List<Player> onDeckParticipants = new ArrayList<>();
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (participantTeams.contains(team)) {
                roundParticipants.add(participant);
            } else {
                onDeckParticipants.add(participant);
            }
        }
        currentRound.start(roundParticipants, onDeckParticipants);
        String round = String.format("Round %d/%d", roundManager.getPlayedRounds() + 1, roundManager.getMaxRounds());
        sidebar.updateLine("round", round);
        adminSidebar.updateLine("round", round);
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
}
