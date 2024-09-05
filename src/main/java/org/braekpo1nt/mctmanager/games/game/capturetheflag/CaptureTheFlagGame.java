package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.CaptureTheFlagState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public class CaptureTheFlagGame implements MCTGame, Configurable, Listener, Headerable {
    
    public @Nullable CaptureTheFlagState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final BattleTopbar topbar;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Capture the Flag"))
            .color(NamedTextColor.BLUE);
    private final TimerManager timerManager;
    private Component title = baseTitle;
    private RoundManager roundManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private CaptureTheFlagConfigController configController;
    private CaptureTheFlagConfig config;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private Map<UUID, Integer> killCount = new HashMap<>();
    private Map<UUID, Integer> deathCount = new HashMap<>();
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new CaptureTheFlagConfigController(plugin.getDataFolder());
        this.topbar = new BattleTopbar();
        
    }
    
    @Override
    public GameType getType() {
        return GameType.CAPTURE_THE_FLAG;
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        if (sidebar != null) {
            sidebar.updateLine("title", title);
        }
        if (adminSidebar != null) {
            adminSidebar.updateLine("title", title);
        }
    }
    
    @Override
    public @NotNull Component getBaseTitle() {
        return baseTitle;
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        if (state != null) {
            throw new ConfigException("CaptureTheFlagGame does not support loading the config mid-game");
        }
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        participants = new ArrayList<>(newParticipants.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        List<String> teamIds = gameManager.getTeamIds(newParticipants);
        roundManager = new RoundManager(teamIds, config.getArenas().size());
        killCount = new HashMap<>(newParticipants.size());
        deathCount = new HashMap<>(newParticipants.size());
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        setState(new DescriptionState(this));
        Main.logger().info("Starting Capture the Flag");
    }
    
    public void initializeParticipant(Player participant) {
        participants.add(participant);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        killCount.putIfAbsent(participant.getUniqueId(), 0);
        deathCount.putIfAbsent(participant.getUniqueId(), 0);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(config.getSpawnObservatory());
        participant.setRespawnLocation(config.getSpawnObservatory());
        ParticipantInitializer.clearInventory(participant);
        int kills = killCount.get(participant.getUniqueId());
        int deaths = deathCount.get(participant.getUniqueId());
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
    }
    
    public void resetParticipant(Player participant) {
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        sidebar.removePlayer(participant.getUniqueId());
        topbar.hidePlayer(participant.getUniqueId());
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
        admin.teleport(config.getSpawnObservatory());
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        if (state != null) {
            state.stop();
        }
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        clearSidebar();
        stopAdmins();
        state = null;
        gameManager.gameIsOver();
        Main.logger().info("Stopping Capture the Flag");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (state == null) {
            return;
        }
        state.onParticipantJoin(participant);
        if (sidebar != null) {
            sidebar.updateLine(participant.getUniqueId(), "title", title);
        }
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (state == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        state.onParticipantQuit(participant);
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
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    /**
     * @param playerUUID the player to add a kill to
     */
    public void addKill(@NotNull UUID playerUUID) {
        int oldKillCount = killCount.get(playerUUID);
        int newKillCount = oldKillCount + 1;
        killCount.put(playerUUID, newKillCount);
        topbar.setKills(playerUUID, newKillCount);
    }
    
    /**
     * @param playerUUID the player to add a death to
     */
    public void addDeath(@NotNull UUID playerUUID) {
        int oldDeathCount = deathCount.get(playerUUID);
        int newDeathCount = oldDeathCount + 1;
        deathCount.put(playerUUID, newDeathCount);
        topbar.setDeaths(playerUUID, newDeathCount);
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
    
    @Override
    public void updateTeamScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    @Override
    public void updatePersonalScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == null) {
            return;
        }
        state.onPlayerDeath(event);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state == null) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (state == null) {
            return;
        }
        Player participant = (Player) event.getEntity();
        if (!participants.contains(participant)) {
            return;
        }
        state.onPlayerLoseHunger(event);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (state == null) {
            return;
        }
        Player participant = (Player) event.getWhoClicked();
        if (!participants.contains(participant)) {
            return;
        }
        state.onClickInventory(event);
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (state == null) {
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
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (state == null) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        boolean cancelled = handleSpectators(event);
        if (cancelled) {
            return;
        }
        state.onPlayerMove(event);
    }
    
    /**
     * prevent spectators from leaving the spectator area
     * @param event the move event
     * @return true if the event was cancelled due to preventing spectator movement
     */
    private boolean handleSpectators(PlayerMoveEvent event) {
        if (config.getSpectatorArea() == null){
            return false;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return false;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getSpawnObservatory());
            return false;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (state == null) {
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
     * Messages all the participants of the game (whether they're in a match or not)
     * @param message The message to send
     */
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
}
