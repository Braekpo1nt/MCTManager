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
        participant.teleport(hubWorld.getSpawnLocation());
        initializeParticipant(participant);
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
    
    public void pedestalTeleport(List<Player> winningTeamParticipants, String winningTeam, ChatColor winningChatColor, List<Player> otherParticipants) {
        setupTeamOptions();
        for (Player participant : otherParticipants) {
            showWinningFastBoard(participant, winningTeam, winningChatColor);
            ParticipantInitializer.clearStatusEffects(participant);
            participant.setGameMode(GameMode.ADVENTURE);
            participant.getInventory().clear();
            giveAmbientStatusEffects(participant);
            participant.teleport(this.observePedestalLocation);
        }
        for (Player winningParticipant : winningTeamParticipants) {
            showWinningFastBoard(winningParticipant, winningTeam, winningChatColor);
            ParticipantInitializer.clearStatusEffects(winningParticipant);
            winningParticipant.setGameMode(GameMode.ADVENTURE);
            winningParticipant.getInventory().clear();
            giveAmbientStatusEffects(winningParticipant);
            winningParticipant.teleport(this.pedestalLocation);
        }
    }
    
    public void cancelReturnToHub() {
        Bukkit.getScheduler().cancelTask(returnToHubTaskId);
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
        Location location = participant.getLocation();
        if (location.getY() < 130) {
            participant.teleport(hubWorld.getSpawnLocation());
            participant.sendMessage("You fell out of the hub boundary");
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
