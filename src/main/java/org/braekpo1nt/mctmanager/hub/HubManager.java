package org.braekpo1nt.mctmanager.hub;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.config.HubConfigController;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HubManager implements Listener, Configurable {
    
    private final GameManager gameManager;
    protected final HubConfigController configController;
    protected HubConfig config;
    private @NotNull final List<LeaderboardManager> leaderboardManagers;
    /**
     * Contains a list of the players who are about to be sent to the hub and can see the countdown
     */
    private final Set<Player> headingToHub = new HashSet<>();
    private boolean boundaryEnabled = true;
    /**
     * A list of the participants who are in the hub
     */
    private final Set<Player> participants = new HashSet<>();
    private final TimerManager timerManager;
    
    public HubManager(Main plugin, GameManager gameManager) {
        this.gameManager = gameManager;
        this.timerManager = gameManager.getTimerManager().createManager();
        this.configController = new HubConfigController(plugin.getDataFolder());
        this.leaderboardManagers = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
        for (LeaderboardManager leaderboardManager : leaderboardManagers) {
            leaderboardManager.tearDown();
        }
        leaderboardManagers.clear();
        for (HubConfig.Leaderboard leaderboard : config.getLeaderboards()) {
            LeaderboardManager leaderboardManager = new LeaderboardManager(
                    gameManager, 
                    leaderboard.getTitle(), 
                    leaderboard.getLocation(), 
                    leaderboard.getTopPlayers()
            );
            for (Player participant : participants) {
                leaderboardManager.onParticipantJoin(participant);
            }
            leaderboardManager.updateScores();
            leaderboardManagers.add(leaderboardManager);
        }
    }
    
    /**
     * Returns the participants to the hub (either instantly, or with a delay)
     * @param newParticipants the participants to send to the hub
     * @param delay false will perform the teleport instantaneously, true will teleport with a delay
     */
    public void returnParticipantsToHub(List<Player> newParticipants, List<Player> newAdmins, boolean delay) {
        if (delay) {
            returnParticipantsToHub(new ArrayList<>(newParticipants), new ArrayList<>(newAdmins), config.getTpToHubDuration());
        } else {
            returnParticipantsToHubInstantly(new ArrayList<>(newParticipants), new ArrayList<>(newAdmins));
        }
    }
    
    private void returnParticipantsToHub(List<Player> newParticipants, List<Player> newAdmins, int duration) {
        headingToHub.addAll(newParticipants);
        final List<Player> adminsHeadingToHub = new ArrayList<>(newAdmins);
        final Sidebar sidebar = gameManager.createSidebar();
        sidebar.addPlayers(newParticipants);
        sidebar.addPlayers(newAdmins);
        sidebar.addLine("backToHub", String.format("Back to Hub: %s", duration));
        timerManager.start(Timer.builder()
                .duration(duration)
                .withSidebar(sidebar, "backToHub")
                .sidebarPrefix(Component.text("Back to Hub: "))
                .onCompletion(() -> {
                    sidebar.deleteAllLines();
                    sidebar.removeAllPlayers();
                    returnParticipantsToHubInstantly(new ArrayList<>(headingToHub), adminsHeadingToHub);
                    headingToHub.clear();
                    adminsHeadingToHub.clear();
                })
                .build());
    }
    
    private void returnParticipantsToHubInstantly(List<Player> newParticipants, List<Player> newAdmins) {
        for (Player participant : newParticipants) {
            returnParticipantToHub(participant);
        }
        for (Player admin : newAdmins) {
            returnAdminToHub(admin);
        }
        setupTeamOptions();
    }
    
    private void returnParticipantToHub(Player participant) {
        participant.sendMessage(Component.text("Returning to hub"));
        participant.teleport(config.getSpawn());
        participant.setRespawnLocation(config.getSpawn(), true);
        initializeParticipant(participant);
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void returnAdminToHub(Player admin) {
        admin.sendMessage(Component.text("Returning to hub"));
        admin.teleport(config.getSpawn());
        initializeAdmin(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admin.setGameMode(GameMode.SPECTATOR);
    }
    
    public void sendAllParticipantsToPodium(List<Player> winningTeamParticipants, List<Player> otherParticipants, List<Player> newAdmins) {
        setupTeamOptions();
        for (Player participant : otherParticipants) {
            sendParticipantToPodium(participant, false);
        }
        for (Player winningParticipant : winningTeamParticipants) {
            sendParticipantToPodium(winningParticipant, true);
        }
        for (Player admin : newAdmins) {
            sendAdminToPodium(admin);
        }
    }
    
    public void sendParticipantToPodium(Player participant, boolean winner) {
        participant.sendMessage(Component.text("Returning to hub"));
        if (winner) {
            participant.teleport(config.getPodium());
        } else {
            participant.teleport(config.getPodiumObservation());
        }
        participant.setRespawnLocation(config.getSpawn(), true);
        initializeParticipant(participant);
    }
    
    private void sendAdminToPodium(Player admin) {
        admin.sendMessage(Component.text("Returning to hub"));
        admin.teleport(config.getPodiumObservation());
    }
    
    /**
     * Removes the given list of participants from the hub.
     * @param participantsToRemove the participants who are leaving the hub
     */
    public void removeParticipantsFromHub(List<Player> participantsToRemove) {
        for (Player participant : participantsToRemove) {
            participants.remove(participant);
        }
    }
    
    /**
     * Should be called when the participant who joined should be in the hub
     * @param participant the participant to add
     */
    public void onParticipantJoin(Player participant) {
        participants.add(participant);
        for (LeaderboardManager leaderboardManager : leaderboardManagers) {
            leaderboardManager.onParticipantJoin(participant);
        }
    }
    
    public void onParticipantQuit(Player participant) {
        participants.remove(participant);
        for (LeaderboardManager leaderboardManager : leaderboardManagers) {
            leaderboardManager.onParticipantQuit(participant);
        }
        participant.setRespawnLocation(config.getSpawn(), true);
    }
    
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
    }
    
    public void onAdminQuit(Player admin) {
        // do nothing
    }
    
    public void updateLeaderboards() {
        for (LeaderboardManager leaderboardManager : leaderboardManagers) {
            leaderboardManager.updateScores();
        }
    }
    
    public void tearDown() {
        cancelAllTasks();
        for (LeaderboardManager leaderboardManager : leaderboardManagers) {
            leaderboardManager.tearDown();
        }
    }
    
    public void cancelAllTasks() {
        timerManager.cancel();
    }
    
    private void setupTeamOptions() {
        for (Team team : gameManager.getMctScoreboard().getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (participants.contains(participant)) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "HubManager.onPlayerDamage()->participant contains cancelled");
            event.setCancelled(true);
            return;
        }
        if (headingToHub.contains(participant)) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "HubManager.onPlayerDamage()->headingToHub contains cancelled");
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant) && !headingToHub.contains(participant)) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (!gameManager.getEventManager().eventIsActive()) {
            return;
        }
        if (gameManager.gameIsRunning()) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (participants.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (headingToHub.contains(participant)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!gameManager.getEventManager().eventIsActive()) {
            return;
        }
        Player participant = event.getPlayer();
        if (participants.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (headingToHub.contains(participant)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (participants.contains(participant)) {
            participant.setFoodLevel(20);
            event.setCancelled(true);
            return;
        }
        if (headingToHub.contains(participant)) {
            participant.setFoodLevel(20);
            event.setCancelled(true);
        }
    }
    
    /**
     * Detects when the player moves out of bounds of the hub, and teleports them back to the starting place
     * @param event A player move event
     */
    @EventHandler
    public void onPlayerOutOfBounds(PlayerMoveEvent event) {
        if (!boundaryEnabled) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        if (!participant.getWorld().equals(config.getWorld())) {
            return;
        }
        Location location = participant.getLocation();
        if (location.getY() < config.getYLimit()) {
            participant.teleport(config.getSpawn());
            participant.sendMessage("You fell out of the hub boundary");
        }
    }
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        this.boundaryEnabled = boundaryEnabled;
    }
}
