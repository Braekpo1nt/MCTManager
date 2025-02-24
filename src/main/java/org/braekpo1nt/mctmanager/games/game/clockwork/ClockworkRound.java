package org.braekpo1nt.mctmanager.games.game.clockwork;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamInfo;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.*;

public class ClockworkRound implements Listener {
    
    
    private final Main plugin;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ClockworkGame clockworkGame;
    private ClockworkConfig config;
    private final ChaosManager chaosManager;
    private final int roundNumber;
    private Map<String, ClockworkTeam> teams = new HashMap<>();
    private Map<UUID, ClockworkParticipant> participants = new HashMap<>();
    private boolean roundActive = false;
    private int clockChimeTaskId;
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
    private final TimerManager timerManager;
    
    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, ClockworkConfig config, int roundNumber, Sidebar sidebar, Sidebar adminSidebar) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.config = config;
        this.roundNumber = roundNumber;
        this.chaosManager = new ChaosManager(plugin, config);
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
    }
    
    public void setConfig(ClockworkConfig config) {
        this.config = config;
        chaosManager.setConfig(config);
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    public void start(Collection<ClockworkTeam> newTeams, Collection<ClockworkParticipant> newParticipants) {
        this.teams = new HashMap<>(newTeams.size());
        for (ClockworkTeam team : newTeams) {
            teams.put(team.getTeamId(), new ClockworkTeam(team));
        }
        this.participants = new HashMap<>(newParticipants.size());
        mustStayOnWedge = false;
        clockIsChiming = false;
        roundActive = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (ClockworkParticipant participant : newParticipants) {
            initializeParticipant(participant);
        }
        chimeInterval = config.getInitialChimeInterval();
        setupTeamOptions();
        startStatusEffectsTask();
        startBreatherDelay();
        chaosManager.start();
        Main.logger().info("Starting Clockwork Round " + roundNumber);
    }
    
    private void initializeParticipant(ClockworkParticipant participant) {
        teams.get(participant.getTeamId()).addParticipant(participant);
        participants.put(participant.getUniqueId(), participant);
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * Participants who join mid-game should be considered eliminated, but are free
     * to join the next round and spectate until then.
     * @param newParticipant the participant
     */
    private void joinParticipantMidRound(Participant newParticipant) {
        ClockworkParticipant participant = new ClockworkParticipant(newParticipant, false);
        participants.put(participant.getUniqueId(), participant);
        teams.get(participant.getTeamId()).addParticipant(participant);
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.SPECTATOR);
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
        chaosManager.stop();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        participants.clear();
        roundActive = false;
        Main.logger().info("Stopping Clockwork round " + roundNumber);
    }
    
    private void resetParticipant(Participant participant) {
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    public void onTeamJoin(Team team) {
        teams.put(team.getTeamId(), new ClockworkTeam(team));
    }
    
    public void onParticipantJoin(Participant participant, Team team) {
        if (!teams.containsKey(team.getTeamId())) {
            teams.put(team.getTeamId(), new ClockworkTeam(team));
        }
        joinParticipantMidRound(participant);
    }
    
    public void onParticipantQuit(ClockworkParticipant participant) {
        if (participant.isAlive()) {
            killParticipants(Collections.singletonList(participant));
        }
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
        if (teams.get(participant.getTeamId()).size() == 0) {
            teams.remove(participant.getTeamId());
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(clockChimeTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        timerManager.cancel();
    }
    
    private void startBreatherDelay() {
        mustStayOnWedge = false;
        timerManager.start(Timer.builder()
                .duration(config.getBreatherDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Clock chimes in: "))
                .onCompletion(this::startClockChime)
                .name("startBreatherDelay")
                .build());
    }
    
    private void startClockChime() {
        sidebar.updateLine("timer", "Chiming...");
        adminSidebar.updateLine("timer", "Chiming...");
        this.numberOfChimes = random.nextInt(1, 13);
        clockIsChiming = true;
        turnOffCollisions();
        for (ClockworkParticipant participant : participants.values()) {
            if (participant.isAlive()) {
                participant.teleport(config.getStartingLocation());
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
                    if (config.getGetToWedgeMessage() != null) {
                        Audience.audience(participants.values()).showTitle(UIUtils.defaultTitle(
                                Component.empty(),
                                config.getGetToWedgeMessage()
                        ));
                    }
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
        for (Participant participant : participants.values()) {
            participant.playSound(participant.getLocation(), config.getClockChimeSound(), config.getClockChimeVolume(), config.getClockChimePitch());
        }
        gameManager.playSoundForAdmins(config.getClockChimeSound(), config.getClockChimeVolume(), config.getClockChimePitch());
    }
    
    private void startGetToWedgeDelay() {
        timerManager.start(Timer.builder()
                .duration(config.getGetToWedgeDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Get to wedge! "))
                .onCompletion(this::startStayOnWedgeDelay)
                .name("startGetToWedgeDelay")
                .build());
    }
    
    private void startStayOnWedgeDelay() {
        timerManager.start(Timer.builder()
                .duration(config.getStayOnWedgeDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Stay on wedge: "))
                .onCompletion(() -> {
                    mustStayOnWedge = false;
                    List<ClockworkTeam> livingTeams = teams.values().stream()
                            .filter(ClockworkTeam::isAlive).toList();
                    if (livingTeams.size() == 1) {
                        onTeamWinsRound(livingTeams.getFirst());
                    } else {
                        incrementChaos();
                        startBreatherDelay();
                    }
                })
                .name("startStayOnWedgeDelay")
                .build());
        killParticipantsNotOnWedge();
        mustStayOnWedge = true;
    }
    
    private void killParticipantsNotOnWedge() {
        List<ClockworkParticipant> participantsToKill = new ArrayList<>();
        Wedge currentWedge = config.getWedges().get(numberOfChimes - 1);
        for (ClockworkParticipant participant : participants.values()) {
            if (participant.isAlive()) {
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
        chimeInterval -= config.getChimeIntervalDecrement();
        if (chimeInterval < 0) {
            chimeInterval = 0;
        }
        chaosManager.incrementChaos();
    }
    
    public void onPlayerDamage(Participant participant, EntityDamageEvent event) {
        if (!roundActive) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            event.setDamage(0);
            ParticipantInitializer.resetHealthAndHunger(participant);
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "ClockworkRound.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!roundActive) {
            return;
        }
        ClockworkParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (!participant.isAlive()) {
            return;
        }
        if (clockIsChiming) {
            Location stayLoc = event.getTo();
            Vector position = config.getStartingLocation().toVector();
            if (!stayLoc.toVector().equals(position)) {
                participant.teleport(position.toLocation(stayLoc.getWorld(), stayLoc.getYaw(), stayLoc.getPitch()));
            }
            return;
        }
        if (!mustStayOnWedge) {
            return;
        }
        Wedge currentWedge = config.getWedges().get(numberOfChimes - 1);
        if (!currentWedge.contains(participant.getLocation().toVector())) {
            killParticipants(Collections.singletonList(participant));
        }
    }
    
    public void killParticipants(Collection<ClockworkParticipant> participantsToKill) {
        // teams which were already dead
        List<ClockworkTeam> existingDeadTeams = teams.values().stream()
                .filter(ClockworkTeam::isDead).toList();
        // participants who will be left alive once participantsToKill are killed
        List<Participant> newLivingParticipants = participants.values().stream()
                .filter(ClockworkParticipant::isAlive)
                .filter(p -> !participantsToKill.contains(p))
                .map(p -> (Participant) p) // TODO: Participant make awardPointsToParticipants accept Collection<T extends Participant>
                .toList();
        
        for (ClockworkParticipant toKill : participantsToKill) {
            toKill.setGameMode(GameMode.SPECTATOR);
            toKill.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(toKill);
            ParticipantInitializer.resetHealthAndHunger(toKill);
            toKill.setAlive(false);
            plugin.getServer().sendMessage(Component.empty()
                    .append(toKill.displayName())
                    .append(Component.text(" was claimed by time")));
            String killedTeamId = toKill.getTeamId();
            
            // award living participants start
            List<Participant> awardedParticipants = newLivingParticipants.stream()
                    .filter(p -> !p.getTeamId().equals(killedTeamId))
                    .toList();
            gameManager.awardPointsToParticipants(awardedParticipants, config.getPlayerEliminationScore());
            // award living participants end
        }
        // who are now dead, which weren't at the start of this method
        List<ClockworkTeam> newlyKilledTeams = teams.values().stream()
                .filter(t -> !existingDeadTeams.contains(t))
                .filter(ClockworkTeam::isDead)
                .toList(); 
        if (newlyKilledTeams.isEmpty()) {
            return;
        }
        List<String> livingTeamIds = teams.values().stream()
                .filter(ClockworkTeam::isAlive)
                .filter(t -> !newlyKilledTeams.contains(t))
                .map(TeamInfo::getTeamId)
                .toList();
        for (ClockworkTeam newlyKilledTeam : newlyKilledTeams) {
            newlyKilledTeam.sendMessage(Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"))
                    .color(NamedTextColor.DARK_RED));
            Audience.audience(teams.values().stream().filter(p -> 
                    !p.getTeamId().equals(newlyKilledTeam.getTeamId()))
                    .toList()).sendMessage(Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"))
                    .color(NamedTextColor.GREEN));
            
            gameManager.awardPointsToTeams(livingTeamIds, config.getTeamEliminationScore());
        }
        boolean allTeamsAreDead = teams.values().stream().noneMatch(ClockworkTeam::isAlive);
        if (allTeamsAreDead) {
            onAllTeamsLoseRound();
        }
    }
    
    private void onAllTeamsLoseRound() {
        messageAllParticipants(Component.text("All teams have been eliminated.")
                .color(NamedTextColor.DARK_RED));
        roundIsOver();
    }
    
    private void onTeamWinsRound(Team winner) {
        gameManager.awardPointsToTeam(winner, config.getWinRoundScore());
        for (Participant participant : participants.values()) {
            if (participant.getTeamId().equals(winner.getTeamId())) {
                participant.sendMessage(Component.empty()
                        .append(winner.getFormattedDisplayName())
                        .append(Component.text(" wins this round!"))
                        .color(NamedTextColor.GREEN));
            } else {
                participant.sendMessage(Component.empty()
                        .append(winner.getFormattedDisplayName())
                        .append(Component.text(" wins this round"))
                        .color(NamedTextColor.DARK_RED));
            }
        }
        roundIsOver();
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, config.getCollisionRule());
        }
    }
    
    private void turnOffCollisions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
    }
    
    /**
     * Sets the collision rule to whatever is in the config
     */
    private void turnOnCollisions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, config.getCollisionRule());
        }
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Participant participant : participants.values()) {
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void messageAllParticipants(Component message) {
        for (Participant participant : participants.values()) {
            participant.sendMessage(message);
        }
        gameManager.messageAdmins(message);
    }
    
}
