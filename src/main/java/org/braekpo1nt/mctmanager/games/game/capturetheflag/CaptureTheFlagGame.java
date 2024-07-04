package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

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
    private final BattleTopbar topbar;
    private CaptureTheFlagConfigController configController;
    private CaptureTheFlagConfig config;
    private RoundManager roundManager;
    private CaptureTheFlagRound currentRound;
    private final String baseTitle = ChatColor.BLUE+"Capture the Flag";
    private String title = baseTitle;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private Map<UUID, Integer> killCount = new HashMap<>();
    private Map<UUID, Integer> deathCount = new HashMap<>();
    private boolean firstRound = true;
    private boolean gameActive = false;
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.configController = new CaptureTheFlagConfigController(plugin.getDataFolder());
        this.topbar = new BattleTopbar();
    }
    
    @Override
    public void setTitle(@NotNull String title) {
        this.title = title;
        if (sidebar != null) {
            sidebar.updateLine("title", title);
        }
        if (adminSidebar != null) {
            adminSidebar.updateLine("title", title);
        }
    }
    
    @Override
    public @NotNull String getBaseTitle() {
        return baseTitle;
    }
    
    @Override
    public GameType getType() {
        return GameType.CAPTURE_THE_FLAG;
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        if (gameActive) {
            throw new ConfigException("CaptureTheFlagGame does not support loading the config mid-game");
        }
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        participants = new ArrayList<>(newParticipants.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        roundManager = new RoundManager(this, config.getArenas().size());
        killCount = new HashMap<>(newParticipants.size());
        deathCount = new HashMap<>(newParticipants.size());
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        displayDescription();
        gameActive = true;
        firstRound = true;
        List<String> teams = gameManager.getTeamNames(participants);
        roundManager.start(teams);
        Bukkit.getLogger().info("Starting Capture the Flag");
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        killCount.putIfAbsent(participant.getUniqueId(), 0);
        deathCount.putIfAbsent(participant.getUniqueId(), 0);
        int kills = killCount.get(participant.getUniqueId());
        int deaths = deathCount.get(participant.getUniqueId());
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
        String roundLine = String.format("Round %d/%d", roundManager.getPlayedRounds() + 1, roundManager.getMaxRounds());
        adminSidebar.updateLine("round", roundLine);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getSpawnObservatory());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        if (currentRound != null && currentRound.isActive()) {
            currentRound.stop();
        }
        gameActive = false;
        firstRound = true;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Capture the Flag");
    }
    
    private void cancelAllTasks() {
        
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        sidebar.removePlayer(participant.getUniqueId());
        topbar.hidePlayer(participant.getUniqueId());
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
        if (currentRound != null) {
            currentRound.onParticipantJoin(participant);
        }
        String team = gameManager.getTeamName(participant.getUniqueId());
        roundManager.onTeamJoin(team);
        sidebar.updateLine(participant.getUniqueId(), "title", title);
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        sidebar.updateLine("round", roundLine);
        adminSidebar.updateLine("round", roundLine);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (currentRound != null) {
            currentRound.onParticipantQuit(participant);
        }
        resetParticipant(participant);
        participants.remove(participant);
        String quittingTeam = gameManager.getTeamName(participant.getUniqueId());
        if (entireTeamHasQuit(quittingTeam)) {
            roundManager.onTeamQuit(quittingTeam);
        }
        String roundLine = String.format("Round %d/%d", roundManager.getPlayedRounds() + 1, roundManager.getMaxRounds());
        sidebar.updateLine("round", roundLine);
        adminSidebar.updateLine("round", roundLine);
    }
    
    /**
     * @param quittingTeam the team of the quitting player
     * @return false if there is at least one member of quittingTeam in the game, true otherwise
     */
    private boolean entireTeamHasQuit(String quittingTeam) {
        for (Player player : participants) {
            String team = gameManager.getTeamName(player.getUniqueId());
            // if there is still at least one team member in the game who is on this team
            if (quittingTeam.equals(team)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Tells the game that the current round is over. If there are no rounds left, ends the game. If there are rounds left, starts the next round.
     */
    public void roundIsOver() {
        roundManager.roundIsOver();
    }
    
    public void startNextRound(List<String> participantTeams, List<MatchPairing> roundMatchPairings) {
        
        currentRound = new CaptureTheFlagRound(this, plugin, gameManager, config, roundMatchPairings, sidebar, adminSidebar, topbar);
        List<Player> roundParticipants = new ArrayList<>();
        List<Player> onDeckParticipants = new ArrayList<>();
        for (Player participant : participants) {
            String teamId = gameManager.getTeamName(participant.getUniqueId());
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
            if (participantTeams.contains(teamId)) {
                announceMatchToParticipant(participant, teamId, teamDisplayName);
                roundParticipants.add(participant);
            } else {
                participant.sendMessage(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" is on-deck this round."))
                        .color(NamedTextColor.YELLOW));
                onDeckParticipants.add(participant);
            }
        }
        setUpTopbarForRound(roundMatchPairings);
        currentRound.setFirstRound(firstRound);
        firstRound = false; // the first round only happens once
        currentRound.start(roundParticipants, onDeckParticipants);
        String round = String.format("Round %d/%d", roundManager.getPlayedRounds() + 1, roundManager.getMaxRounds());
        sidebar.updateLine("round", round);
        adminSidebar.updateLine("round", round);
    }
    
    private void setUpTopbarForRound(List<MatchPairing> roundMatchPairings) {
        topbar.removeAllTeamPairs();
        for (MatchPairing mp : roundMatchPairings) {
            NamedTextColor northColor = gameManager.getTeamNamedTextColor(mp.northTeam());
            NamedTextColor southColor = gameManager.getTeamNamedTextColor(mp.southTeam());
            topbar.addTeam(mp.northTeam(), northColor);
            topbar.addTeam(mp.southTeam(), southColor);
            topbar.linkTeamPair(mp.northTeam(), mp.southTeam());
            int northAlive = 0;
            int southAlive = 0;
            for (Player participant : participants) {
                String teamId = gameManager.getTeamName(participant.getUniqueId());
                if (mp.northTeam().equals(teamId)) {
                    topbar.linkToTeam(participant.getUniqueId(), teamId);
                    northAlive++;
                } else if (mp.southTeam().equals(teamId)) {
                    topbar.linkToTeam(participant.getUniqueId(), teamId);
                    southAlive++;
                }
            }
            topbar.setMembers(mp.northTeam(), northAlive, 0);
            topbar.setMembers(mp.southTeam(), southAlive, 0);
        }
        topbar.setNoTeamLeft(Component.text("On Deck")
                .color(NamedTextColor.GRAY));
    }
    
    /**
     * Send a message to the participant who they are fighting against in the current match
     * @param participant the participant to send the message to
     * @param team the team that the participant is on
     */
    private void announceMatchToParticipant(Player participant, String team, Component teamDisplayName) {
        String oppositeTeam = currentRound.getOppositeTeam(team);
        Component oppositeTeamDisplayName = gameManager.getFormattedTeamDisplayName(oppositeTeam);
        participant.sendMessage(Component.empty()
                .append(teamDisplayName)
                .append(Component.text(" is competing against "))
                .append(oppositeTeamDisplayName)
                .append(Component.text(" this round."))
                .color(NamedTextColor.YELLOW));
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
        currentRound.onPlayerDamage(participant, event);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!gameActive) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (currentRound == null) {
            participant.setFoodLevel(20);
            event.setCancelled(true);
            return;
        }
        currentRound.onPlayerLoseHunger(participant, event);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.contains(event.getPlayer())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getSpawnObservatory());
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.contains(event.getPlayer())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Stop players from removing their equipment
     */
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!participants.contains(participant)) {
            return;
        }
        if (currentRound == null) {
            event.setCancelled(true);
            return;
        }
        currentRound.onClickInventory(participant, event);
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!gameActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameActive) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!participants.contains(event.getPlayer())) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
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
        plugin.getLogger().info("delete all lines");
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("round", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
        topbar.removeAllTeamPairs();
        topbar.hideAllPlayers();
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
    
    /**
     * @param playerUUID the player to add a kill to
     */
    void addKill(@NotNull UUID playerUUID) {
        int oldKillCount = killCount.get(playerUUID);
        int newKillCount = oldKillCount + 1;
        killCount.put(playerUUID, newKillCount);
        topbar.setKills(playerUUID, newKillCount);
    }
    
    /**
     * @param playerUUID the player to add a death to
     */
    void addDeath(@NotNull UUID playerUUID) {
        int oldDeathCount = deathCount.get(playerUUID);
        int newDeathCount = oldDeathCount + 1;
        deathCount.put(playerUUID, newDeathCount);
        topbar.setDeaths(playerUUID, newDeathCount);
    }
    
    // Testing methods
    
    /**
     * No-arg constructor for testing purposes only
     */
    CaptureTheFlagGame() {
        this.plugin = null;
        this.gameManager = null;
        this.config = null;
        this.topbar = new BattleTopbar();
    }
    
    /**
     * Returns a copy of the list of participants. Not the actual list, modifying the return value
     * of this function does not modify the actual list of participants.
     * @return A copy of the list of participants
     */
    public List<Player> getParticipants() {
        return new ArrayList<>(participants);
    }
    
    /**
     * @return The number of rounds that have been played already in this game
     */
    public int getPlayedRounds() {
        return roundManager.getPlayedRounds();
    }
    
    /**
     * @return the maximum number of rounds
     */
    public int getMaxRounds() {
        return roundManager.getMaxRounds();
    }
    
    public CaptureTheFlagRound getCurrentRound() {
        return currentRound;
    }
    
    public boolean isGameActive() {
        return gameActive;
    }
}
