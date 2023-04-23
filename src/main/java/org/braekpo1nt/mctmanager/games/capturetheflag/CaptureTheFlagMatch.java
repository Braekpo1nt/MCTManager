package org.braekpo1nt.mctmanager.games.capturetheflag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class CaptureTheFlagMatch implements Listener {
    
    private final CaptureTheFlagRound captureTheFlagRound;
    private final Main plugin;
    private final GameManager gameManager;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private List<Player> northParticipants;
    private List<Player> southParticipants;
    private List<Player> allParticipants;
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<UUID, Integer> killCount;
    private boolean matchActive = false;
    private int classSelectionCountdownTaskIt;
    private int matchTimerTaskId;
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    private Location northFlagPosition;
    private Location southFlagPosition;
    private Material northBanner;
    private Material southBanner;
    
    public CaptureTheFlagMatch(CaptureTheFlagRound captureTheFlagRound, Main plugin, GameManager gameManager, MatchPairing matchPairing, Arena arena) {
        this.captureTheFlagRound = captureTheFlagRound;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.matchPairing = matchPairing;
        this.arena = arena;
        this.northClassPicker = new ClassPicker();
        this.southClassPicker = new ClassPicker();
    }
    
    public MatchPairing getMatchPairing() {
        return matchPairing;
    }
    
    public void start(List<Player> newNorthParticipants, List<Player> newSouthParticipants) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        northParticipants = new ArrayList<>();
        southParticipants = new ArrayList<>();
        allParticipants = new ArrayList<>();
        participantsAreAlive = new HashMap<>();
        killCount = new HashMap<>();
        String northTeam = gameManager.getTeamName(newNorthParticipants.get(0).getUniqueId());
        String southTeam = gameManager.getTeamName(newSouthParticipants.get(0).getUniqueId());
        placeFlags(northTeam, southTeam);
        closeGlassBarriers();
        for (Player northParticipant : newNorthParticipants) {
            initializeParticipant(northParticipant, true);
        }
        for (Player southParticipant : newSouthParticipants) {
            initializeParticipant(southParticipant, false);
        }
        setupTeamOptions();
        startClassSelectionPeriod();
        matchActive = true;
        Bukkit.getLogger().info(String.format("Starting capture the flag match %s", matchPairing));
    }
    
    private void initializeParticipant(Player participant, boolean north) {
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        killCount.put(participantUniqueId, 0);
        if (north) {
            northParticipants.add(participant);
            participant.teleport(arena.northSpawn());
        } else {
            southParticipants.add(participant);
            participant.teleport(arena.southSpawn());
        }
        allParticipants.add(participant);
        initializeFastBoard(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void matchIsOver() {
        stop();
        captureTheFlagRound.matchIsOver(this);
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        northClassPicker.stop(false);
        southClassPicker.stop(false);
        for (Player participant : allParticipants) {
            resetParticipant(participant);
        }
        allParticipants.clear();
        northParticipants.clear();
        southParticipants.clear();
        matchActive = false;
        Bukkit.getLogger().info("Stopping capture the flag match " + matchPairing);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void startMatch() {
        for (Player participants : allParticipants) {
            participants.closeInventory();
        }
        messageAllParticipants(Component.text("Begin!"));
        openGlassBarriers();
        startMatchTimer();
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(classSelectionCountdownTaskIt);
        Bukkit.getScheduler().cancelTask(matchTimerTaskId);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!matchActive) {
            return;
        }
        Player participant = event.getPlayer();
        Location location = participant.getLocation();
        if (northParticipants.contains(participant)) {
            if (!canPickUpSouthFlag(location)) {
                return;
            }
            pickUpSouthFlag(participant);
            return;
        }
        if (southParticipants.contains(participant)) {
            if (!canPickUpNorthFlag(location)) {
                return;
            }
            pickUpNorthFlag(participant);
        }
    }
    
    private synchronized void pickUpSouthFlag(Player northParticipant) {
        messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was captured!"))
                .color(NamedTextColor.DARK_RED));
        messageNorthParticipants(Component.empty()
                .append(Component.text("You captured the flag!"))
                .color(NamedTextColor.GREEN));
        northParticipant.getEquipment().setHelmet(new ItemStack(southBanner));
        southFlagPosition.getBlock().setType(Material.AIR);
        southFlagPosition = null;
    }
    
    private synchronized void pickUpNorthFlag(Player southParticipant) {
        messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was captured!"))
                .color(NamedTextColor.DARK_RED));
        messageSouthParticipants(Component.empty()
                .append(Component.text("You captured the flag!"))
                .color(NamedTextColor.GREEN));
        southParticipant.getEquipment().setHelmet(new ItemStack(northBanner));
        northFlagPosition.getBlock().setType(Material.AIR);
        northFlagPosition = null;
    }
    
    /**
     * Returns true if the north flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#northFlagPosition}
     * @param location The location to check
     * @return Whether the north flag is dropped and the location is on the north flag
     */
    private boolean canPickUpNorthFlag(Location location) {
        if (northFlagPosition == null) {
            return false;
        }
        return northFlagPosition.getBlockX() == location.getBlockX() && northFlagPosition.getBlockY() == location.getBlockY() && northFlagPosition.getBlockZ() == location.getBlockZ();
    }
    
    /**
     * Returns true if the south flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#southFlagPosition}
     * @param location The location to check
     * @return Whether the south flag is dropped and the location is on the south flag
     */
    private boolean canPickUpSouthFlag(Location location) {
        if (southFlagPosition == null) {
            return false;
        }
        return southFlagPosition.getBlockX() == location.getBlockX() && southFlagPosition.getBlockY() == location.getBlockY() && southFlagPosition.getBlockZ() == location.getBlockZ();
    }
    
    private void startClassSelectionPeriod() {
        messageAllParticipants(Component.text("Choose your class"));
        northClassPicker.start(plugin, northParticipants);
        southClassPicker.start(plugin, southParticipants);
        
        this.classSelectionCountdownTaskIt = new BukkitRunnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    northClassPicker.stop(true);
                    southClassPicker.stop(true);
                    startMatch();
                    this.cancel();
                    return;
                }
                for (Player participant : allParticipants) {
                    String timeString = TimeStringUtils.getTimeString(count);
                    updateClassSelectionFastBoardTimer(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startMatchTimer() {
        this.matchTimerTaskId = new BukkitRunnable() {
            int count = 3*60;
            @Override
            public void run() {
                if (count <= 0) {
                    matchIsOver();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : allParticipants) {
                    updateMatchTimer(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeFastBoard(Player participant) {
        String enemyTeam = matchPairing.northTeam();
        if (northParticipants.contains(participant)) {
            enemyTeam = matchPairing.southTeam();
        }
        ChatColor enemyColor = gameManager.getTeamChatColor(enemyTeam);
        String enemyDisplayName = gameManager.getTeamDisplayName(enemyTeam);
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                "vs: "+enemyColor+enemyDisplayName
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Round:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                "7:00"
        );
    }
    
    private void updateClassSelectionFastBoardTimer(Player participant, String timerString) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Class selection:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                timerString
        );
    }
    
    private void updateMatchTimer(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Round:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                timeLeft
        );
    }
    
    private void placeFlags(String northTeam, String southTeam) {
        northBanner = gameManager.getTeamBannerColor(northTeam);
        northFlagPosition = arena.northFlag().clone();
        northFlagPosition.getBlock().setType(northBanner);
        
        southBanner = gameManager.getTeamBannerColor(southTeam);
        southFlagPosition = arena.southFlag().clone();
        southFlagPosition.getBlock().setType(southBanner);
    }
    
    /**
     * Closes the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void closeGlassBarriers() {
        BlockPlacementUtils.createCube(arena.northBarrier(), 5, 4, 1, Material.GLASS_PANE);
        BlockPlacementUtils.createCube(arena.southBarrier(), 5, 4, 1, Material.GLASS_PANE);
    }
    
    /**
     * Opens the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void openGlassBarriers() {
        BlockPlacementUtils.createCube(arena.northBarrier(), 5, 4, 1, Material.AIR);
        BlockPlacementUtils.createCube(arena.southBarrier(), 5, 4, 1, Material.AIR);
    }
    
    /**
     * Sets up the team options for the teams in this match
     */
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            if (team.getName().matches(matchPairing.northTeam()) || team.getName().matches(matchPairing.southTeam())) {
                team.setAllowFriendlyFire(false);
                team.setCanSeeFriendlyInvisibles(true);
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        }
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : allParticipants) {
            participant.sendMessage(message);
        }
    }
    
    private void messageNorthParticipants(Component message) {
        for (Player participant : northParticipants) {
            participant.sendMessage(message);
        }
    }
    
    private void messageSouthParticipants(Component message) {
        for (Player participant : southParticipants) {
            participant.sendMessage(message);
        }
    }
    
}
