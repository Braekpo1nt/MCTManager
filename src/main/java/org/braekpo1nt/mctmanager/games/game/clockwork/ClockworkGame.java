package org.braekpo1nt.mctmanager.games.game.clockwork;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClockworkGame implements Listener, MCTGame {
    private final Main plugin;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ClockworkConfig config;
    private final Map<UUID, ClockworkParticipant> participants;
    private final Map<UUID, ClockworkParticipant.QuitData> quitDatas;
    private final Map<String, ClockworkTeam> teams;
    private final Map<String, ClockworkTeam.QuitData> teamQuitDatas;
    private final List<Player> admins = new ArrayList<>();
    private final List<ClockworkRound> rounds;
    private int currentRoundIndex = 0;
    private boolean descriptionShowing = false;
    private boolean gameActive = false;
    private final TimerManager timerManager;
    
    private @NotNull Component title;
    
    public ClockworkGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ClockworkConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.title = title;
        this.config = config;
        this.teams = new HashMap<>(newTeams.size());
        for (Team team : newTeams) {
            teams.put(team.getTeamId(), new ClockworkTeam(team, 0));
        }
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Participant participant : newParticipants) {
            initializeParticipant(participant, 0);
        }
        initializeSidebar();
        this.rounds = new ArrayList<>(config.getRounds());
        for (int i = 0; i < config.getRounds(); i++) {
            rounds.add(new ClockworkRound(plugin, gameManager, this, config, i+1, sidebar, adminSidebar));
        }
        currentRoundIndex = 0;
        setupTeamOptions();
        startAdmins(newAdmins);
        displayDescription();
        gameActive = true;
        startDescriptionPeriod();
        Main.logger().info("Started clockwork");
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sidebar.updateLine("title", title);
        adminSidebar.updateLine("title", title);
    }
    
    @Override
    public GameType getType() {
        return GameType.CLOCKWORK;
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeParticipant(Participant newParticipant, int score) {
        ClockworkParticipant participant = new ClockworkParticipant(newParticipant, score);
        teams.get(participant.getTeamId()).addParticipant(participant);
        participants.put(participant.getUniqueId(), participant);
        participant.setGameMode(GameMode.ADVENTURE);
        sidebar.addPlayer(participant);
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLines(admin.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round %d/%d", currentRoundIndex+1, rounds.size()))
        );
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    public void stop() {
        if (currentRoundIndex < rounds.size()) {
            ClockworkRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.stop();
            }
        }
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        rounds.clear();
        descriptionShowing = false;
        gameActive = false;
        saveScores();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        participants.clear();
        quitDatas.clear();
        teamQuitDatas.clear();
        gameManager.gameIsOver();
        Main.logger().info("Stopping Clockwork");
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (ClockworkTeam team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (ClockworkParticipant participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (Map.Entry<String, ClockworkTeam.QuitData> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, ClockworkParticipant.QuitData> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    private void resetParticipant(Participant participant) {
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        sidebar.removePlayer(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        timerManager.start(Timer.builder()
                .duration(config.getDescriptionDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .titleAudience(Audience.audience(participants.values()))
                .onCompletion(() -> {
                    descriptionShowing = false;
                    startNextRound();
                })
                .name("startDescriptionPeriod")
                .build());
    }
    
    public void roundIsOver() {
        if (currentRoundIndex+1 >= rounds.size()) {
            stop();
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void startNextRound() {
        ClockworkRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(teams.values(), participants.values());
        updateRoundFastBoard();
    }
    
    private void onTeamJoin(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        ClockworkTeam.QuitData quitData = teamQuitDatas.get(team.getTeamId());
        if (quitData != null) {
            teams.put(team.getTeamId(), new ClockworkTeam(team, quitData.getScore()));
        } else {
            teams.put(team.getTeamId(), new ClockworkTeam(team, 0));
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        if (!gameActive) {
            return;
        }
        onTeamJoin(team);
        ClockworkParticipant.QuitData quitData = quitDatas.remove(participant.getUniqueId());
        if (quitData != null) {
            initializeParticipant(participant, quitData.getScore());
        } else {
            initializeParticipant(participant, 0);
        }
        sidebar.updateLines(participant.getUniqueId(), 
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round %d/%d", currentRoundIndex+1, rounds.size()))
        );
        ClockworkParticipant clockworkParticipant = participants.get(participant.getUniqueId());
        displayScore(clockworkParticipant);
        ClockworkTeam clockworkTeam = teams.get(team.getTeamId());
        displayScore(clockworkTeam);
        if (descriptionShowing) {
            return;
        }
        if (currentRoundIndex < rounds.size()) {
            ClockworkRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantJoin(clockworkParticipant, clockworkTeam);
            }
        }
    }
    
    private void onTeamQuit(ClockworkTeam team) {
        if (team.size() > 0) {
            return;
        }
        ClockworkTeam removed = teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), removed.getQuitData());
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        if (!gameActive) {
            return;
        }
        ClockworkParticipant participant = participants.get(participantUUID);
        if (participant == null) {
            return;
        }
        quitDatas.put(participant.getUniqueId(), participant.getQuitData());
        if (descriptionShowing) {
            resetParticipant(participant);
            participants.remove(participant.getUniqueId());
            return;
        }
        if (currentRoundIndex < rounds.size()) {
            ClockworkRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantQuit(participant);
            }
        }
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
        onTeamQuit(teams.get(teamId));
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getStartingLocation());
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameActive) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!gameActive) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        Participant participant = participants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        if (descriptionShowing) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "ClockworkGame.onPlayerDamage()->descriptionShowing cancelled");
            event.setCancelled(true);
            return;
        }
        ClockworkRound round = rounds.get(currentRoundIndex);
        round.onPlayerDamage(participant, event);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        Participant participant = participants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    /**
     * Stop players from removing their equipment
     */
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (!participants.containsKey(event.getWhoClicked().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!gameActive) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("round", ""),
                new KeyLine("playerCount", ""),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("round", ""),
                new KeyLine("playerCount", ""),
                new KeyLine("timer", "")
        );
        for (ClockworkTeam team : teams.values()) {
            displayScore(team);
        }
        for (ClockworkParticipant participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    public void updateScore(ClockworkRoundTeam team) {
        ClockworkTeam clockworkTeam = teams.get(team.getTeamId());
        clockworkTeam.setScore(team.getScore());
        displayScore(clockworkTeam);
    }
    
    private void displayScore(ClockworkTeam team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
    }
    
    public void updateScore(ClockworkRoundParticipant participant) {
        ClockworkParticipant clockworkParticipant = participants.get(participant.getUniqueId());
        clockworkParticipant.setScore(participant.getScore());
        displayScore(clockworkParticipant);
    }
    
    private void displayScore(ClockworkParticipant participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
    }
    
    private void updateRoundFastBoard() {
        String round = String.format("Round %d/%d", currentRoundIndex + 1, rounds.size());
        sidebar.updateLine("round", round);
        adminSidebar.updateLine("round", round);
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }
    }
    
    private void messageAllParticipants(Component message) {
        Audience.audience(
                Audience.audience(participants.values()),
                Audience.audience(admins)
        ).sendMessage(message);
    }
}
