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
import org.braekpo1nt.mctmanager.participant.TeamData;
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
import org.jetbrains.annotations.NotNull;

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
    private Map<String, TeamData<Participant>> teams = new HashMap<>();
    private Map<UUID, Participant> participants = new HashMap<>();
    private Map<UUID, Boolean> participantsAreAlive = new HashMap<>();
    private Map<String, Integer> teamsLivingMembers = new HashMap<>();
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
    
    public void start(Collection<TeamData<Participant>> newTeams, Collection<Participant> newParticipants) {
        this.teams = new HashMap<>(newTeams.size());
        for (Team team : newTeams) {
            teams.put(team.getTeamId(), new TeamData<>(team));
        }
        this.participants = new HashMap<>(newParticipants.size());
        this.participantsAreAlive = new HashMap<>(newParticipants.size());
        this.teamsLivingMembers = new HashMap<>();
        mustStayOnWedge = false;
        clockIsChiming = false;
        roundActive = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        chimeInterval = config.getInitialChimeInterval();
        setupTeamOptions();
        startStatusEffectsTask();
        startBreatherDelay();
        chaosManager.start();
        Main.logger().info("Starting Clockwork Round " + roundNumber);
    }
    
    private void initializeParticipant(Participant participant) {
        teams.get(participant.getTeamId()).addParticipant(participant);
        participants.put(participant.getUniqueId(), participant);
        participantsAreAlive.put(participant.getUniqueId(), true);
        String team = participant.getTeamId();
        if (teamsLivingMembers.containsKey(team)) {
            int livingMembers = teamsLivingMembers.get(team);
            teamsLivingMembers.put(team, livingMembers + 1);
        } else {
            teamsLivingMembers.put(team, 1);
        }
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void rejoinParticipant(Participant participant) {
        participants.put(participant.getUniqueId(), participant);
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * Participants who join mid-game should be considered eliminated, but are free
     * to join the next round and spectate until then.
     * @param participant the participant
     */
    private void joinParticipantMidRound(Participant participant) {
        participants.put(participant.getUniqueId(), participant);
        participantsAreAlive.put(participant.getUniqueId(), false);
        if (!teamsLivingMembers.containsKey(participant.getTeamId())) {
            teamsLivingMembers.put(participant.getTeamId(), 0);
        }
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * @param participant the participant
     * @return true if the participant was in the game before, left, and should be
     * considered as dead/eliminated
     */
    private boolean participantShouldRejoin(Participant participant) {
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
        cancelAllTasks();
        chaosManager.stop();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        teamsLivingMembers.clear();
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
        teams.put(team.getTeamId(), new TeamData<>(team));
    }
    
    public void onParticipantJoin(Participant participant) {
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            joinParticipantMidRound(participant);
        }
    }
    
    public void onParticipantQuit(Participant participant) {
        if (participantsAreAlive.get(participant.getUniqueId())) {
            killParticipants(Collections.singletonList(participant));
        }
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
    }
    
    public void onTeamQuit(Team team) {
        teams.remove(team.getTeamId());
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
        for (Participant participant : participants.values()) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
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
                    List<String> livingTeams = getLivingTeams();
                    if (livingTeams.size() == 1) {
                        String winningTeamId = livingTeams.getFirst();
                        onTeamWinsRound(teams.get(winningTeamId));
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
        List<Participant> participantsToKill = new ArrayList<>();
        Wedge currentWedge = config.getWedges().get(numberOfChimes - 1);
        for (Participant participant : participants.values()) {
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
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (!participantsAreAlive.get(participant.getUniqueId())) {
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
    
    public void killParticipants(Collection<Participant> killedParticipants) {
        Map<String, Integer> teamsKilledMembers = new HashMap<>();
        for (Participant killed : killedParticipants) {
            killed.setGameMode(GameMode.SPECTATOR);
            killed.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(killed);
            ParticipantInitializer.resetHealthAndHunger(killed);
            plugin.getServer().sendMessage(Component.empty()
                    .append(killed.displayName())
                    .append(Component.text(" was claimed by time")));
            participantsAreAlive.put(killed.getUniqueId(), false);
            String killedTeamId = killed.getTeamId();
            
            List<Participant> awardedParticipants = new ArrayList<>();
            for (Participant participant : participants.values()) {
                if (participantsAreAlive.get(participant.getUniqueId()) 
                        && !killedParticipants.contains(participant)
                        && !participant.getTeamId().equals(killedTeamId)) {
                    awardedParticipants.add(participant);
                }
            }
            gameManager.awardPointsToParticipants(awardedParticipants, config.getPlayerEliminationScore());
            
            if (!teamsKilledMembers.containsKey(killedTeamId)) {
                teamsKilledMembers.put(killedTeamId, 1);
            } else {
                teamsKilledMembers.put(killedTeamId, teamsKilledMembers.get(killedTeamId) + 1);
            }
        }
        List<Team> newlyKilledTeams = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : teamsKilledMembers.entrySet()) {
            String teamId = entry.getKey();
            int killedMembers = entry.getValue();
            int livingMembers = teamsLivingMembers.get(teamId);
            int newLivingMembers = livingMembers - killedMembers;
            if (newLivingMembers <= 0) {
                teamsLivingMembers.put(teamId, 0);
                newlyKilledTeams.add(teams.get(teamId));
            } else {
                teamsLivingMembers.put(teamId, newLivingMembers);
            }
        }
        if (newlyKilledTeams.isEmpty()) {
            return;
        }
        for (Team newlyKilledTeam : newlyKilledTeams) {
            for (Participant participant : participants.values()) {
                if (participant.getTeamId().equals(newlyKilledTeam.getTeamId())) {
                    participant.sendMessage(Component.empty()
                            .append(newlyKilledTeam.getFormattedDisplayName())
                            .append(Component.text(" has been eliminated"))
                            .color(NamedTextColor.DARK_RED));
                } else {
                    participant.sendMessage(Component.empty()
                            .append(newlyKilledTeam.getFormattedDisplayName())
                            .append(Component.text(" has been eliminated"))
                            .color(NamedTextColor.GREEN));
                }
            }
            List<Team> livingTeams = new ArrayList<>();
            for (Team team : teams.values()) {
                if (teamsLivingMembers.get(team.getTeamId()) > 0 && !newlyKilledTeams.contains(team)) {
                    livingTeams.add(teams.get(team.getTeamId()));
                }
            }
            // TODO: does livingTeams need to be a list of actual teams?
            gameManager.awardPointsToTeams(Team.getTeamIds(livingTeams), config.getTeamEliminationScore());
        }
        List<String> livingTeams = getLivingTeams();
        if (livingTeams.isEmpty()) {
            onAllTeamsLoseRound();
        }
    }
    
    private @NotNull List<String> getLivingTeams() {
        // TODO: Teams replace this with a team attribute
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
