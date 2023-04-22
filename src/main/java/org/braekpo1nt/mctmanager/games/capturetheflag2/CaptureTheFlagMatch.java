package org.braekpo1nt.mctmanager.games.capturetheflag2;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class CaptureTheFlagMatch {
    
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
        northParticipants = new ArrayList<>();
        southParticipants = new ArrayList<>();
        allParticipants = new ArrayList<>();
        participantsAreAlive = new HashMap<>();
        killCount = new HashMap<>();
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
        Bukkit.getLogger().info("Starting capture the flag match " + matchPairing);
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
    
    public void stop(boolean report) {
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
        if (report) {
            captureTheFlagRound.matchIsOver(this);
        }
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
    
    private void startClassSelectionPeriod() {
        messageAllParticipants(Component.text("Choose your class"));
        northClassPicker.start(plugin, northParticipants);
        southClassPicker.start(plugin, southParticipants);
        
        this.classSelectionCountdownTaskIt = new BukkitRunnable() {
            private int count = 20;
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllParticipants(Component.text("Class selection is over"));
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
            int count = 15;
            @Override
            public void run() {
                if (count <= 0) {
                    stop(true);
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
        String enemyTeam = matchPairing.southTeam();
        if (northParticipants.contains(participant)) {
            enemyTeam = matchPairing.northTeam();
        }
        ChatColor enemyColor = gameManager.getTeamChatColor(enemyTeam);
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                "vs: "+enemyColor+enemyTeam
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
    
}
