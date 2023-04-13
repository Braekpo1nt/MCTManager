package org.braekpo1nt.mctmanager.hub;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
    private int returnToHubTaskId;
    private final Location observePedestalLocation;
    private final Location pedestalLocation;

    public HubManager(Main plugin, Scoreboard mctScoreboard, FastBoardManager fastBoardManager) {
        this.plugin = plugin;
        this.mctScoreboard = mctScoreboard;
        this.fastBoardManager = fastBoardManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.hubWorld = worldManager.getMVWorld("Hub").getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        observePedestalLocation = anchorManager.getAnchorLocation("pedestal-view");
        pedestalLocation = anchorManager.getAnchorLocation("pedestal");
        initializedStatusEffectLoop();
    }
    
    public void returnParticipantsToHubWithDelay(List<Player> participants) {
        this.returnToHubTaskId = new BukkitRunnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    for (Player participant : participants){
                        participant.sendMessage(Component.text("Returning to hub"));
                    }
                } else {
                    String timeString = TimeStringUtils.getTimeString(count);
                    for (Player participant : participants) {
                        updateReturnToHubTimerFastBoard(participant, timeString);
                    }
                }
                if (count <= 0) {
                    for (Player participant : participants) {
                        returnParticipantToHub(participant);
                    }
                    setupTeamOptions();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    public void pedestalTeleport(List<Player> winningTeamParticipants, String winningTeam, ChatColor winningChatColor, List<Player> otherParticipants) {
        setupTeamOptions();
        for (Player participant : otherParticipants) {
            showWinningFastBoard(participant, winningTeam, winningChatColor);
            clearStatusEffects(participant);
            participant.setGameMode(GameMode.ADVENTURE);
            participant.getInventory().clear();
            giveAmbientStatusEffects(participant);
            participant.teleport(this.observePedestalLocation);
        }
        for (Player winningParticipant : winningTeamParticipants) {
            showWinningFastBoard(winningParticipant, winningTeam, winningChatColor);
            clearStatusEffects(winningParticipant);
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
    
    private void hideFastBoard(Player participant) {
        fastBoardManager.updateLines(
                participant.getUniqueId()
        );
    }
    
    public void returnParticipantToHub(Player participant) {
        hideFastBoard(participant);
        clearStatusEffects(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        teleportPlayerToHub(participant);
        participant.getInventory().clear();
        giveAmbientStatusEffects(participant);
    }
    
    private void setupTeamOptions() {
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
    private void teleportPlayerToHub(Player participant) {
        participant.teleport(hubWorld.getSpawnLocation());
    }
    
    
    
    private void clearStatusEffects(Player participant) {
        for (PotionEffect effect : participant.getActivePotionEffects()) {
            participant.removePotionEffect(effect.getType());
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
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(hubWorld)) {
                        giveAmbientStatusEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }
}
