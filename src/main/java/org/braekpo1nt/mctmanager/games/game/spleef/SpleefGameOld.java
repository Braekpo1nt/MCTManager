package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SpleefGameOld implements Listener, MCTGame {
    private final Main plugin;
    private final GameManager gameManager;
    private final Random random = new Random();
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final SpleefConfig config;
    private final Map<UUID, SpleefParticipant> participants;
    private final Map<UUID, SpleefParticipant.QuitData> quitDatas;
    private final Map<String, SpleefTeam> teams;
    private final Map<String, SpleefTeam.QuitData> teamQuitDatas;
    private final List<Player> admins;
    private final List<SpleefRound> rounds;
    private int currentRoundIndex = 0;
    private final TimerManager timerManager;
    
    private @NotNull Component title;
    
    public SpleefGameOld(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull SpleefConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.title = title;
        this.config = config;
        this.admins = new ArrayList<>(newAdmins.size());
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas =  new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        this.teamQuitDatas = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Team newTeam : newTeams) {
            SpleefTeam team = new SpleefTeam(newTeam, 0);
            this.teams.put(team.getTeamId(), team);
        }
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        rounds = new ArrayList<>(config.getRounds());
        for (int i = 0; i < config.getRounds(); i++) {
            rounds.add(new SpleefRound(plugin, gameManager, this, config, sidebar, adminSidebar));
        }
        rounds.getFirst().setFirstRound(true);
        plugin.getLogger().info(String.format("rounds.size() = %d", rounds.size()));
        currentRoundIndex = 0;
        setupTeamOptions();
        startAdmins(newAdmins);
        displayDescription();
        startNextRound();
        Main.logger().info("Started Spleef");
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sidebar.updateLine("title", title);
        adminSidebar.updateLine("title", title);
    }
    
    @Override
    public GameType getType() {
        return GameType.SPLEEF;
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeParticipant(Participant newParticipant) {
        SpleefParticipant participant = new SpleefParticipant(newParticipant, 0);
        participants.put(participant.getUniqueId(), participant);
        teams.get(participant.getTeamId()).addParticipant(participant);
        sidebar.addPlayer(participant);
        teleportParticipantToRandomStartingPosition(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    private void reJoinParticipant(SpleefParticipant participant) {
        participants.put(participant.getUniqueId(), participant);
        teams.get(participant.getTeamId()).addParticipant(participant);
        sidebar.addPlayer(participant);
        teleportParticipantToRandomStartingPosition(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    private void teleportParticipantToRandomStartingPosition(Participant participant) {
        int index = random.nextInt(config.getStartingLocations().size());
        participant.teleport(config.getStartingLocations().get(index));
        participant.setRespawnLocation(config.getStartingLocations().get(index), true);
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
                new KeyLine("round", String.format("Round %d/%d", currentRoundIndex+1, config.getRounds()))
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
        admin.teleport(config.getStartingLocations().getFirst());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        if (currentRoundIndex < rounds.size()) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.stop();
            }
        }
        rounds.clear();
        saveScores();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        participants.clear();
        teams.clear();
        quitDatas.clear();
        teamQuitDatas.clear();
        gameManager.gameIsOver();
        Main.logger().info("Stopping Spleef");
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (SpleefTeam team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (SpleefParticipant participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (Map.Entry<String, SpleefTeam.QuitData> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, SpleefParticipant.QuitData> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    private void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant);
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        sidebar.removePlayer(participant.getUniqueId());
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
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (config.getSpectatorBoundary() == null){
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!config.getSpectatorBoundary().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getStartingLocations().getFirst());
            return;
        }
        if (!config.getSpectatorBoundary().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (config.getSpectatorBoundary() == null){
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
        if (!config.getSpectatorBoundary().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onTeamJoin(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        SpleefTeam.QuitData quitData = teamQuitDatas.get(team.getTeamId());
        if (quitData != null) {
            teams.put(team.getTeamId(), new SpleefTeam(team, quitData.getScore()));
        } else {
            teams.put(team.getTeamId(), new SpleefTeam(team, 0));
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        onTeamJoin(team);
        SpleefParticipant.QuitData quitData = quitDatas.remove(participant.getUniqueId());
        if (quitData != null) {
            reJoinParticipant(new SpleefParticipant(participant, quitData));
        } else {
            initializeParticipant(participant);
        }
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round %d/%d", currentRoundIndex+1, config.getRounds()))
        );
        displayScore(participants.get(participant.getUniqueId()));
        displayScore(teams.get(team.getTeamId()));
        if (currentRoundIndex < rounds.size()) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantJoin(
                        participants.get(participant.getUniqueId()), 
                        teams.get(team.getTeamId()));
            }
        }
    }
    
    private void onTeamQuit(SpleefTeam team) {
        if (team.size() > 0) {
            return;
        }
        SpleefTeam removed = teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), removed.getQuitData());
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        SpleefParticipant participant = participants.get(participantUUID);
        if (participant == null) {
            return;
        }
        quitDatas.put(participant.getUniqueId(), participant.getQuitData());
        SpleefTeam spleefTeam = teams.get(teamId);
        if (currentRoundIndex < rounds.size()) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantQuit(participant, spleefTeam);
            }
        }
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
        onTeamQuit(spleefTeam);
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
        SpleefRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants.values(), teams.values());
        String round = String.format("Round %d/%d", currentRoundIndex + 1, rounds.size());
        sidebar.updateLine("round", round);
        adminSidebar.updateLine("round", round);
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round %d/%d", 1, config.getRounds())),
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
                new KeyLine("round", String.format("Round %d/%d", 1, config.getRounds())),
                new KeyLine("timer", "")
        );
        
        for (SpleefTeam team : teams.values()) {
            displayScore(team);
        }
        for (SpleefParticipant participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    // Only if you have subteams, like rounds
    public void updateScore(SpleefRoundTeam team) {
        SpleefTeam myTeam = teams.get(team.getTeamId());
        myTeam.setScore(team.getScore());
        displayScore(myTeam);
    }
    
    // make this private if above used
    private void displayScore(SpleefTeam team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
    }
    
    // Only if you have sub-participants, like rounds
    public void updateScore(SpleefRoundParticipant participant) {
        SpleefParticipant myParticipant = participants.get(participant.getUniqueId());
        myParticipant.setScore(participant.getScore());
        displayScore(myParticipant);
    }
    
    // make this private if above used
    private void displayScore(SpleefParticipant participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
    }
    
    private void messageAllParticipants(Component message) {
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).sendMessage(message);
    }
    
    public void showTitle(@NotNull Title title) {
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).showTitle(title);
    }
}
