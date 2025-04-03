package org.braekpo1nt.mctmanager.games.game.spleef_old;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayManager;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefInterfaceDeleteMe;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SpleefRound implements Listener, SpleefInterfaceDeleteMe {
    private final Main plugin;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final Random random = new Random();
    private SpleefConfig config;
    private Map<UUID, SpleefRoundParticipant> participants = new HashMap<>();
    private Map<UUID, SpleefRoundParticipant.QuitData> quitDatas = new HashMap<>();
    private Map<String, SpleefRoundTeam> teams = new HashMap<>();
    private boolean spleefHasStarted = false;
    private boolean roundActive = false;
    private final SpleefGameOld spleefGame;
    private final DecayManager decayManager;
    private final PowerupManager powerupManager;
    private boolean descriptionShowing = false;
    private boolean firstRound = false;
    private final TimerManager timerManager;
    
    public SpleefRound(Main plugin, GameManager gameManager, SpleefGameOld spleefGame, SpleefConfig config, Sidebar sidebar, Sidebar adminSidebar) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.spleefGame = spleefGame;
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
        this.config = config;
        this.decayManager = new DecayManager(plugin, config, this);
        this.powerupManager = new PowerupManager(plugin, config);
    }
    
    public void setConfig(SpleefConfig config) {
        this.config = config;
        this.decayManager.setConfig(config);
        this.powerupManager.setConfig(config);
    }
    
    public void start(Collection<SpleefParticipant> newParticipants, Collection<SpleefTeam> newTeams) {
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas = new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        for (SpleefTeam newTeam : newTeams) {
            SpleefRoundTeam team = new SpleefRoundTeam(newTeam);
            this.teams.put(team.getTeamId(), team);
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        placeLayers(true);
        for (SpleefParticipant participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        initializeAdminSidebar();
        setupTeamOptions();
        decayManager.setAliveCount(newParticipants.size());
        decayManager.setAlivePercent(1);
        spleefHasStarted = false;
        roundActive = true;
        if (firstRound) {
            startDescriptionPeriod();
        } else {
            startRoundStartingCountDown();
        }
        Main.logger().info("Starting Spleef round");
    }
    
    private void initializeParticipant(SpleefParticipant newParticipant) {
        SpleefRoundParticipant participant = new SpleefRoundParticipant(newParticipant);
        participants.put(participant.getUniqueId(), participant);
        teleportParticipantToRandomStartingPosition(participant);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void reJoinParticipant(SpleefRoundParticipant participant) {
        participants.put(participant.getUniqueId(), participant);
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    private void resetParticipant(SpleefRoundParticipant participant) {
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    /**
     * If this is the first round, then the description period will be used
     * @param firstRound true if this is the first round, false otherwise
     */
    public void setFirstRound(boolean firstRound) {
        this.firstRound = firstRound;
    }
    
    private void roundIsOver() {
        stop();
        spleefGame.roundIsOver();
    }
    
    public void stop() {
        spleefHasStarted = false;
        roundActive = false;
        firstRound = false;
        descriptionShowing = false;
        HandlerList.unregisterAll(this);
        decayManager.stop();
        powerupManager.stop();
        placeLayers(false);
        cancelAllTasks();
        for (SpleefRoundParticipant participant : participants.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        clearAdminSidebar();
        participants.clear();
        teams.clear();
        quitDatas.clear();
        Main.logger().info("Stopping Spleef round");
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    private void onTeamJoin(SpleefTeam team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        teams.put(team.getTeamId(), new SpleefRoundTeam(team));
    }
    
    public void onParticipantJoin(SpleefParticipant newParticipant, SpleefTeam team) {
        if (!roundActive) {
            return;
        }
        onTeamJoin(team);
        SpleefRoundParticipant.QuitData quitData = quitDatas.remove(newParticipant.getUniqueId());
        if (quitData != null) {
            SpleefRoundParticipant participant = new SpleefRoundParticipant(newParticipant, quitData);
            reJoinParticipant(participant);
        } else {
            initializeParticipant(newParticipant);
            if (spleefHasStarted) {
                giveTool(newParticipant);
                newParticipant.setGameMode(GameMode.SURVIVAL);
            }
        }
        powerupManager.addParticipant(newParticipant);
        long aliveCount = getAliveCount();
        Component alive = Component.empty()
                .append(Component.text("Alive: "))
                .append(Component.text(aliveCount));
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        decayManager.setAliveCount(aliveCount);
        decayManager.setAlivePercent(aliveCount / (double) participants.size());
    }
    
    private void onTeamQuit(SpleefRoundTeam team) {
        if (team.size() > 0) {
            return;
        }
        teams.remove(team.getTeamId());
    }
    
    public void onParticipantQuit(SpleefParticipant spleefParticipant, SpleefTeam spleefTeam) {
        if (!roundActive) {
            return;
        }
        SpleefRoundParticipant participant = participants.get(spleefParticipant.getUniqueId());
        if (participant == null) {
            return;
        }
        SpleefRoundTeam team = teams.get(spleefTeam.getTeamId());
        if (descriptionShowing) {
            resetParticipant(participant);
            participants.remove(participant.getUniqueId());
            onTeamQuit(team);
            return;
        }
        if (spleefHasStarted && participant.isAlive()) {
            Component deathMessage = Component.empty()
                    .append(Component.text(participant.getName()))
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant.getPlayer(),
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            onPlayerDeath(fakeDeathEvent);
        }
        quitDatas.put(participant.getUniqueId(), participant.getQuitData());
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
        onTeamQuit(team);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!roundActive) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!participants.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!cause.equals(EntityDamageEvent.DamageCause.LAVA)
                && !cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SpleefRound.onPlayerDamage()->not fire or lava cancelled");
            event.setCancelled(true);
            return;
        }
        if (!spleefHasStarted) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SpleefRound.onPlayerDamage()->!spleefHasStarted cancelled");
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!roundActive) {
            return;
        }
        SpleefRoundParticipant killed = participants.get(event.getPlayer().getUniqueId());
        if (killed == null) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "SpleefRound.onPlayerDeath() cancelled");
        event.setCancelled(true);
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            plugin.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (lessThanTwoPlayersAlive() || exactlyOneTeamIsAlive()) {
            roundIsOver();
        }
    }
    
    /**
     * @return true if exactly one team is alive, false otherwise
     */
    private boolean exactlyOneTeamIsAlive() {
        String onlyTeam = null;
        for (SpleefRoundParticipant participant : participants.values()) {
            if (participant.isAlive()) {
                String livingTeam = participant.getTeamId();
                if (onlyTeam == null) {
                    onlyTeam = livingTeam;
                } else if (!onlyTeam.equals(livingTeam)) {
                    return false;
                }
            }
        }
        return onlyTeam != null;
    }
    
    private boolean lessThanTwoPlayersAlive() {
        return getAliveCount() < 2;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!roundActive) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (config.getPreventInteractions().contains(clickedBlock.getType())) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }
    
    private void onParticipantDeath(SpleefRoundParticipant killed) {
        ParticipantInitializer.clearStatusEffects(killed);
        ParticipantInitializer.resetHealthAndHunger(killed);
        killed.getInventory().clear();
        killed.setAlive(false);
        powerupManager.removeParticipant(killed);
        int aliveCount = participants.size();
        List<SpleefRoundTeam> awardedTeams = new ArrayList<>();
        for (SpleefRoundParticipant participant : participants.values()) {
            if (participant.isAlive()) {
                if (!participant.getTeamId().equals(killed.getTeamId())) {
                    int multiplied = (int) (gameManager.getMultiplier() * config.getSurviveScore());
                    participant.awardPoints(multiplied);
                    SpleefRoundTeam team = teams.get(participant.getTeamId());
                    team.addPoints(multiplied);
                    spleefGame.updateScore(participant);
                    awardedTeams.add(team);
                }
            } else {
                aliveCount--;
            }
        }
        for (SpleefRoundTeam team : awardedTeams) {
            spleefGame.updateScore(team);
        }
        
        Component alive = Component.empty()
                        .append(Component.text("Alive: "))
                        .append(Component.text(aliveCount));
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        decayManager.setAliveCount(aliveCount);
        decayManager.setAlivePercent(aliveCount / (double) participants.size());
    }
    
    private void startSpleef() {
        String alive = String.format("Alive: %s", participants.size());
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        giveTools();
        for (Participant participant : participants.values()) {
            participant.setGameMode(GameMode.SURVIVAL);
        }
        spleefHasStarted = true;
        decayManager.start();
        powerupManager.start(participants.values());
    }
    
    private void giveTools() {
        for (Participant participant : participants.values()) {
            participant.getInventory().addItem(config.getTool());
        }
    }
    
    private void giveTool(Participant participant) {
        participant.getInventory().addItem(config.getTool());
    }
    
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        firstRound = false;
        timerManager.start(Timer.builder()
                .duration(config.getDescriptionDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    descriptionShowing = false;
                    startRoundStartingCountDown();
                })
                .build());
    }
    
    private void startRoundStartingCountDown() {
        timerManager.start(Timer.builder()
                .duration(config.getRoundStartingDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Starting: "))
                .titleAudience(Audience.audience(participants.values()))
                .onCompletion(this::startSpleef)
                .build());
    }
    
    private void placeLayers(boolean replaceStencil) {
        for (int i = 0; i < config.getStructures().size(); i++) {
            Structure layer = config.getStructures().get(i);
            layer.place(config.getStructureOrigins().get(i), true, StructureRotation.NONE, Mirror.NONE, 0, 1, random);
        }
        if (replaceStencil && config.getStencilBlock() != null) {
            for (BoundingBox layerArea : config.getDecayLayers()) {
                BlockPlacementUtils.createCubeReplace(config.getWorld(), layerArea, config.getStencilBlock(), config.getLayerBlock());
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!roundActive) {
            return;
        }
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        powerupManager.onParticipantBreakBlock(participant);
        event.setDropItems(false);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!roundActive) {
            return;
        }
        if (spleefHasStarted) {
            return;
        }
        if (config.getSafetyArea() == null) {
            return;
        }
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (!config.getSafetyArea().contains(event.getFrom().toVector())) {
            participant.teleport(config.getStartingLocations().getFirst());
            return;
        }
        if (!config.getSafetyArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLine("alive", "Alive: ");
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteLine("alive");
    }
    
    private void initializeSidebar() {
        sidebar.addLine("alive", "Alive: ");
    }
    
    private void clearSidebar() {
        sidebar.deleteLine("alive");
    }
    
    private void teleportParticipantToRandomStartingPosition(Participant participant) {
        int index = random.nextInt(config.getStartingLocations().size());
        participant.teleport(config.getStartingLocations().get(index));
        participant.setRespawnLocation(config.getStartingLocations().get(index), true);
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    @Override
    public void messageAllParticipants(@NotNull Component message) {
        gameManager.messageAdmins(message);
        for (Participant participant : participants.values()) {
            participant.sendMessage(message);
        }
    }
    
    @Override
    public void titleAllParticipants(@NotNull Title title) {
        spleefGame.showTitle(title);
    }
    
    /**
     * @return the number of participants who are alive in this round
     */
    private long getAliveCount() {
        return participants.values().stream().filter(SpleefRoundParticipant::isAlive).count();
    }
    
    /**
     * @param shouldGivePowerups true means powerups should be given, false means they should not
     */
    @Override
    public void setShouldGivePowerups(boolean shouldGivePowerups) {
        powerupManager.setShouldGivePowerups(shouldGivePowerups);
    }
}
