package org.braekpo1nt.mctmanager.games.game.clockwork;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClockworkRound implements Listener {
    
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkGame clockworkGame;
    private final ClockworkStorageUtil storageUtil;
    private final ChaosManager chaosManager;
    private final int roundNumber;
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Boolean> participantsAreAlive = new HashMap<>();
    private Map<String, Integer> teamsLivingMembers = new HashMap<>();
    private boolean roundActive = false;
    private int breatherDelayTaskId;
    private int clockChimeTaskId;
    private int getToWedgeDelayTaskId;
    private int stayOnWedgeDelayTaskId;
    private final Random random = new Random();
    private int numberOfChimes = 1;
    private double chimeInterval = 20;
    /**
     * indicates whether players should be killed if they step off of the wedge indicated by numberOfChimes
     */
    private boolean mustStayOnWedge = false;
    
    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, ClockworkStorageUtil storageUtil, int roundNumber) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.storageUtil = storageUtil;
        this.roundNumber = roundNumber;
        this.chaosManager = new ChaosManager(plugin, storageUtil);
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        this.participantsAreAlive = new HashMap<>(newParticipants.size());
        this.teamsLivingMembers = new HashMap<>();
        mustStayOnWedge = false;
        roundActive = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        chimeInterval = storageUtil.getInitialChimeInterval();
        setupTeamOptions();
        startBreatherDelay();
        chaosManager.start();
        Bukkit.getLogger().info("Starting Clockwork Round " + roundNumber);
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participantsAreAlive.put(participant.getUniqueId(), true);
        String team = gameManager.getTeamName(participant.getUniqueId());
        if (teamsLivingMembers.containsKey(team)) {
            int livingMembers = teamsLivingMembers.get(team);
            teamsLivingMembers.put(team, livingMembers + 1);
        } else {
            teamsLivingMembers.put(team, 1);
        }
        participant.teleport(storageUtil.getStartingLocation());
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
        chaosManager.stop();
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        teamsLivingMembers.clear();
        roundActive = false;
        Bukkit.getLogger().info("Stopping Clockwork round " + roundNumber);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        hideFastBoard(participant);
    }
    
    private void cancelAllTasks() {
        Bukkit.getLogger().info("Cancelling tasks ");
        Bukkit.getScheduler().cancelTask(breatherDelayTaskId);
        Bukkit.getScheduler().cancelTask(clockChimeTaskId);
        Bukkit.getScheduler().cancelTask(getToWedgeDelayTaskId);
        Bukkit.getScheduler().cancelTask(stayOnWedgeDelayTaskId);
    }
    
    private void startBreatherDelay() {
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
        updateTimerFastBoard("");
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
        }.runTaskTimer(plugin, 0L, (long) chimeInterval).getTaskId();
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
        this.stayOnWedgeDelayTaskId = new BukkitRunnable() {
            int count = storageUtil.getStayOnWedgeDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    mustStayOnWedge = false;
//                    List<String> livingTeams = getLivingTeams();
//                    if (livingTeams.size() == 1) {
//                        String winningTeam = livingTeams.get(0);
//                        onTeamWinsRound(winningTeam);
//                        this.cancel();
//                        return;
//                    }
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
//        killParticipantsNotOnWedge();
//        mustStayOnWedge = true;
    }
    
    private void killParticipantsNotOnWedge() {
        List<Player> participantsToKill = new ArrayList<>();
        Wedge currentWedge = storageUtil.getWedges().get(numberOfChimes - 1);
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                if (!currentWedge.contains(participant.getLocation().toVector())) {
                    participantsToKill.add(participant);
                }
            }
        }
        if (participantsToKill.isEmpty()) {
            return;
        }
        killParticipants(participantsToKill);
    }
    
    private void incrementChaos() {
        chimeInterval -= storageUtil.getChimeIntervalDecrement();
        if (chimeInterval < 0) {
            chimeInterval = 0;
        }
        chaosManager.incrementChaos();
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!roundActive) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            event.setDamage(0);
            ParticipantInitializer.resetHealthAndHunger(participant);
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!roundActive) {
            return;
        }
        if (!mustStayOnWedge) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        if (!participantsAreAlive.get(participant.getUniqueId())) {
            return;
        }
        Wedge currentWedge = storageUtil.getWedges().get(numberOfChimes - 1);
        if (!currentWedge.contains(participant.getLocation().toVector())) {
            killParticipants(Collections.singletonList(participant));
        }
    }
    
    public void killParticipants(List<Player> killedParticipants) {
        Map<String, Integer> teamsKilledMembers = new HashMap<>();
        for (Player killed : killedParticipants) {
            killed.setGameMode(GameMode.SPECTATOR);
            killed.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(killed);
            ParticipantInitializer.resetHealthAndHunger(killed);
            Bukkit.getServer().sendMessage(Component.text(killed.getName())
                    .append(Component.text(" was claimed by time")));
            participantsAreAlive.put(killed.getUniqueId(), false);
            for (Player participant : participants) {
                if (participantsAreAlive.get(participant.getUniqueId()) && !killedParticipants.contains(participant)) {
                    gameManager.awardPointsToParticipant(participant, storageUtil.getPlayerEliminationScore());
                }
            }
            String team = gameManager.getTeamName(killed.getUniqueId());
            if (!teamsKilledMembers.containsKey(team)) {
                teamsKilledMembers.put(team, 1);
            } else {
                teamsKilledMembers.put(team, teamsKilledMembers.get(team) + 1);
            }
        }
        List<String> newlyKilledTeams = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : teamsKilledMembers.entrySet()) {
            String team = entry.getKey();
            int killedMembers = entry.getValue();
            int livingMembers = teamsLivingMembers.get(team);
            int newLivingMembers = livingMembers - killedMembers;
            if (newLivingMembers <= 0) {
                teamsLivingMembers.put(team, 0);
                newlyKilledTeams.add(team);
            }
        }
        if (newlyKilledTeams.isEmpty()) {
            return;
        }
        List<String> allTeams = gameManager.getTeamNames(participants);
        for (String newlyKilledTeam : newlyKilledTeams) {
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(newlyKilledTeam);
            for (Player participant : participants) {
                String team = gameManager.getTeamName(participant.getUniqueId());
                if (team.equals(newlyKilledTeam)) {
                    participant.sendMessage(Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" has been eliminated"))
                            .color(NamedTextColor.DARK_RED));
                } else {
                    participant.sendMessage(Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" has been eliminated"))
                            .color(NamedTextColor.GREEN));
                }
            }
            for (String team : allTeams) {
                if (teamsLivingMembers.get(team) > 0 && !newlyKilledTeams.contains(team)) {
                    gameManager.awardPointsToTeam(team, storageUtil.getTeamEliminationScore());
                }
            }
        }
        List<String> livingTeams = getLivingTeams();
        if (livingTeams.isEmpty()) {
            onAllTeamsLoseRound();
        }
    }
    
    private @NotNull List<String> getLivingTeams() {
        List<String> livingTeams = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : teamsLivingMembers.entrySet()) {
            String team = entry.getKey();
            int livingMembers = entry.getValue();
            if (livingMembers > 0) {
                livingTeams.add(team);
            }
        }
        return livingTeams;
    }
    
    private void onAllTeamsLoseRound() {
        messageAllParticipants(Component.text("All teams have been eliminated.")
                .color(NamedTextColor.DARK_RED));
        roundIsOver();
    }
    
    private void onTeamWinsRound(String winningTeam) {
        gameManager.awardPointsToTeam(winningTeam, storageUtil.getWinRoundScore());
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (team.equals(winningTeam)) {
                participant.sendMessage(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" wins this round!"))
                        .color(NamedTextColor.GREEN));
            } else {
                participant.sendMessage(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" wins this round"))
                        .color(NamedTextColor.DARK_RED));
            }
        }
        roundIsOver();
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
    
    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
        gameManager.messageAdmins(message);
    }
    
}
