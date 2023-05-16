package org.braekpo1nt.mctmanager.hub;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class HubManager implements Listener {
    
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 200, true, false, false);
    private final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 70, 200, true, false, false);
    private final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70, 1, true, false, false);
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private final World hubWorld;
    private final Main plugin;
    private final Scoreboard mctScoreboard;
    private final FastBoardManager fastBoardManager;
    private final GameManager gameManager;
    private int returnToHubTaskId;
    private final Location observePedestalLocation;
    private final Location pedestalLocation;
    /**
     * Contains a list of the players who are about to be sent to the hub and can see the countdown from the {@link HubManager#returnParticipantsToHubWithDelay(List)}
     */
    private final List<Player> headingToHub = new ArrayList<>();
    private boolean boundaryEnabled = true;
    /**
     * A list of the participants who are in the hub
     */
    private final List<Player> participants = new ArrayList<>();
    private int hubTimerTaskId;
    private boolean hubTimerPaused = false;
    
    public HubManager(Main plugin, Scoreboard mctScoreboard, FastBoardManager fastBoardManager, GameManager gameManager) {
        this.plugin = plugin;
        this.mctScoreboard = mctScoreboard;
        this.fastBoardManager = fastBoardManager;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.hubWorld = worldManager.getMVWorld("Hub").getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        observePedestalLocation = anchorManager.getAnchorLocation("pedestal-view");
        pedestalLocation = anchorManager.getAnchorLocation("pedestal");
        initializedStatusEffectLoop();
    }
    
    public void returnParticipantsToHubWithDelay(List<Player> newParticipants) {
        headingToHub.addAll(newParticipants);
        this.returnToHubTaskId = new BukkitRunnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    returnParticipantsToHub(newParticipants);
                    headingToHub.clear();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : newParticipants) {
                    updateReturnToHubTimerFastBoard(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void pauseHubTimer() {
        hubTimerPaused = true;
    }
    
    public void kickOffHubTimer() {
        hubTimerPaused = false;
        for (Player participant : participants) {
            initializeHubDelayCountDown(participant, "");
        }
        this.hubTimerTaskId = new BukkitRunnable() {
            int count = 20;
            @Override
            public void run() {
                if (hubTimerPaused) {
                    return;
                }
                if (count <= 0) {
                    gameManager.startVote();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : participants) {
                    updateHubTimerDisplay(participant, timeString);
                }
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
        if (gameManager.eventIsActive()) {
            kickOffHubTimer();
        }
    }
    
    public void returnParticipantToHub(Player participant) {
        participant.sendMessage(Component.text("Returning to hub"));
        participant.teleport(hubWorld.getSpawnLocation());
        initializeParticipant(participant);
    }
    
    public void sendParticipantsToPedestal(List<Player> winningTeamParticipants, String winningTeam, ChatColor winningChatColor, List<Player> otherParticipants) {
        headingToHub.addAll(winningTeamParticipants);
        headingToHub.addAll(otherParticipants);
        this.returnToHubTaskId = new BukkitRunnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    setupTeamOptions();
                    for (Player participant : otherParticipants) {
                        sendParticipantToPedestal(participant, false, winningTeam, winningChatColor);
                    }
                    for (Player winningParticipant : winningTeamParticipants) {
                        sendParticipantToPedestal(winningParticipant, true, winningTeam, winningChatColor);
                    }
                    headingToHub.clear();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : winningTeamParticipants) {
                    updateReturnToHubTimerFastBoard(participant, timeString);
                }
                for (Player participant : otherParticipants) {
                    updateReturnToHubTimerFastBoard(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void sendParticipantToPedestal(Player participant, boolean winner, String winningTeam, ChatColor winningChatColor) {
        participant.sendMessage(Component.text("Returning to hub"));
        if (winner) {
            participant.teleport(pedestalLocation);
        } else {
            participant.teleport(observePedestalLocation);
        }
        initializeParticipant(participant);
        showWinningFastBoard(participant, winningTeam, winningChatColor);
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        initializeFastBoard(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        giveAmbientStatusEffects(participant);
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
        if (!participant.getWorld().equals(hubWorld)) {
            return;
        }
        participants.add(participant);
    }
    
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(returnToHubTaskId);
        Bukkit.getScheduler().cancelTask(hubTimerTaskId);
    }
    
    private void updateReturnToHubTimerFastBoard(Player participant, String timeString) {
        fastBoardManager.updateLines(
                participant.getUniqueId(),
                "",
                "Back to Hub:",
                timeString
        );
    }
    
    private void showWinningFastBoard(Player participant, String winningTeam, ChatColor chatColor) {
        fastBoardManager.updateLines(
                participant.getUniqueId(),
                "",
                chatColor+"Winners:",
                chatColor+winningTeam
        );
    }
    
    private void initializeFastBoard(Player participant) {
        fastBoardManager.updateLines(
                participant.getUniqueId()
        );
    }
    
    private void initializeHubDelayCountDown(Player participant, String timeString) {
        fastBoardManager.updateLines(
                participant.getUniqueId(),
                "",
                timeString
        );
    }
    
    private void updateHubTimerDisplay(Player participant, String timeString) {
        fastBoardManager.updateLine(
                participant.getUniqueId(),
                1,
                timeString
        );
    }
    
    private void setupTeamOptions() {
        for (Team team : mctScoreboard.getTeams()) {
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
        if (!player.getWorld().equals(this.hubWorld)) {
            return;
        }
        giveAmbientStatusEffects(player);
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
        if (headingToHub.contains(participant) || participant.getWorld().equals(hubWorld)) {
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
        if (!participant.getWorld().equals(hubWorld)) {
            return;
        }
        Location location = participant.getLocation();
        if (location.getY() < 130) {
            participant.teleport(hubWorld.getSpawnLocation());
            participant.sendMessage("You fell out of the hub boundary");
        }
    }
    
    @EventHandler
    public void teleportListener(PlayerTeleportEvent event) {
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        if (event.getTo().getWorld().equals(hubWorld)) {
            if (!event.getFrom().getWorld().equals(hubWorld)) {
                initializeParticipant(participant);
            }
        } else {
            this.participants.remove(participant);
        }
    }
    
    private void giveAmbientStatusEffects(Player player) {
        player.addPotionEffect(RESISTANCE);
        player.addPotionEffect(REGENERATION);
        player.addPotionEffect(FIRE_RESISTANCE);
        player.addPotionEffect(SATURATION);
    }
    
    private void initializedStatusEffectLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player participant : participants) {
                    giveAmbientStatusEffects(participant);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        this.boundaryEnabled = boundaryEnabled;
    }
}
