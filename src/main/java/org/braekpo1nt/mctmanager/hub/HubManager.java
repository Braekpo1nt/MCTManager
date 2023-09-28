package org.braekpo1nt.mctmanager.hub;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubStorageUtil;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class HubManager implements Listener, Configurable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private final HubStorageUtil storageUtil;
    private int returnToHubTaskId;
    /**
     * Contains a list of the players who are about to be sent to the hub and can see the countdown from the {@link HubManager#returnParticipantsToHubWithDelay(List)}
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
        this.storageUtil = new HubStorageUtil(plugin.getDataFolder());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    /**
     * Returns the participants to the hub instantly, without a delay
     * @param newParticipants the participants to send to the hub
     * @param delay false will perform the teleport instantaneously, true will teleport with a delay
     */
    public void returnParticipantsToHub(List<Player> newParticipants, boolean delay) {
        if (delay) {
            returnParticipantsToHub(newParticipants, storageUtil.getTpToHubDuration());
        } else {
            returnParticipantsToHub(newParticipants, -1);
        }
    }
    
    private void returnParticipantsToHub(List<Player> newParticipants, int duration) {
        headingToHub.addAll(newParticipants);
        sidebar.addPlayers(newParticipants);
        sidebar.addLine("backToHub", "");
        this.returnToHubTaskId = new BukkitRunnable() {
            private int count = duration;
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.deleteLine("backToHub");
                    for (Player participant : newParticipants) {
                        returnParticipantToHub(participant);
                    }
                    setupTeamOptions();
                    headingToHub.clear();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                sidebar.updateLine("backToHub", String.format("Back to Hub: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void returnParticipantToHub(Player participant) {
        participant.sendMessage(Component.text("Returning to hub"));
        participant.teleport(storageUtil.getSpawn());
        initializeParticipant(participant);
    }
    
    public void sendParticipantsToPodium(List<Player> winningTeamParticipants, List<Player> otherParticipants) {
        setupTeamOptions();
        for (Player participant : otherParticipants) {
            sendParticipantToPodium(participant, false);
        }
        for (Player winningParticipant : winningTeamParticipants) {
            sendParticipantToPodium(winningParticipant, true);
        }
    }
    
    private void sendParticipantToPodium(Player participant, boolean winner) {
        participant.sendMessage(Component.text("Returning to hub"));
        if (winner) {
            participant.teleport(storageUtil.getPodium());
        } else {
            participant.teleport(storageUtil.getPodiumObservation());
        }
        initializeParticipant(participant);
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * Removes the given list of participants from the hub.
     * @param participantsToRemove the participants who are leaving the hub
     */
    public void removeParticipantsFromHub(List<Player> participantsToRemove) {
        for (Player participant : participantsToRemove) {
            if (this.participants.remove(participant)) {
                sidebar.removePlayer(participant.getUniqueId());
            }
        }
    }
    
    public void onParticipantQuit(Player participant) {
        if (this.participants.remove(participant)) {
            sidebar.removePlayer(participant.getUniqueId());
        }
    }
    
    /**
     * Should be called when the participant who joined should be in the hub
     * @param participant the participant to add
     */
    public void onParticipantJoin(Player participant) {
        participants.add(participant);
        sidebar.addPlayer(participant);
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
        if (!participants.contains(participant)) {
            return;
        }
        if (headingToHub.contains(participant) || participant.getWorld().equals(storageUtil.getWorld())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (!headingToHub.contains(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
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
        if (!participant.getWorld().equals(storageUtil.getWorld())) {
            return;
        }
        Location location = participant.getLocation();
        if (location.getY() < storageUtil.getYLimit()) {
            participant.teleport(storageUtil.getSpawn());
            participant.sendMessage("You fell out of the hub boundary");
        }
    }
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        this.boundaryEnabled = boundaryEnabled;
    }
    
    public void initializeSidebar(SidebarFactory sidebarFactory) {
        sidebar = sidebarFactory.createSidebar();
    }
}
