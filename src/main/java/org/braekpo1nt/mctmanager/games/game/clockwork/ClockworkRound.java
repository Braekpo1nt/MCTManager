package org.braekpo1nt.mctmanager.games.game.clockwork;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ClockworkRound implements Listener {
    
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkGame clockworkGame;
    private final ClockworkStorageUtil storageUtil;
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Boolean> participantsAreAlive = new HashMap<>();
    private boolean roundActive = false;
    private int breatherDelayTaskId;
    private int clockChimeTaskId;
    private int getToWedgeDelayTaskId;
    private int stayOnWedgeDelayTaskId;
    private final Random random = new Random();
    private int numberOfChimes = 1;
    private long chimeInterval = 20L;
    /**
     * indicates whether players should be killed if they step off of the wedge indicated by numberOfChimes
     */
    private boolean mustStayOnWedge = false;
    
    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, ClockworkStorageUtil storageUtil) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.storageUtil = storageUtil;
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        this.participantsAreAlive = new HashMap<>(newParticipants.size());
        mustStayOnWedge = false;
        roundActive = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setupTeamOptions();
        startBreatherDelay();
        Bukkit.getLogger().info("Starting Clockwork Round");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participantsAreAlive.put(participant.getUniqueId(), true);
        initializeFastBoard(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void roundIsOver() {
        stop();
        clockworkGame.roundIsOver();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        roundActive = false;
        Bukkit.getLogger().info("Stopping Clockwork round");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        hideFastBoard(participant);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(breatherDelayTaskId);
        Bukkit.getScheduler().cancelTask(clockChimeTaskId);
        Bukkit.getScheduler().cancelTask(getToWedgeDelayTaskId);
        Bukkit.getScheduler().cancelTask(stayOnWedgeDelayTaskId);
    }
    
    private void startBreatherDelay() {
        for (Player participant : participants) {
            participant.teleport(storageUtil.getStartingLocation());
        }
        mustStayOnWedge = false;
        this.breatherDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getBreatherDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    startClockChime();
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Clock chimes in: %s",
                        TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startClockChime() {
        this.numberOfChimes = random.nextInt(1, 13);
        this.clockChimeTaskId = new BukkitRunnable() {
            int count = numberOfChimes;
            @Override
            public void run() {
                if (count <= 0) {
                    startGetToWedgeDelay();
                    this.cancel();
                    return;
                }
                playChimeSound();
                count--;
            }
        }.runTaskTimer(plugin, 0L, chimeInterval).getTaskId();
        
    }
    
    private void playChimeSound() {
        for (Player participant : participants) {
            participant.playSound(participant.getLocation(), storageUtil.getClockChimeSound(), storageUtil.getClockChimeVolume(), storageUtil.getClockChimePitch());
        }
        gameManager.playSoundForAdmins(storageUtil.getClockChimeSound(), storageUtil.getClockChimeVolume(), storageUtil.getClockChimePitch());
    }
    
    private void startGetToWedgeDelay() {
        this.getToWedgeDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getGetToWedgeDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    startStayOnWedgeDelay();
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Get to wedge! %s",
                        TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStayOnWedgeDelay() {
        killParticipantsNotOnWedge();
        mustStayOnWedge = true;
        this.stayOnWedgeDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getGetToWedgeDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    mustStayOnWedge = false;
                    incrementChaos();
                    startBreatherDelay();
                    this.cancel();
                    return;
                }
                updateTimerFastBoard(String.format("Stay on wedge: %s",
                        TimeStringUtils.getTimeString(count)));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void killParticipantsNotOnWedge() {
        List<Player> participantsToKill = new ArrayList<>();
        Wedge currentWedge = storageUtil.getWedges().get(numberOfChimes - 1);
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                if (currentWedge.contains(participant.getLocation().toVector())) {
                    participantsToKill.add(participant);
                }
            }
        }
        if (participantsToKill.isEmpty()) {
            return;
        }
        for (Player participant : participantsToKill) {
            participantsAreAlive.put(participant.getUniqueId(), false);
        }
        for (Player participant : participantsToKill) {
            List<ItemStack> drops = Arrays.stream(participant.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .toList();
            Component deathMessage = Component.text(participant.getName())
                    .append(Component.text("'s time has come."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant, drops, 0, deathMessage);
            Bukkit.getServer().getPluginManager().callEvent(fakeDeathEvent);
        }
    }
    
    private void incrementChaos() {
        Bukkit.getLogger().info("increasing chaos (placeholder)");
    }
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                ""
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                ""
        );
    }
    
    private void updateTimerFastBoard(String time) {
        for (Player participant : participants) {
            updateTimerFastBoard(participant, time);
        }
    }
    
    private void updateTimerFastBoard(Player participant, String time) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                time
        );
    }
    
    private void initializeAliveCountFastBoard(Player participant, String countLine) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                countLine
        );
    }
    
    private void updateAliveCountFastBoard(Player participant, String countLine) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                countLine
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
}
