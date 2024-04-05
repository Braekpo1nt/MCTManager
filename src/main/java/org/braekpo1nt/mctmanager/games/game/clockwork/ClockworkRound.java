package org.braekpo1nt.mctmanager.games.game.clockwork;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClockworkRound implements Listener {
    
    
    private final Main plugin;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
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
    private int statusEffectsTaskId;
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final Random random = new Random();
    private int numberOfChimes = 1;
    private double chimeInterval = 20;
    /**
     * indicates whether players should be killed if they step off of the wedge indicated by numberOfChimes
     */
    private boolean mustStayOnWedge = false;
    /**
     * indicates whether the clock is chiming still and players should be kept in the center
     */
    private boolean clockIsChiming = false;
    
    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, ClockworkStorageUtil storageUtil, int roundNumber, Sidebar sidebar, Sidebar adminSidebar) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.storageUtil = storageUtil;
        this.roundNumber = roundNumber;
        this.chaosManager = new ChaosManager(plugin, storageUtil);
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        this.participantsAreAlive = new HashMap<>(newParticipants.size());
        this.teamsLivingMembers = new HashMap<>();
        mustStayOnWedge = false;
        clockIsChiming = false;
        roundActive = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        chimeInterval = storageUtil.getInitialChimeInterval();
        setupTeamOptions();
        startStatusEffectsTask();
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
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void rejoinParticipant(Player participant) {
        participants.add(participant);
        participant.teleport(storageUtil.getStartingLocation());
        participant.getInventory().clear();
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * Participants who join mid-game should be considered eliminated, but are free
     * to join the next round and spectate until then.
     * @param participant the participant
     */
    private void joinParticipantMidRound(Player participant) {
        participants.add(participant);
        participantsAreAlive.put(participant.getUniqueId(), false);
        String team = gameManager.getTeamName(participant.getUniqueId());
        if (!teamsLivingMembers.containsKey(team)) {
            teamsLivingMembers.put(team, 0);
        }
        participant.teleport(storageUtil.getStartingLocation());
        participant.getInventory().clear();
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * @param participant the participant
     * @return true if the participant was in the game before, left, and should be
     * considered as dead/eliminated
     */
    private boolean participantShouldRejoin(Player participant) {
        if (!roundActive) {
            return false;
        }
        return participantsAreAlive.containsKey(participant.getUniqueId());
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
    }
    
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            joinParticipantMidRound(participant);
        }
    }
    
    public void onParticipantQuit(Player participant) {
        if (participantsAreAlive.get(participant.getUniqueId())) {
            killParticipants(Collections.singletonList(participant));
        }
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void cancelAllTasks() {
        Bukkit.getLogger().info("Cancelling tasks ");
        Bukkit.getScheduler().cancelTask(breatherDelayTaskId);
        Bukkit.getScheduler().cancelTask(clockChimeTaskId);
        Bukkit.getScheduler().cancelTask(getToWedgeDelayTaskId);
        Bukkit.getScheduler().cancelTask(stayOnWedgeDelayTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
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
                String timer = String.format("Clock chimes in: %s",
                        TimeStringUtils.getTimeString(count));
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startClockChime() {
        sidebar.updateLine("timer", "Chiming...");
        adminSidebar.updateLine("timer", "Chiming...");
        this.numberOfChimes = random.nextInt(1, 13);
        clockIsChiming = true;
        turnOffCollisions();
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                participant.teleport(storageUtil.getStartingLocation());
                participant.setArrowsInBody(0);
            }
        }
        this.clockChimeTaskId = new BukkitRunnable() {
            int count = numberOfChimes;
            @Override
            public void run() {
                if (count <= 0) {
                    clockIsChiming = false;
                    turnOnCollisions();
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
                String timer = String.format("Get to wedge! %s",
                        TimeStringUtils.getTimeString(count));
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
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
                    List<String> livingTeams = getLivingTeams();
                    Bukkit.getLogger().info(String.format("livingTeams.size() = %s", livingTeams.size()));
                    if (livingTeams.size() == 1) {
                        String winningTeam = livingTeams.get(0);
                        onTeamWinsRound(winningTeam);
                        this.cancel();
                        return;
                    }
                    incrementChaos();
                    startBreatherDelay();
                    this.cancel();
                    return;
                }
                String timer = String.format("Stay on wedge: %s",
                        TimeStringUtils.getTimeString(count));
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        killParticipantsNotOnWedge();
        mustStayOnWedge = true;
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
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!roundActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        if (!participantsAreAlive.get(participant.getUniqueId())) {
            return;
        }
        if (clockIsChiming) {
            Location stayLoc = event.getTo();
            Vector position = storageUtil.getStartingLocation().toVector();
            if (!stayLoc.toVector().equals(position)) {
                participant.teleport(position.toLocation(stayLoc.getWorld(), stayLoc.getYaw(), stayLoc.getPitch()));
            }
            return;
        }
        if (!mustStayOnWedge) {
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
            } else {
                teamsLivingMembers.put(team, newLivingMembers);
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
        Bukkit.getLogger().info(String.format("teamsLivingMembers = %s", teamsLivingMembers.toString()));
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
    
    private void turnOffCollisions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    private void turnOnCollisions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player participant : participants) {
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
        gameManager.messageAdmins(message);
    }
    
}
