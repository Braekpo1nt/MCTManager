package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.CaptureTheFlagState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
public class CaptureTheFlagGame implements MCTGame, Listener {
    
    public @NotNull CaptureTheFlagState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final BattleTopbar topbar;
    private final TimerManager timerManager;
    private final RoundManager roundManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final CaptureTheFlagConfig config;
    private final Map<UUID, CTFParticipant> participants;
    private final Map<UUID, CTFParticipant.QuitData> quitDatas;
    private final Map<String, CTFTeam> teams;
    private final Map<String, CTFTeam.QuitData> teamQuitDatas;
    private final List<Player> admins;
    
    private @NotNull Component title;
    
    private final Map<String, CTFTeam> quitTeams = new HashMap<>();
    
    public CaptureTheFlagGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager, 
            @NotNull Component title, 
            @NotNull CaptureTheFlagConfig config, 
            @NotNull Collection<Team> newTeams, 
            @NotNull Collection<Participant> newParticipants, 
            @NotNull List<Player> newAdmins) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.topbar = new BattleTopbar();
        this.sidebar = gameManager.createSidebar();
        this.adminSidebar = gameManager.createSidebar();
        this.title = title;
        this.config = config;
        this.admins = new ArrayList<>(newAdmins.size());
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas =  new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        this.teamQuitDatas = new HashMap<>();
        // start()
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Team newTeam : newTeams) {
            CTFTeam team = new CTFTeam(newTeam, 0);
            teams.put(team.getTeamId(), team);
        }
        Set<String> teamIds = Participant.getTeamIds(newParticipants);
        roundManager = new RoundManager(teamIds, config.getArenas().size());
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        updateRoundLine();
        this.state = new DescriptionState(this);
        Main.logger().info("Starting Capture the Flag");
    }
    
    @Override
    public GameType getType() {
        return GameType.CAPTURE_THE_FLAG;
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sidebar.updateLine("title", title);
        adminSidebar.updateLine("title", title);
    }
    
    public void initializeParticipant(Participant newParticipant) {
        initializeParticipant(newParticipant, 0, 0, 0);
    }
    
    public void initializeParticipant(Participant newParticipant, int kills, int deaths, int score) {
        CTFParticipant participant = new CTFParticipant(newParticipant, kills, deaths, score);
        participants.put(participant.getUniqueId(), participant);
        CTFTeam team = teams.get(participant.getTeamId());
        team.addParticipant(participant);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(config.getSpawnObservatory());
        participant.setRespawnLocation(config.getSpawnObservatory());
        ParticipantInitializer.clearInventory(participant);
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
    }
    
    public void resetParticipant(CTFParticipant participant) {
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        sidebar.removePlayer(participant.getUniqueId());
        topbar.hidePlayer(participant.getUniqueId());
    }
    
    private void startAdmins(List<Player> newAdmins) {
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        topbar.showPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getSpawnObservatory());
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
        topbar.hidePlayer(admin.getUniqueId());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        state.stop();
        saveScores();
        for (CTFParticipant participant : participants.values()) {
            resetParticipant(participant);
        }
        participants.clear();
        quitDatas.clear();
        teams.clear();
        stopAdmins();
        clearSidebar();
        gameManager.gameIsOver();
        Main.logger().info("Stopping Capture the Flag");
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (CTFTeam team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (CTFParticipant participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (Map.Entry<String, CTFTeam.QuitData> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, CTFParticipant.QuitData> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    /**
     * Called when a team has a member join, even if members of that team are already present
     * @param team the team which had a member join
     */
    public void onTeamJoin(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        CTFTeam.QuitData quitData = teamQuitDatas.remove(team.getTeamId());
        quitTeams.remove(team.getTeamId());
        if (quitData != null) {
            teams.put(team.getTeamId(), new CTFTeam(team, quitData));
        } else {
            teams.put(team.getTeamId(), new CTFTeam(team, 0));
        }
        roundManager.regenerateRounds(Team.getTeamIds(teams),
                config.getArenas().size());
        updateRoundLine();
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        state.onParticipantJoin(participant, team);
        state.updateSidebar(participant, this);
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        CTFParticipant participant = participants.get(participantUUID);
        if (participant == null) {
            return;
        }
        state.onParticipantQuit(participant);
    }
    
    /**
     * called when a participant on the given team quits, not just when the last member quits
     * @param team the team that had a member quit
     */
    public void onTeamQuit(CTFTeam team) {
        if (team.size() > 0) {
            return;
        }
        CTFTeam removed = teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), removed.getQuitData());
        quitTeams.put(team.getTeamId(), team);
        roundManager.regenerateRounds(Team.getTeamIds(teams),
                config.getArenas().size());
        updateRoundLine();
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        adminSidebar.updateLine("round", roundLine);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("round", "")
        );
        for (CTFTeam team : teams.values()) {
            displayScore(team);
        }
        for (CTFParticipant participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    public void updateRoundLine(UUID participantUUID) {
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        sidebar.updateLine(participantUUID, "round", roundLine);
    }
    
    public void updateRoundLine() {
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        sidebar.updateLine("round", roundLine);
        adminSidebar.updateLine("round", roundLine);
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        topbar.removeAllTeamPairs();
        topbar.hideAllPlayers();
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("round", ""),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
    }
    
    public void displayScore(CTFTeam team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
    }
    
    public void displayScore(CTFParticipant participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        state.onPlayerDeath(event);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_DAMAGE_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!participants.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!participants.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        state.onPlayerLoseHunger(event);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        state.onClickInventory(event);
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
    public void onPlayerInteract(PlayerInteractEvent event) {
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
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        boolean cancelled = handleSpectators(event);
        if (cancelled) {
            return;
        }
        state.onPlayerMove(event);
    }
    
    /**
     * prevent spectators from leaving the spectator area
     * @param event the move event
     * @return true if the event was cancelled due to preventing spectator movement
     */
    private boolean handleSpectators(PlayerMoveEvent event) {
        if (config.getSpectatorArea() == null){
            return false;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return false;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getSpawnObservatory());
            return false;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
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
    
    /**
     * Messages all the participants of the game (whether they're in a match or not)
     * @param message The message to send
     */
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Participant participant : participants.values()) {
            participant.sendMessage(message);
        }
    }
}
