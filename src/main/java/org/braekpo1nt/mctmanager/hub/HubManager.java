package org.braekpo1nt.mctmanager.hub;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.config.HubConfigController;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class HubManager implements Listener, Configurable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final HubConfigController configController;
    private HubConfig config;
    private int returnToHubTaskId;
    /**
     * Contains a list of the players who are about to be sent to the hub and can see the countdown
     */
    private final List<Player> headingToHub = new ArrayList<>();
    private boolean boundaryEnabled = true;
    /**
     * A list of the participants who are in the hub
     */
    private final List<Player> participants = new ArrayList<>();
    
    public HubManager(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.configController = new HubConfigController(plugin.getDataFolder());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        this.config = configController.getConfig();
        return true;
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
        final Sidebar sidebar = gameManager.getSidebarFactory().createSidebar();
        sidebar.addPlayers(newParticipants);
        sidebar.addPlayers(newAdmins);
        sidebar.addLine("backToHub", String.format("Back to Hub: %s", duration));
        this.returnToHubTaskId = new BukkitRunnable() {
            private int count = duration;
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.deleteAllLines();
                    sidebar.removeAllPlayers();
                    returnParticipantsToHubInstantly(headingToHub, adminsHeadingToHub);
                    headingToHub.clear();
                    adminsHeadingToHub.clear();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                sidebar.updateLine("backToHub", String.format("Back to Hub: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
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
        initializeParticipant(participant);
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participant.getInventory().clear();
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
    
    public void onParticipantQuit(Player participant) {
        participants.remove(participant);
    }
    
    /**
     * Should be called when the participant who joined should be in the hub
     * @param participant the participant to add
     */
    public void onParticipantJoin(Player participant) {
        participants.add(participant);
    }
    
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
    }
    
    public void onAdminQuit(Player admin) {
        
    }
    
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(returnToHubTaskId);
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
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (participants.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (headingToHub.contains(participant)) {
            event.setCancelled(true);
        }
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
    
    public void initializeSidebar(SidebarFactory sidebarFactory) {
//        sidebar = sidebarFactory.createSidebar();
    }
}
