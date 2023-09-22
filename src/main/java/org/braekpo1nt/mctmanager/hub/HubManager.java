package org.braekpo1nt.mctmanager.hub;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubStorageUtil;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class HubManager implements Listener, Configurable {
    
    private final Main plugin;
    private final GameManager gameManager;
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
    
    public void returnParticipantsToHubWithDelay(List<Player> newParticipants) {
        headingToHub.addAll(newParticipants);
        gameManager.getSidebarManager().addLine("backToHub", "");
        this.returnToHubTaskId = new BukkitRunnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    gameManager.getSidebarManager().deleteLine("backToHub");
                    returnParticipantsToHub(newParticipants);
                    headingToHub.clear();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                gameManager.getSidebarManager().updateLine("backToHub", String.format("Back to Hub: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * Returns the participants to the hub instantly, without a delay
     * @param newParticipants the participants to send to the hub
     */
    public void returnParticipantsToHub(List<Player> newParticipants) {
        for (Player participant : newParticipants){
            returnParticipantToHub(participant);
        }
        setupTeamOptions();
    }
    
    public void returnParticipantToHub(Player participant) {
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
            this.participants.remove(participant);
        }
    }
    
    public void onParticipantQuit(Player participant) {
        this.participants.remove(participant);
    }
    
    /**
     * if the given participant is in the hub world, it will be added to the list of hub participants
     * @param participant the participant to add
     */
    public void onParticipantJoin(Player participant) {
        if (!participant.getWorld().equals(storageUtil.getWorld())) {
            return;
        }
        participants.add(participant);
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().equals(storageUtil.getWorld())) {
            return;
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
    
    @EventHandler
    public void teleportListener(PlayerTeleportEvent event) {
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        if (event.getTo().getWorld().equals(storageUtil.getWorld())) {
            if (!event.getFrom().getWorld().equals(storageUtil.getWorld())) {
                initializeParticipant(participant);
            }
        } else {
            this.participants.remove(participant);
        }
    }
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        this.boundaryEnabled = boundaryEnabled;
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
}
