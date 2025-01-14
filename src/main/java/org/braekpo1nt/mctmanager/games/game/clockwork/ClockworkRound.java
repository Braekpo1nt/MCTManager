package org.braekpo1nt.mctmanager.games.game.clockwork;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
    private ClockworkConfig config;
    private final ChaosManager chaosManager;
    private final int roundNumber;
    private List<Player> participants = new ArrayList<>();
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
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
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
        participants.add(participant);
        participantsAreAlive.put(participant.getUniqueId(), true);
        String team = gameManager.getTeamId(participant.getUniqueId());
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
    
    private void rejoinParticipant(Player participant) {
        participants.add(participant);
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
    private void joinParticipantMidRound(Player participant) {
        participants.add(participant);
        participantsAreAlive.put(participant.getUniqueId(), false);
        String team = gameManager.getTeamId(participant.getUniqueId());
        if (!teamsLivingMembers.containsKey(team)) {
            teamsLivingMembers.put(team, 0);
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
        cancelAllTasks();
        chaosManager.stop();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        teamsLivingMembers.clear();
        roundActive = false;
        Main.logger().info("Stopping Clockwork round " + roundNumber);
    }
    
    private void resetParticipant(Player participant) {
        ParticipantInitializer.clearInventory(participant);
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
        for (Player participant : participants) {
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
                        Audience.audience(participants).showTitle(UIUtils.defaultTitle(
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
        for (Player participant : participants) {
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
                        String winningTeam = livingTeams.get(0);
                        onTeamWinsRound(winningTeam);
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
        List<Player> participantsToKill = new ArrayList<>();
        Wedge currentWedge = config.getWedges().get(numberOfChimes - 1);
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
        chimeInterval -= config.getChimeIntervalDecrement();
        if (chimeInterval < 0) {
            chimeInterval = 0;
        }
        chaosManager.incrementChaos();
    }
    
    public void onPlayerDamage(Player participant, EntityDamageEvent event) {
        if (!roundActive) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
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
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "ClockworkRound.onPlayerDamage() cancelled");
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
    
    public void killParticipants(List<Player> killedParticipants) {
        Map<String, Integer> teamsKilledMembers = new HashMap<>();
        for (Player killed : killedParticipants) {
            killed.setGameMode(GameMode.SPECTATOR);
            killed.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(killed);
            ParticipantInitializer.resetHealthAndHunger(killed);
            plugin.getServer().sendMessage(Component.empty()
                    .append(killed.displayName())
                    .append(Component.text(" was claimed by time")));
            participantsAreAlive.put(killed.getUniqueId(), false);
            String killedTeamId = gameManager.getTeamId(killed.getUniqueId());
            
            List<Player> awardedParticipants = new ArrayList<>();
            for (Player participant : participants) {
                String teamId = gameManager.getTeamId(participant.getUniqueId());
                if (participantsAreAlive.get(participant.getUniqueId()) 
                        && !killedParticipants.contains(participant)
                        && !teamId.equals(killedTeamId)) {
                    awardedParticipants.add(participant);
                }
            }
            gameManager.awardPointsToPlayers(awardedParticipants, config.getPlayerEliminationScore());
            
            if (!teamsKilledMembers.containsKey(killedTeamId)) {
                teamsKilledMembers.put(killedTeamId, 1);
            } else {
                teamsKilledMembers.put(killedTeamId, teamsKilledMembers.get(killedTeamId) + 1);
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
        List<String> allTeamIds = gameManager.getTeamIds(participants);
        for (String newlyKilledTeam : newlyKilledTeams) {
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(newlyKilledTeam);
            for (Player participant : participants) {
                String team = gameManager.getTeamId(participant.getUniqueId());
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
            List<String> livingTeamIds = new ArrayList<>();
            for (String teamId : allTeamIds) {
                if (teamsLivingMembers.get(teamId) > 0 && !newlyKilledTeams.contains(teamId)) {
                    livingTeamIds.add(teamId);
                }
            }
            gameManager.awardPointsToTeams(livingTeamIds, config.getTeamEliminationScore());
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
        gameManager.awardPointsToTeam(winningTeam, config.getWinRoundScore());
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        for (Player participant : participants) {
            String team = gameManager.getTeamId(participant.getUniqueId());
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
            team.setOption(Team.Option.COLLISION_RULE, config.getCollisionRule());
        }
    }
    
    private void turnOffCollisions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    /**
     * Sets the collision rule to whatever is in the config
     */
    private void turnOnCollisions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setOption(Team.Option.COLLISION_RULE, config.getCollisionRule());
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
