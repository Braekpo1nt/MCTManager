package org.braekpo1nt.mctmanager.games.gamemanager.states;

import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.Config;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfigController;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfigController;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.games.game.example.config.ExampleConfig;
import org.braekpo1nt.mctmanager.games.game.example.config.ExampleConfigController;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfigController;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfigController;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfigController;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.config.HubConfigController;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.participant.ColorAttributes;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class GameManagerState {
    
    protected final @NotNull GameManager context;
    protected final @NotNull ContextReference contextReference;
    protected final @NotNull TabList tabList;
    protected final Scoreboard mctScoreboard;
    
    protected final Map<String, MCTTeam> teams;
    protected final Map<UUID, OfflineParticipant> allParticipants;
    protected final Map<UUID, MCTParticipant> onlineParticipants;
    protected final List<Player> onlineAdmins;
    protected final Main plugin;
    protected final GameStateStorageUtil gameStateStorageUtil;
    protected final SidebarFactory sidebarFactory;
    protected final List<LeaderboardManager> leaderboardManagers;
    protected final Sidebar sidebar;
    protected final @NotNull Map<GameInstanceId, MCTGame> activeGames;
    /**
     * A reference to which participant is in which game<br>
     * If a participant's UUID is a key in this map, that participant is
     * in a game.
     */
    protected final Map<UUID, GameInstanceId> participantGames;
    /**
     * A reference to which admin is in which game<br>
     * If an admin's UUID is a key in this map, that admin is in
     * a game.
     */
    protected final Map<UUID, GameInstanceId> adminGames;
    /**
     * A reference to which admin is in which editor<br>
     * If an admin's UUID is a key in this map, that admin is in
     * an editor.
     */
    protected final Map<UUID, GameInstanceId> adminEditors;
    private final @NotNull Executor mainThreadExecutor;
    @Setter
    protected @NotNull HubConfig config;
    
    public GameManagerState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference) {
        this.context = context;
        this.contextReference = contextReference;
        this.tabList = contextReference.getTabList();
        this.leaderboardManagers = contextReference.getLeaderboardManagers();
        this.sidebar = contextReference.getSidebar();
        this.mctScoreboard = contextReference.getMctScoreboard();
        this.participantGames = contextReference.getParticipantGames();
        this.adminGames = contextReference.getAdminGames();
        this.adminEditors = contextReference.getAdminEditors();
        this.plugin = contextReference.getPlugin();
        this.activeGames = contextReference.getActiveGames();
        this.gameStateStorageUtil = contextReference.getGameStateStorageUtil();
        this.sidebarFactory = contextReference.getSidebarFactory();
        
        this.teams = contextReference.getTeams();
        this.allParticipants = contextReference.getAllParticipants();
        this.onlineParticipants = contextReference.getOnlineParticipants();
        this.onlineAdmins = contextReference.getOnlineAdmins();
        this.config = context.getConfig();
        this.mainThreadExecutor = contextReference.getMainThreadExecutor();
    }
    
    /**
     * Called after this state is assigned to the context
     */
    public abstract void enter();
    
    /**
     * Called before this state is un-assigned from the context
     */
    public abstract void exit();
    
    public abstract CommandResult switchMode(@NotNull Mode mode);
    
    public abstract @NotNull Mode getMode();
    
    public void cleanup() {
        this.leaderboardManagers.forEach(LeaderboardManager::tearDown);
        this.tabList.cleanup();
        this.sidebar.deleteAllLines();
        this.onlineAdmins.clear();
        this.onlineParticipants.clear();
        this.teams.clear();
        this.allParticipants.clear();
    }
    
    protected abstract @NotNull CompletableFuture<CommandResult> rebuildFromScores();
    
    public @NotNull CompletableFuture<CommandResult> loadGameState() {
        CompletableFuture<CommandResult> chain = CompletableFuture.completedFuture(CommandResult.success());
        if (!context.getActiveGameIds().isEmpty()) {
            chain = chain.thenCompose(ignored -> stopAllGames());
        }
        if (editorIsRunning()) {
            chain = chain.thenApplyAsync(results -> results.and(stopEditor()), mainThreadExecutor);
        }
        // a given participant or admin may not be re-added when loading the new state
        for (Player admin : new ArrayList<>(onlineAdmins)) {
            onAdminQuit(admin);
        }
        for (MCTParticipant participant : new ArrayList<>(onlineParticipants.values())) {
            chain = chain.thenCompose(results -> {
                CompletableFuture<Void> joinFuture = onParticipantQuit(participant);
                return joinFuture.thenApply(v -> results);
            });
        }
        return CommandResult.appendAsync(
                        chain,
                        this::rebuildAndReload,
                        mainThreadExecutor
                )
                .thenComposeAsync(compositeResult -> {
                    CompletableFuture<CommandResult> joinFuture = loadGameStateMainThread();
                    return joinFuture.thenApply(loadResult -> loadResult.and(compositeResult));
                }, mainThreadExecutor);
    }
    
    protected @NotNull CompletableFuture<CommandResult> rebuildAndReload() {
        return rebuildFromScores()
                .thenComposeAsync(result -> {
                    try {
                        CompletableFuture<Void> joinFuture = gameStateStorageUtil.loadGameState();
                        return joinFuture.thenApply(v -> result);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, context.getMainThreadExecutor())
                .exceptionally(e -> CommandResult.throwable("loading game state", e));
    }
    
    public abstract @NotNull String getSystemStateDescription();
    
    /**
     * The part of the loading the game state action that should be run on the main
     * Bukkit thread, because it impacts the world state
     * @return a result detailing the success or failure of the action
     */
    protected @NotNull CompletableFuture<CommandResult> loadGameStateMainThread() {
        List<CommandResult> results = new ArrayList<>();
        try {
            this.config = new HubConfigController(plugin.getDataFolder()).getConfig();
            this.leaderboardManagers.forEach(LeaderboardManager::tearDown);
            this.leaderboardManagers.clear();
            this.leaderboardManagers.addAll(context.createLeaderboardManagers());
            setConfig(this.config);
            results.add(CommandResult.success(Component.text("Loaded hub config")));
        } catch (ConfigException e) {
            results.add(CommandResult.failure(Component.text("Could not load hub config. See console for details.")));
            Main.logger().log(Level.SEVERE, String.format("Could not load new hub config, reverting to last working one. See console for details. %s", e.getMessage()), e);
        }
        gameStateStorageUtil.setupScoreboard(mctScoreboard);
        teams.clear();
        allParticipants.clear();
        onlineParticipants.clear();
        onlineAdmins.clear();
        for (String teamId : gameStateStorageUtil.getTeamIds()) {
            String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamId);
            NamedTextColor teamColor = gameStateStorageUtil.getTeamColor(teamId);
            ColorAttributes colorAttributes = gameStateStorageUtil.getTeamColorAttributes(teamId);
            List<UUID> members = gameStateStorageUtil.getParticipantUUIDsOnTeam(teamId);
            int score = gameStateStorageUtil.getTeamScore(teamId);
            MCTTeam team = new MCTTeam(
                    teamId,
                    teamDisplayName,
                    teamColor,
                    colorAttributes,
                    members,
                    score);
            teams.put(teamId, team);
        }
        for (UUID uuid : gameStateStorageUtil.getPlayerUniqueIds()) {
            OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(uuid);
            if (offlineParticipant != null) {
                allParticipants.put(offlineParticipant.getUniqueId(), offlineParticipant);
            }
        }
        Map<UUID, Player> onlinePlayers = plugin.getServer().getOnlinePlayers().stream()
                .collect(Collectors.toMap(Player::getUniqueId, Function.identity()));
        // TabList start
        tabList.cleanup();
        for (MCTTeam team : teams.values()) {
            int teamScore = gameStateStorageUtil.getTeamScore(team.getTeamId());
            tabList.addTeam(team.getTeamId(), team.getDisplayName(), team.getColor());
            tabList.setScore(team.getTeamId(), teamScore);
        }
        for (OfflineParticipant participant : allParticipants.values()) {
            boolean grey = !onlinePlayers.containsKey(participant.getUniqueId());
            tabList.joinParticipant(participant.getParticipantID(), participant.getName(), participant.getTeamId(), grey);
        }
        // TabList stop
        
        // Log on all online admins and participants
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (Player player : onlinePlayers.values()) {
            if (context.isAdmin(player.getUniqueId())) {
                onAdminJoin(player);
            }
            OfflineParticipant offlineParticipant = allParticipants.get(player.getUniqueId());
            if (offlineParticipant != null) {
                MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
                chain = chain.thenComposeAsync(v -> onParticipantJoin(participant), mainThreadExecutor);
            }
        }
        
        // sidebar start
        updateSidebarTeamScores();
        updateSidebarPersonalScores(onlineParticipants.values());
        // sidebar stop
        results.add(CommandResult.success(Component.text("Loaded gameState")));
        postLoadGameState();
        return chain.thenApply(v -> CompositeCommandResult.all(results));
    }
    
    protected void postLoadGameState() {
        // do nothing
    }
    
    // leave/join start
    public final void onAdminJoin(@NotNull PlayerJoinEvent event, @NotNull Player admin) {
        onAdminJoin(admin);
        event.joinMessage(GameManagerUtils.replaceWithDisplayName(admin, event.joinMessage()));
    }
    
    public void onAdminJoin(@NotNull Player admin) {
        context.getOnlineAdmins().add(admin);
        admin.setScoreboard(context.getMctScoreboard());
        admin.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.empty()
                .append(Component.text("[Admin]")
                        .color(GameManager.ADMIN_COLOR))
                .append(Component.text(admin.getName()));
        admin.displayName(displayName);
        admin.playerListName(displayName);
        tabList.showPlayer(admin);
        sidebar.addPlayer(admin);
        updateSidebarTeamScores();
    }
    
    public final CompletableFuture<Void> onParticipantJoin(@NotNull PlayerJoinEvent event, @NotNull MCTParticipant participant) {
        CompletableFuture<Void> joinFuture = onParticipantJoin(participant);
        event.joinMessage(GameManagerUtils.replaceWithDisplayName(participant, event.joinMessage()));
        return joinFuture;
    }
    
    public void onNonJoin(@NotNull Player player) {
        // do nothing
    }
    
    /**
     * Handles when a participant joins
     * @param participant the participant who joined
     * @return any async work done
     */
    public CompletableFuture<Void> onParticipantJoin(@NotNull MCTParticipant participant) {
        onlineParticipants.put(participant.getUniqueId(), participant);
        MCTTeam team = teams.get(participant.getTeamId());
        team.joinOnlineMember(participant);
        setupScoreboard(participant);
        participant.addPotionEffect(Main.NIGHT_VISION);
        participant.setGameMode(GameMode.ADVENTURE);
        Component displayName = Component.text(participant.getName(), team.getColor());
        participant.getPlayer().displayName(displayName);
        participant.getPlayer().playerListName(displayName);
        tabList.showPlayer(participant);
        tabList.setParticipantGrey(participant.getParticipantID(), false);
        sidebar.addPlayer(participant);
        ColorMap.colorLeatherArmor(participant, team.getBukkitColor());
        leaderboardManagers.forEach(manager -> manager.showPlayer(participant.getPlayer()));
        updateScoreVisuals(Collections.singletonList(team), Collections.singletonList(participant));
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Set up the internal scoreboard connections
     * @param participant the participant to set up the scoreboard for
     */
    private void setupScoreboard(@NotNull MCTParticipant participant) {
        participant.getPlayer().setScoreboard(mctScoreboard);
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(participant.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.addPlayer(participant.getPlayer());
        } else {
            Main.logger().severe(String.format("Error retrieving team with ID %s from scoreboard", participant.getTeamId()));
        }
    }
    
    public final void onAdminQuit(@NotNull PlayerQuitEvent event, @NotNull Player admin) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(admin, event.quitMessage()));
        onAdminQuit(admin);
    }
    
    public void onAdminQuit(@NotNull Player admin) {
        GameInstanceId id = adminGames.get(admin.getUniqueId());
        if (id != null) {
            MCTGame activeGame = activeGames.get(id);
            if (activeGame != null) {
                activeGame.onAdminQuit(admin);
            }
            onAdminReturnToHub(admin);
        }
        onlineAdmins.remove(admin);
        GameInstanceId gameInstanceId = adminGames.get(admin.getUniqueId());
        if (gameInstanceId != null) {
            MCTGame game = activeGames.get(gameInstanceId);
            if (game != null) {
                game.onAdminQuit(admin);
            }
        }
        GameInstanceId editorId = adminEditors.get(admin.getUniqueId());
        if (editorId != null) {
            if (context.getActiveEditor() != null) {
                context.getActiveEditor().onAdminQuit(admin.getUniqueId());
            }
        }
        tabList.hidePlayer(admin.getUniqueId());
        sidebar.removePlayer(admin);
        Component displayName = Component.text(admin.getName(), NamedTextColor.WHITE);
        admin.displayName(displayName);
        admin.playerListName(displayName);
    }
    
    public final CompletableFuture<Void> onParticipantQuit(@NotNull PlayerQuitEvent event, @NotNull MCTParticipant participant) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(participant, event.quitMessage()));
        return onParticipantQuit(participant);
    }
    
    /**
     * Handles when a participant leaves the event.
     * Should be called when a participant disconnects (quits/leaves) from the server
     * (see {@link GameManager#onPlayerQuit(PlayerQuitEvent)}),
     * or when they are removed from the participants list
     * @param participant The participant who left the event
     * @return database operations in alt thread future
     * @see GameManager#leaveParticipant(OfflineParticipant)
     */
    public CompletableFuture<Void> onParticipantQuit(@NotNull MCTParticipant participant) {
        GameInstanceId id = participantGames.get(participant.getUniqueId());
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        if (id != null) {
            MCTGame activeGame = activeGames.get(id);
            if (activeGame != null) {
                CompletableFuture<Void> quitGameFuture = activeGame.onQuit(participant.getTeamId(), participant.getUniqueId());
                chain = chain.thenCompose(v -> quitGameFuture);
            }
            onParticipantReturnToHub(participant);
            participant.teleport(config.getSpawn());
        }
        MCTTeam team = teams.get(participant.getTeamId());
        team.quitOnlineMember(participant.getUniqueId());
        onlineParticipants.remove(participant.getUniqueId());
        tabList.hidePlayer(participant.getUniqueId());
        sidebar.removePlayer(participant);
        Component displayName = Component.text(participant.getName(),
                NamedTextColor.WHITE);
        participant.getPlayer().displayName(displayName);
        participant.getPlayer().playerListName(displayName);
        GameManagerUtils.deColorLeatherArmor(participant.getInventory());
        tabList.setParticipantGrey(participant.getParticipantID(), true);
        leaderboardManagers.forEach(manager -> manager.hidePlayer(participant.getPlayer()));
        return chain;
    }
    // leave/join end
    
    // ui start
    public void updateScoreVisuals(Collection<? extends Team> mctTeams, Collection<? extends Participant> mctParticipants) {
        if (!mctTeams.isEmpty()) {
            updateSidebarTeamScores();
        }
        updateSidebarPersonalScores(mctParticipants);
        tabList.setScores(mctTeams);
        context.updateLeaderboards();
    }
    
    /**
     * @return a new sidebar
     */
    public Sidebar createSidebar() {
        return sidebarFactory.createSidebar();
    }
    
    /**
     * Reorders the team lines in the sidebar from highest to lowest
     * score, up to 10
     */
    public void updateSidebarTeamScores() {
        List<Team> sortedTeams = context.getSortedTeams();
        int numOfTeamLines = Math.min(10, sortedTeams.size());
        KeyLine[] teamLines = new KeyLine[10];
        for (int i = 0; i < numOfTeamLines; i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team" + i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        // fill out any empty lines
        for (int i = numOfTeamLines; i < 10; i++) {
            teamLines[i] = new KeyLine("team" + i, Component.empty());
        }
        sidebar.updateLines(teamLines);
    }
    
    /**
     * Updates the sidebars of the given participants, unless they are in a game
     * @param updateParticipants the participants to update the sidebars of
     */
    public void updateSidebarPersonalScores(Collection<? extends Participant> updateParticipants) {
        for (Participant participant : updateParticipants) {
            if (!isParticipantInGame(participant)) {
                sidebar.updateLine(participant.getUniqueId(), "personalScore",
                        Component.empty()
                                .append(Component.text("Personal: "))
                                .append(Component.text(participant.getScore()))
                                .color(NamedTextColor.GOLD));
            }
        }
    }
    
    protected void displayStats(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull GameInstanceId id) {
        List<MCTTeam> sortedTeams = teamScores.keySet().stream()
                .map(teams::get)
                .filter(t -> teamScores.containsKey(t.getTeamId()))
                .sorted(Comparator.comparing(
                        t -> teamScores.get(t.getTeamId()),
                        Comparator.reverseOrder()))
                .toList();
        List<OfflineParticipant> sortedParticipants = participantScores.keySet().stream()
                .map(allParticipants::get)
                .filter(p -> participantScores.containsKey(p.getUniqueId()))
                .sorted(Comparator.comparing(
                        p -> participantScores.get(p.getUniqueId()),
                        Comparator.reverseOrder()))
                .toList();
        
        TextComponent.Builder everyone = Component.text();
        everyone.append(
                Component.empty()
                        .append(Component.text("["))
                        .append(Component.text(getMultiplier()))
                        .append(Component.text(" Multiplier]\n"))
                        .color(NamedTextColor.GRAY)
        );
        everyone.append(Component.text("Top 5 Teams:"))
                .append(Component.newline());
        for (int i = 0; i < Math.min(sortedTeams.size(), 5); i++) {
            MCTTeam team = sortedTeams.get(i);
            everyone
                    .append(Component.text("  "))
                    .append(Component.text(i + 1))
                    .append(Component.text(". "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScores.get(team.getTeamId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.newline());
        }
        
        // TODO: adjust output to not show scores of 0 instead of specifically farm rush
        if (id.getGameType() == GameType.FARM_RUSH) {
            Audience.audience(
                    Audience.audience(sortedTeams),
                    plugin.getServer().getConsoleSender()
            ).sendMessage(everyone.build());
            return;
        }
        
        everyone.append(Component.text("\nTop 5 Participants:"))
                .append(Component.newline());
        boolean shouldShowMultiplier = getMultiplier() != 1.0;
        for (int i = 0; i < Math.min(sortedParticipants.size(), 5); i++) {
            OfflineParticipant participant = sortedParticipants.get(i);
            everyone
                    .append(Component.text("  "))
                    .append(Component.text(i + 1))
                    .append(Component.text(". "))
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(Component.text(participantScores.get(participant.getUniqueId()))
                            .color(NamedTextColor.GOLD));
            if (shouldShowMultiplier) {
                everyone
                        .append(Component.text(" ("))
                        .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(" x "))
                        .append(Component.text(getMultiplier()))
                        .append(Component.text(")"));
            }
            everyone
                    .append(Component.newline());
        }
        Audience.audience(
                Audience.audience(sortedTeams),
                plugin.getServer().getConsoleSender()
        ).sendMessage(everyone.build());
        
        for (MCTTeam team : sortedTeams) {
            TextComponent.Builder message = Component.text();
            message
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScores.get(team.getTeamId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.newline());
            int i = 1;
            for (OfflineParticipant participant : sortedParticipants) {
                if (participant.getTeamId().equals(team.getTeamId())) {
                    message
                            .append(Component.text("  "))
                            .append(Component.text(i))
                            .append(Component.text(". "))
                            .append(participant.displayName())
                            .append(Component.text(": "))
                            .append(Component.text(participantScores.get(participant.getUniqueId()))
                                    .color(NamedTextColor.GOLD));
                    if (shouldShowMultiplier) {
                        message
                                .append(Component.text(" ("))
                                .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(" x "))
                                .append(Component.text(getMultiplier()))
                                .append(Component.text(")"));
                    }
                    message
                            .append(Component.newline());
                    i++;
                }
            }
            team.sendMessage(message.build());
        }
        
        for (OfflineParticipant offlineParticipant : sortedParticipants) {
            Participant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
            if (participant != null) {
                TextComponent.Builder message = Component.text();
                message
                        .append(Component.text("Personal")
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(": "))
                        .append(Component.text(participantScores.get(offlineParticipant.getUniqueId()))
                                .color(NamedTextColor.GOLD));
                if (shouldShowMultiplier) {
                    message
                            .append(Component.text(" ("))
                            .append(Component.text((int) (participantScores.get(offlineParticipant.getUniqueId()) / getMultiplier()))
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text(" x "))
                            .append(Component.text(getMultiplier()))
                            .append(Component.text(")"));
                }
                participant.sendMessage(message);
            }
        }
    }
    // ui end
    
    // game start
    public CompletableFuture<CommandResult> startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        GameInstanceId gameInstanceId = new GameInstanceId(gameType, configFile);
        if (teamIds.isEmpty()) {
            return CommandResult.failure("Can't start a game with no teams.").asFuture();
        }
        
        if (activeGames.containsKey(gameInstanceId)) {
            return CommandResult.failure(Component.text("There is already a ")
                    .append(Component.text(gameInstanceId.getTitle()))
                    .append(Component.text(" game running."))
            ).asFuture();
        }
        
        if (context.editorIsRunning()) {
            return CommandResult.failure(Component.text("There is an editor running. You must stop the editor before you start a game.")
            ).asFuture();
        }
        
        if (onlineParticipants.isEmpty()) {
            return CommandResult.failure(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join ")))
            ).asFuture();
        }
        
        Set<MCTTeam> gameTeams = new HashSet<>();
        Set<MCTParticipant> gameParticipants = new HashSet<>();
        for (String teamId : teamIds) {
            MCTTeam team = teams.get(teamId);
            if (team == null) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid teamId"))
                ).asFuture();
            }
            Collection<MCTParticipant> onlineMembers = team.getOnlineMembers();
            for (MCTParticipant onlineMember : onlineMembers) {
                if (participantGames.containsKey(onlineMember.getUniqueId())) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text("Can't start a game with "))
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" because one of its members is already in a game"))
                    ).asFuture();
                }
            }
            if (!onlineMembers.isEmpty()) {
                gameTeams.add(team);
                gameParticipants.addAll(onlineMembers);
            }
        }
        // make sure the player and team count requirements are met
        if (gameTeams.isEmpty()) {
            return CommandResult.failure("None of the specified teams are online").asFuture();
        }
        switch (gameType) {
            case CAPTURE_THE_FLAG -> {
                if (gameTeams.size() < 2) {
                    return CommandResult.failure(Component.text("Capture the Flag needs at least 2 teams online to play.").color(NamedTextColor.RED)
                    ).asFuture();
                }
            }
            case FINAL, COLOSSAL_COMBAT -> {
                if (gameTeams.size() < 2) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(gameType.getTitle()))
                            .append(Component.text(" needs at least 2 teams online to play"))
                    ).asFuture();
                }
            }
        }
        
        return instantiateGame(
                gameInstanceId,
                new HashSet<>(gameTeams),
                new HashSet<>(gameParticipants),
                gameAdmins);
    }
    
    public CommandResult startEditor(@NotNull GameType gameType, @NotNull String configFile) {
        GameInstanceId gameInstanceId = new GameInstanceId(gameType, configFile);
        if (!activeGames.isEmpty()) {
            return CommandResult.failure(Component.text("Can't start an editor while any games are active"));
        }
        
        if (onlineAdmins.isEmpty()) {
            return CommandResult.failure(Component.text("There are no online admins. You can add admins using:\n")
                    .append(Component.text("/mct admin add <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct admin add "))));
        }
        
        if (editorIsRunning()) {
            return CommandResult.failure(Component.text("An editor is already running. You must stop it before you can start another one."));
        }
        
        if (!editorExists(gameType)) {
            return CommandResult.failure(Component.text("Can't find editor for game type " + gameType));
        }
        
        for (Player admin : onlineAdmins) {
            onAdminJoinEditor(gameInstanceId, admin);
        }
        
        try {
            context.setActiveEditor(instantiateEditor(
                    gameType,
                    configFile,
                    new ArrayList<>(onlineAdmins)
            ));
        } catch (Exception e) {
            for (Player admin : onlineAdmins) {
                onAdminReturnToHub(admin);
            }
            Main.logger().log(Level.SEVERE, String.format("Error starting editor %s", gameType), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.empty()
                            .append(Component.text(gameType.name()))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("'s editor. Error starting editor. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
        }
        return CommandResult.success();
    }
    
    protected void onParticipantJoinGame(@NotNull GameInstanceId id, Participant participant) {
        participantGames.put(participant.getUniqueId(), id);
        tabList.hidePlayer(participant);
        sidebar.removePlayer(participant);
    }
    
    protected void onAdminJoinGame(@NotNull GameInstanceId id, Player admin) {
        adminGames.put(admin.getUniqueId(), id);
        tabList.hidePlayer(admin);
        sidebar.removePlayer(admin);
    }
    
    protected void onAdminJoinEditor(@NotNull GameInstanceId id, Player admin) {
        adminEditors.put(admin.getUniqueId(), id);
        tabList.hidePlayer(admin);
        sidebar.removePlayer(admin);
    }
    
    protected void onParticipantReturnToHub(@NotNull Participant participant) {
        participantGames.remove(participant.getUniqueId());
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        tabList.showPlayer(participant);
        sidebar.addPlayer(participant);
        MCTTeam team = teams.get(participant.getTeamId());
        updateScoreVisuals(Collections.singletonList(team), Collections.singletonList(participant));
    }
    
    protected void onAdminReturnToHub(@NotNull Player admin) {
        adminGames.remove(admin.getUniqueId());
        adminEditors.remove(admin.getUniqueId());
        admin.setGameMode(GameMode.SPECTATOR);
        tabList.showPlayer(admin);
        sidebar.addPlayer(admin);
        updateSidebarTeamScores();
    }
    
    public CompletableFuture<CommandResult> stopGame(@NotNull GameType gameType, @Nullable String configFile) {
        GameInstanceId id;
        if (configFile == null) {
            List<GameInstanceId> activeIds = new ArrayList<>();
            for (GameInstanceId gameInstanceId : activeGames.keySet()) {
                if (gameInstanceId.getGameType().equals(gameType)) {
                    activeIds.add(gameInstanceId);
                }
            }
            if (activeIds.size() == 1) {
                id = activeIds.getFirst();
            } else if (activeIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" game is active right now"))
                ).asFuture();
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("More than one instance of "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" is active. Please specify a config."))
                ).asFuture();
            }
        } else {
            id = new GameInstanceId(gameType, configFile);
        }
        MCTGame game = activeGames.get(id);
        if (game == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No instances of game "))
                    .append(Component.text(id.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" are active"))
            ).asFuture();
        }
        return game.stop()
                .thenApply(v -> CommandResult.success(Component.empty()
                        .append(Component.text("Stopping "))
                        .append(Component.text(id.getTitle()))
                        .append(Component.text(" ("))
                        .append(Component.text(id.getConfigFile()))
                        .append(Component.text(")")))
                );
    }
    
    public CompletableFuture<CommandResult> stopAllGames() {
        if (activeGames.isEmpty()) {
            return CommandResult.success(Component.text("No games are running")).asFuture();
        }
        List<MCTGame> gamesToCancel = new ArrayList<>(activeGames.values());
        CompletableFuture<CommandResult> chain = CompletableFuture.completedFuture(CommandResult.success());
        for (MCTGame game : gamesToCancel) {
            chain = game.stop()
                    .thenApply(v -> CommandResult.success(Component.empty()
                            .append(Component.text("Stopped "))
                            .append(Component.text(game.getType().getTitle()))));
        }
        return chain;
    }
    
    /**
     * Called by an active game when the game is over.
     * @param gameSessionId the id of the {@link GameSession} entity associated with the finished game
     * @param id the instance id of the finished game
     * @param teamScores the team scores
     * @param participantScores the participant scores
     * @param gameParticipants the UUIDs of the participants which are online and were in the finished
     * game. Must be UUIDs which are keys in {@link #onlineParticipants}.
     * @param gameAdmins the admins who were in the game
     * @return a future containing database operations
     */
    public CompletableFuture<Void> gameIsOver(int gameSessionId, @NotNull GameInstanceId id, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants, @NotNull List<Player> gameAdmins) {
        MCTGame game = activeGames.remove(id);
        if (game == null) {
            return CompletableFuture.completedFuture(null);
        }
        for (UUID uuid : gameParticipants) {
            MCTParticipant participant = onlineParticipants.get(uuid);
            onParticipantReturnToHub(participant);
            participant.teleport(config.getSpawn());
            participant.sendMessage(Component.text("Returning to hub"));
        }
        for (Player admin : gameAdmins) {
            onAdminReturnToHub(admin);
            admin.teleport(config.getSpawn());
            admin.sendMessage(Component.text("Returning to hub"));
        }
        Date endDate = new Date();
        return addScores(teamScores, participantScores, id)
                .thenComposeAsync(v -> CompletableFuture.runAsync(() -> {
                    try {
                        context.getScoreService().setGameSessionEndDate(gameSessionId, endDate);
                    } catch (SQLException e) {
                        Main.logger().log(Level.SEVERE, "An error occurred saving end-game data to the database", e);
                        context.messageAdmins(Component.empty()
                                .append(Component.text("An error occurred saving end-game data to te database. See console for details.")));
                    }
                }, plugin.getDatabaseExecutor()))
                ;
    }
    
    /**
     * Add the given scores to the given teams and participants, save the game state, update
     * the UI, etc. <br>
     * If any invalid teamIds or UUIDs are used, there will be errors
     * @param newTeamScores map of teamId to score to add. Must be teamIds of real teams
     * @param newParticipantScores map of UUID to score to add. Must be UUIDs of real participants
     * @param id the {@link GameInstanceId}
     * @return a future with the database operations
     */
    protected CompletableFuture<Void> addScores(
            Map<String, Integer> newTeamScores,
            Map<UUID, Integer> newParticipantScores,
            @NotNull GameInstanceId id) {
        // some values might be from offline teams who have been removed, but still saved as QuitData
        Map<String, Integer> teamScores = newTeamScores.entrySet().stream()
                .filter(e -> teams.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, Integer> participantScores = newParticipantScores.entrySet().stream()
                .filter(e -> allParticipants.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            String teamId = entry.getKey();
            int newScore = entry.getValue();
            MCTTeam team = teams.get(teamId);
            teams.put(teamId, new MCTTeam(team, team.getScore() + newScore));
        }
        for (Map.Entry<UUID, Integer> entry : participantScores.entrySet()) {
            UUID uuid = entry.getKey();
            int newScore = entry.getValue();
            OfflineParticipant offlineParticipant = allParticipants.get(uuid);
            allParticipants.put(uuid, new OfflineParticipant(offlineParticipant,
                    offlineParticipant.getScore() + newScore));
            MCTParticipant participant = onlineParticipants.get(uuid);
            if (participant != null) {
                onlineParticipants.put(uuid, new MCTParticipant(participant,
                        participant.getScore() + newScore));
            }
        }
        if (plugin.isEnabled()) {
            gameStateStorageUtil.updateScores(teams.values(), allParticipants.values());
        }
        // TODO: this may not be needed if active_* tables are rebuilt on plugin start
        updateScoreVisuals(teams.values(), onlineParticipants.values());
        displayStats(teamScores, participantScores, id);
        return CompletableFuture.runAsync(() -> {
            try {
                gameStateStorageUtil.persistScores(teams.values(), allParticipants.values());
            } catch (Exception e) {
                context.reportGameStateException("updating scores", e);
            }
        }, plugin.getDatabaseExecutor());
    }
    
    @Nullable public String getEventId() {
        return null;
    }
    
    /**
     * Meant to be called from an asynchronous thread<br>
     * Loads the config file. If that succeeds, creates an entry for the game in the database.
     * If that succeeds, start the game on the main thread.
     * @param gameInstanceId the gameInstanceId
     * @param newTeams the teams to send to the game
     * @param newParticipants the participants to send to the game
     * @param newAdmins the admins to send to the game
     * @return a future result of starting the game
     */
    private CompletableFuture<CommandResult> instantiateGame(
            @NotNull GameInstanceId gameInstanceId,
            Collection<Team> newTeams,
            Collection<Participant> newParticipants,
            List<Player> newAdmins
    ) {
        GameType gameType = gameInstanceId.getGameType();
        String configFile = gameInstanceId.getConfigFile();
        
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return context.getScoreService().createGameSession(GameSession.builder()
                                .gameType(gameType)
                                .eventId(getEventId())
                                .configFile(configFile)
                                .startTime(new Date())
                                .mode(getMode())
                                .multiplier(getMultiplier())
                                .build());
                    } catch (SQLException e) {
                        throw new CompletionException("Unable to create gameSession in database", e);
                    }
                }, plugin.getDatabaseExecutor())
                // intentionally not catching with exceptionally clause, so that callers can decide how to proceed
                .thenApplyAsync(gameSession -> _instantiateGame(
                        gameInstanceId,
                        newTeams,
                        newParticipants,
                        newAdmins,
                        gameSession,
                        gameType,
                        configFile
                ), plugin.getMainThreadExecutor());
    }
    
    private @NotNull CommandResult _instantiateGame(
            @NotNull GameInstanceId gameInstanceId,
            Collection<Team> newTeams,
            Collection<Participant> newParticipants,
            List<Player> newAdmins,
            @NotNull GameSession gameSession,
            GameType gameType,
            String configFile
    ) {
        Component title = createNewTitle(gameType.getTitle());
        Config config = switch (gameType) {
            case SPLEEF -> new SpleefConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case CLOCKWORK ->
                    new ClockworkConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case SURVIVAL_GAMES ->
                    new SurvivalGamesConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case FARM_RUSH ->
                    new FarmRushConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case FOOT_RACE ->
                    new FootRaceConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case PARKOUR_PATHWAY ->
                    new ParkourPathwayConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case CAPTURE_THE_FLAG ->
                    new CaptureTheFlagConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case EXAMPLE -> new ExampleConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case FINAL ->
                    new ColossalCombatConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
            case COLOSSAL_COMBAT ->
                    new FinalConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
        };
        for (Participant participant : newParticipants) {
            onParticipantJoinGame(gameInstanceId, participant);
        }
        for (Player admin : newAdmins) {
            onAdminJoinGame(gameInstanceId, admin);
        }
        try {
            MCTGame game = switch (gameType) {
                case SPLEEF -> {
                    yield new SpleefGame(plugin, context, title, gameSession.getId(), (SpleefConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case CLOCKWORK -> {
                    yield new ClockworkGame(plugin, context, title, gameSession.getId(), (ClockworkConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case SURVIVAL_GAMES -> {
                    yield new SurvivalGamesGame(plugin, context, title, gameSession.getId(), (SurvivalGamesConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case FARM_RUSH -> {
                    yield new FarmRushGame(plugin, context, title, gameSession.getId(), (FarmRushConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case FOOT_RACE -> {
                    yield new FootRaceGame(plugin, context, title, gameSession.getId(), (FootRaceConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case PARKOUR_PATHWAY -> {
                    yield new ParkourPathwayGame(plugin, context, title, gameSession.getId(), (ParkourPathwayConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case CAPTURE_THE_FLAG -> {
                    yield new CaptureTheFlagGame(plugin, context, title, gameSession.getId(), (CaptureTheFlagConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case EXAMPLE -> {
                    yield new ExampleGame(plugin, context, title, gameSession.getId(), (ExampleConfig) config, configFile, newTeams, newParticipants, newAdmins);
                }
                case FINAL -> {
                    List<Team> sortedTeams = newTeams.stream()
                            .sorted((t1, t2) -> {
                                int scoreComparison = t2.getScore() - t1.getScore();
                                if (scoreComparison != 0) {
                                    return scoreComparison;
                                }
                                return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
                            }).toList();
                    yield new ColossalCombatGame(plugin, context, title, gameSession.getId(), (ColossalCombatConfig) config, configFile, sortedTeams.getFirst(), sortedTeams.get(1), sortedTeams, newParticipants, newAdmins);
                }
                case COLOSSAL_COMBAT -> {
                    List<Team> sortedTeams = newTeams.stream()
                            .sorted((t1, t2) -> {
                                int scoreComparison = t2.getScore() - t1.getScore();
                                if (scoreComparison != 0) {
                                    return scoreComparison;
                                }
                                return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
                            }).toList();
                    yield new FinalGame(plugin, context, title, gameSession.getId(), (FinalConfig) config, configFile, sortedTeams.getFirst(), sortedTeams.get(1), sortedTeams, newParticipants, newAdmins);
                }
            };
            activeGames.put(gameInstanceId, game);
        } catch (Exception e) {
            for (Participant participant : newParticipants) {
                onParticipantReturnToHub(participant);
            }
            for (Player admin : newAdmins) {
                onAdminReturnToHub(admin);
            }
            Main.logger().log(Level.SEVERE, "Could not start game", e);
            return CommandResult.failure("Could not start game, see console for details");
        }
        return CommandResult.success(Component.empty()
                .append(Component.text("Started "))
                .append(title)
        );
    }
    
    /**
     * @param gameType the game type to check for an editor of
     * @return true if an editor for the given game type exists, false otherwise
     */
    protected boolean editorExists(@NotNull GameType gameType) {
        return switch (gameType) {
            case PARKOUR_PATHWAY,
                 FOOT_RACE -> true;
            default -> false;
        };
    }
    
    public boolean editorIsRunning() {
        return context.getActiveEditor() != null;
    }
    
    /**
     * @param gameType the game type to get the {@link GameEditor} for
     * @return the {@link GameEditor} associated with the given type, or null if there is no editor for the
     * given type (or if the type is null).
     */
    protected @Nullable GameEditor instantiateEditor(
            @NotNull GameType gameType,
            String configFile,
            Collection<Player> newAdmins
    ) {
        return switch (gameType) {
            case PARKOUR_PATHWAY -> {
                ParkourPathwayConfig config = new ParkourPathwayConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ParkourPathwayEditor(
                        plugin,
                        context,
                        config,
                        configFile,
                        newAdmins
                );
            }
            case FOOT_RACE -> {
                FootRaceConfig config = new FootRaceConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new FootRaceEditor(
                        plugin,
                        context,
                        config,
                        configFile,
                        newAdmins
                );
            }
            default -> null;
        };
    }
    
    protected @NotNull Component createNewTitle(String baseTitle) {
        return Component.empty()
                .append(Component.text(baseTitle))
                .color(NamedTextColor.BLUE);
    }
    // game end
    
    // editor start
    public CommandResult stopEditor() {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running"));
        }
        context.getActiveEditor().stop();
        context.setActiveEditor(null);
        return CommandResult.success();
    }
    
    /**
     * Called by an active editor when the editor is ended
     * @param editorAdmins the admins who were in the editor when it ended
     */
    public void editorIsOver(@NotNull Collection<Player> editorAdmins) {
        for (Player admin : editorAdmins) {
            onAdminReturnToHub(admin);
        }
    }
    
    public CommandResult validateEditor(@NotNull String configFile) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        return context.getActiveEditor().configIsValid(configFile);
    }
    
    public CommandResult saveEditor(@NotNull String configFile, boolean skipValidation) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        return context.getActiveEditor().saveConfig(configFile, skipValidation);
    }
    
    public CommandResult loadEditor(@NotNull String configFile) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running"));
        }
        return context.getActiveEditor().loadConfig(configFile);
    }
    // editor end
    
    // commands start
    public CommandResult top(@NotNull MCTParticipant participant) {
        GameInstanceId id = participantGames.get(participant.getUniqueId());
        if (id == null) {
            return CommandResult.failure("Can't use /top at this time");
        }
        MCTGame game = activeGames.get(id);
        return game.top(participant.getUniqueId());
    }
    // commands end
    
    // event start
    public CompletableFuture<CommandResult> startEvent(@NotNull EventInfo eventInfo, int maxGames, int currentGameNumber) {
        return CommandResult.failure("Can't start an event in this mode").asFuture();
    }
    
    public @NotNull CompletableFuture<CommandResult> stopEvent() {
        return CommandResult.failure("No event is running").asFuture();
    }
    
    public boolean eventIsActive() {
        return false;
    }
    
    
    public CommandResult readyUpParticipant(@NotNull MCTParticipant participant) {
        return CommandResult.failure("Can't ready up at this time");
    }
    
    public CommandResult unReadyParticipant(@NotNull MCTParticipant participant) {
        return CommandResult.failure("Can't un-ready at this time");
    }
    
    public CommandResult openHubMenu(@NotNull MCTParticipant participant) {
        return CommandResult.failure("Can't open the hub menu at this time");
    }
    
    public CommandResult undoGame(int gameSessionId) {
        return CommandResult.failure("Can't undo games in this state");
    }
    
    public CommandResult redoGame(int gameSessionId) {
        return CommandResult.failure("Can't redo games in this state");
    }
    
    public CompletableFuture<CommandResult> modifyMaxGames(int newMaxGames) {
        return CommandResult.failure("No event is active").asFuture();
    }
    
    public CommandResult whitelist(boolean whitelist) {
        return CommandResult.failure("No event is active");
    }
    
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
        return CommandResult.failure("No event is active");
    }
    
    public CommandResult removeGameFromVotingPool(@NotNull GameType gameToRemove) {
        return CommandResult.failure("No event is active");
    }
    
    public CommandResult listReady(@Nullable String teamId) {
        return CommandResult.failure("No event is active");
    }
    
    public void setWinner(@NotNull MCTTeam team) {
        // do nothing
    }
    // event stop
    
    // team start
    
    /**
     * Add a team to the game.
     * @param teamId The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     * @return the newly created team, or null if the given team already exists or could not be created
     */
    public CompletableFuture<Team> addTeam(String teamId, String teamDisplayName, String colorString) {
        if (teams.containsKey(teamId)) {
            return null;
        }
        return gameStateStorageUtil.addTeam(teamId, teamDisplayName, colorString)
                .thenApplyAsync(score -> {
                    NamedTextColor color = ColorMap.getNamedTextColor(colorString);
                    ColorAttributes colorAttributes = ColorMap.getColorAttributes(colorString);
                    MCTTeam team = new MCTTeam(teamId, teamDisplayName, color, colorAttributes, score);
                    teams.put(teamId, team);
                    
                    org.bukkit.scoreboard.Team newTeam = mctScoreboard.registerNewTeam(teamId);
                    newTeam.displayName(Component.text(teamDisplayName));
                    newTeam.color(color);
                    tabList.addTeam(teamId, teamDisplayName, color);
                    updateScoreVisuals(Collections.singletonList(team), Collections.emptyList());
                    return (Team) team;
                }, mainThreadExecutor)
                .exceptionally(e -> {
                    // this is unlikely to happen as a result of a sql error, some other error happened
                    Main.logger().log(Level.SEVERE, "unable to persist team", e);
                    return null;
                })
                ;
    }
    
    /**
     * Remove the given team from the game
     * @param teamId teamId of the team to remove
     */
    public CompletableFuture<CommandResult> removeTeam(String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return CommandResult.failure(Component.text("Team ")
                    .append(Component.text(teamId))
                    .append(Component.text(" does not exist."))
            ).asFuture();
        }
        
        // leave all participants on team start
        Set<OfflineParticipant> members = team.getMemberUUIDs().stream()
                .map(allParticipants::get)
                .collect(Collectors.toSet());
        Set<MCTParticipant> onlineMembers = team.getMemberUUIDs().stream()
                .map(onlineParticipants::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (MCTParticipant participant : onlineMembers) {
            chain = chain.thenCompose(ignore -> onParticipantQuit(participant));
            onlineParticipants.remove(participant.getUniqueId());
        }
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(teamId);
        if (scoreboardTeam == null) {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (removeTeam)", teamId));
        }
        List<CommandResult> results = new ArrayList<>();
        for (OfflineParticipant participant : members) {
            team.leaveMember(participant.getUniqueId());
            allParticipants.remove(participant.getUniqueId());
            tabList.leaveParticipant(participant.getParticipantID());
            results.add(CommandResult.success(Component.text("Removed ")
                    .append(participant.displayName())
                    .append(Component.text(" from team "))
                    .append(team.getFormattedDisplayName())));
            if (scoreboardTeam != null) {
                if (participant.getPlayer() != null) {
                    scoreboardTeam.removePlayer(participant.getPlayer());
                }
            }
        }
        context.updateLeaderboards();
        Audience.audience(onlineMembers).sendMessage(Component.text("You've been removed from ")
                .append(team.getFormattedDisplayName()));
        
        // leave all participants on team end
        
        teams.remove(team.getTeamId());
        tabList.removeTeam(teamId);
        updateSidebarTeamScores();
        if (scoreboardTeam != null) {
            scoreboardTeam.unregister();
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (removeTeam)", teamId));
        }
        
        return chain
                .thenCompose(v -> gameStateStorageUtil.removeTeam(teamId))
                .thenApply(v -> {
                    results.add(CommandResult.success(Component.text("Removed team ")
                            .append(team.getFormattedDisplayName())));
                    return CompositeCommandResult.all(results);
                })
                .exceptionally(e -> {
                    context.reportGameStateException("removing team", e);
                    results.add(CommandResult.failure(Component.text("error occurred removing team, see console for details.")));
                    return CompositeCommandResult.all(results);
                });
        
    }
    // team stop
    
    // participant start
    
    private void validateUUIDAndIGN(@NotNull UUID uuid, @NotNull String ign) throws IllegalArgumentException {
        /*
        - check if the player's UUID is in the game state already
            - if their UUID IS in the game state
                - check to see if their IGN matches the given one
                    - if the IGN does not match, return an error (IGN does not match UUID)
                    - if the IGN matches, continue join operation
            - if their UUID is NOT in the game state
                - check to see if the given IGN is in the game state
                    - if the IGN is in the game state
                        - then the UUID input is wrong, throw an error (UUID does not match IGN)
                    - if the IGN is NOT in the game state
                        - then it's a new player with a new IGN and UUID, continue join operation
         */
        
    }
    
    /**
     * Joins the given player to the given team.
     * If the player was on a team already they
     * will be removed from that team and added to the other team.<br>
     * This is the variant for offline participants. If the player is online, you should use
     * {@link #joinOnlineParticipantToTeam(Player, MCTTeam)}
     * @param uuid The uuid of the participant to join to the given team
     * @param ign The ign of the participant to join to the given team
     * @param team The internal teamId of the team to join the player to.
     */
    public CompletableFuture<CommandResult> joinOfflineParticipantToTeam(@NotNull UUID uuid, @NotNull String ign, @NotNull MCTTeam team) {
        // warn about UUID and IGN mismatches
        OfflineParticipant existingParticipant = allParticipants.get(uuid);
        if (existingParticipant != null) {
            // the UUID is in the game state
            if (!existingParticipant.getName().equals(ign)) {
                // the name doesn't match
                return CommandResult.failure(Component.empty()
                        .append(Component.text("The given IGN does not match the UUID in the game state"))
                ).asFuture();
            }
            // the name matches, proceed
        } else {
            // the UUID is not in the game state
            OfflineParticipant offlineParticipantWithIGN = context.getOfflineParticipant(ign);
            if (offlineParticipantWithIGN != null) {
                // someone with that IGN but a different UUID exists
                return CommandResult.failure(Component.empty()
                        .append(Component.text("The given UUID does not match the IGN in the game state"))
                ).asFuture();
            }
        }
        return joinParticipantToTeam(
                uuid,
                ign,
                team,
                existingParticipant,
                null
        );
        
    }
    
    /**
     * Joins the given player to the given team.
     * If the player was on a team already they
     * will be removed from that team and added to the other team.<br>
     * This is the variant for online participants. If the player is not online, you should use
     * {@link #joinOfflineParticipantToTeam(UUID, String, MCTTeam)}
     * @param player The player to join to the given team
     * @param team The internal teamId of the team to join the player to.
     */
    public CompletableFuture<CommandResult> joinOnlineParticipantToTeam(@NotNull Player player, @NotNull MCTTeam team) {
        UUID uuid = player.getUniqueId();
        OfflineParticipant existingParticipant = allParticipants.get(uuid);
        return joinParticipantToTeam(
                uuid,
                player.getName(),
                team,
                existingParticipant,
                player
        );
    }
    
    /**
     * The shared functionality of both online and offline player join to team versions<br>
     * {@link #joinOfflineParticipantToTeam(UUID, String, MCTTeam)}<br>
     * {@link #joinOnlineParticipantToTeam(Player, MCTTeam)}<br>
     * Joins the given player to the given team
     * @param uuid the uuid of the player
     * @param ign the ign of the player
     * @param team the team to join them to
     * @param existingParticipant the existing participant, if one exists
     * @param player the player to join, if they are online
     * @return a future containing the join operation and database operations with appropriate threads
     */
    protected CompletableFuture<CommandResult> joinParticipantToTeam(
            @NotNull UUID uuid,
            @NotNull String ign,
            @NotNull MCTTeam team,
            @Nullable OfflineParticipant existingParticipant,
            @Nullable Player player
    ) {
        CompletableFuture<CommandResult> chain = CompletableFuture.completedFuture(CommandResult.success());
        if (context.isAdmin(uuid)) {
            chain = chain.thenComposeAsync(compositeResult -> {
                CompletableFuture<CommandResult> joinFuture = removeAdmin(uuid, ign);
                return joinFuture.thenApply(v -> compositeResult);
            }, mainThreadExecutor);
        }
        if (existingParticipant != null) {
            if (existingParticipant.isOnTeam(team)) {
                return chain.thenApply(result -> result
                        .and(CommandResult.success(Component.empty()
                                .append(existingParticipant.displayName())
                                .append(Component.text(" is already a member of "))
                                .append(team.getFormattedDisplayName())
                                .append(Component.text(". Nothing happened.")))
                        )
                );
            }
            chain = chain.thenComposeAsync(compositeResult -> {
                CompletableFuture<CommandResult> joinFuture = leaveParticipant(existingParticipant);
                return joinFuture.thenApply(compositeResult::and);
            }, mainThreadExecutor);
        }
        
        return chain.thenComposeAsync(compositeResult -> {
            CompletableFuture<OfflineParticipant> subChain = gameStateStorageUtil.joinPlayer(uuid, ign, team.getTeamId())
                    .thenApplyAsync(score -> {
                        Component participantDisplayName = GameManagerUtils.createDisplayName(ign, team.getColor());
                        OfflineParticipant offlineParticipant = new OfflineParticipant(uuid, ign, participantDisplayName, team.getTeamId(), score);
                        allParticipants.put(offlineParticipant.getUniqueId(), offlineParticipant);
                        team.joinMember(offlineParticipant.getUniqueId());
                        tabList.joinParticipant(
                                offlineParticipant.getParticipantID(),
                                offlineParticipant.getName(),
                                offlineParticipant.getTeamId(),
                                true);
                        
                        context.updateLeaderboards();
                        return offlineParticipant;
                    }, mainThreadExecutor);
            if (player != null) {
                subChain = subChain.thenComposeAsync(offlineParticipant -> {
                    MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
                    participant.sendMessage(Component.text("You've been joined to team ")
                            .append(team.getFormattedDisplayName()));
                    return onParticipantJoin(participant)
                            .thenApply(v -> offlineParticipant);
                }, mainThreadExecutor);
            }
            return subChain.thenApply(offlineParticipant -> compositeResult.and(CommandResult.success(Component.text("Joined ")
                            .append(offlineParticipant.displayName())
                            .append(Component.text(" to "))
                            .append(team.getFormattedDisplayName()))))
                    .exceptionally(e -> CommandResult.throwable("add a new player", e))
                    ;
        }, mainThreadExecutor);
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well.
     * @param offlineParticipant The participant to remove from their team
     */
    public CompletableFuture<CommandResult> leaveParticipant(@NotNull OfflineParticipant offlineParticipant) {
        MCTTeam team = teams.get(offlineParticipant.getTeamId());
        MCTParticipant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        if (participant != null) {
            chain = chain
                    .thenCompose(v -> onParticipantQuit(participant))
                    .thenRunAsync(() -> {
                        participant.sendMessage(Component.text("You've been removed from ")
                                .append(team.getFormattedDisplayName()));
                        onlineParticipants.remove(participant.getUniqueId()); // TODO: is this redundant?
                    }, mainThreadExecutor);
        }
        return chain.thenRunAsync(() -> {
                    team.leaveMember(offlineParticipant.getUniqueId());
                    allParticipants.remove(offlineParticipant.getUniqueId());
                }, mainThreadExecutor)
                .thenCompose(v -> gameStateStorageUtil.leavePlayer(offlineParticipant.getUniqueId()))
                .thenApplyAsync(v -> {
                    context.updateLeaderboards();
                    tabList.leaveParticipant(offlineParticipant.getParticipantID());
                    org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(offlineParticipant.getTeamId());
                    if (scoreboardTeam != null) {
                        if (offlineParticipant.getPlayer() != null) {
                            scoreboardTeam.removePlayer(offlineParticipant.getPlayer());
                        }
                    } else {
                        Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (leaveParticipant)", offlineParticipant.getTeamId()));
                    }
                    return CommandResult.success(Component.text("Removed ")
                            .append(offlineParticipant.displayName())
                            .append(Component.text(" from team "))
                            .append(team.getFormattedDisplayName()));
                }, mainThreadExecutor)
                .exceptionally(e -> CommandResult.throwable("leave player", e));
    }
    
    public CompletableFuture<CommandResult> joinParticipantToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return CommandResult.failure(Component.text("Already in a game")).asFuture();
        }
        GameInstanceId id;
        if (configFile == null) {
            List<GameInstanceId> activeIds = new ArrayList<>();
            for (GameInstanceId gameInstanceId : activeGames.keySet()) {
                if (gameInstanceId.getGameType().equals(gameType)) {
                    activeIds.add(gameInstanceId);
                }
            }
            if (activeIds.size() == 1) {
                id = activeIds.getFirst();
            } else if (activeIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" game is active right now"))
                ).asFuture();
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("More than one instance of "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" is active. Please specify a config."))
                ).asFuture();
            }
        } else {
            id = new GameInstanceId(gameType, configFile);
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No "))
                    .append(Component.text(id.getTitle()))
                    .append(Component.text(" game is active right now"))
            ).asFuture();
        }
        @Nullable GameInstanceId teamGameId = context.getTeamActiveGame(participant.getTeamId());
        MCTTeam team = teams.get(participant.getTeamId());
        if (teamGameId != null && !teamGameId.equals(id)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Can't join "))
                    .append(Component.text(id.getTitle()))
                    .append(Component.text(" because "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is in "))
                    .append(Component.text(teamGameId.getTitle()))
            ).asFuture();
        }
        onParticipantJoinGame(id, participant);
        return activeGame.onJoin(team, participant)
                .thenApply(v -> CommandResult.success(Component.empty()
                        .append(Component.text("Joining "))
                        .append(Component.text(id.getTitle()))))
                ;
    }
    
    public @Nullable List<String> tabCompleteActiveGames(@NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchTabList(
                    activeGames.keySet().stream()
                            .map(id -> id.getGameType().getId())
                            .toList(),
                    args[0]);
        }
        if (args.length == 2) {
            return CommandUtils.partialMatchTabList(
                    activeGames.keySet().stream()
                            .map(GameInstanceId::getConfigFile)
                            .toList(),
                    args[1]);
        }
        
        return Collections.emptyList();
    }
    
    public CommandResult joinAdminToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull Player admin) {
        if (isAdminInGame(admin)) {
            return CommandResult.failure(Component.text("Already in a game"));
        }
        GameInstanceId id;
        if (configFile == null) {
            List<GameInstanceId> activeIds = new ArrayList<>();
            for (GameInstanceId gameInstanceId : activeGames.keySet()) {
                if (gameInstanceId.getGameType().equals(gameType)) {
                    activeIds.add(gameInstanceId);
                }
            }
            if (activeIds.size() == 1) {
                id = activeIds.getFirst();
            } else if (activeIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" game is active right now")));
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("More than one instance of "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" is active. Please specify a config.")));
            }
        } else {
            id = new GameInstanceId(gameType, configFile);
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No "))
                    .append(Component.text(id.getTitle()))
                    .append(Component.text(" game is active right now")));
        }
        onAdminJoinGame(id, admin);
        activeGame.onAdminJoin(admin);
        return CommandResult.success(Component.empty()
                .append(Component.text("Joining "))
                .append(Component.text(id.getTitle())));
    }
    
    public CompletableFuture<CommandResult> returnParticipantToHub(@NotNull MCTParticipant participant) {
        return returnParticipantToHub(participant, config.getSpawn());
    }
    
    protected CompletableFuture<CommandResult> returnParticipantToHub(@NotNull MCTParticipant participant, @NotNull Location spawn) {
        GameInstanceId id = participantGames.remove(participant.getUniqueId());
        if (id == null) {
            participant.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub")).asFuture();
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            // this should not happen
            participant.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub")).asFuture();
        }
        CompletableFuture<Void> quitFuture = activeGame.onQuit(participant.getTeamId(), participant.getUniqueId());
        onParticipantReturnToHub(participant);
        participant.teleport(spawn);
        return quitFuture.thenApply(v ->
                CommandResult.success(Component.text("Quitting current game. Returning to hub.")));
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin) {
        return returnAdminToHub(admin, config.getSpawn());
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin, @NotNull Location spawn) {
        GameInstanceId id = adminGames.remove(admin.getUniqueId());
        if (id == null) {
            admin.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        MCTGame activeGame = activeGames.get(id);
        if (activeGame == null) {
            // this should not happen
            admin.teleport(spawn);
            return CommandResult.success(Component.text("Returning to hub"));
        }
        activeGame.onAdminQuit(admin);
        onAdminReturnToHub(admin);
        admin.teleport(spawn);
        return CommandResult.success(Component.text("Quitting current game. Returning to hub."));
    }
    // participant stop
    
    // admin start
    
    /**
     * Adds the given player as an admin. If the player is already an admin, nothing happens. If the player is a
     * participant, they are removed from their team and added as an admin.
     * @param newAdmin The player to add
     */
    public CompletableFuture<CommandResult> addAdmin(Player newAdmin) {
        UUID uniqueId = newAdmin.getUniqueId();
        if (gameStateStorageUtil.isAdmin(uniqueId)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(newAdmin.getName())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is already an admin. Nothing happened."))
            ).asFuture();
        }
        CompletableFuture<CommandResult> chain = CompletableFuture.completedFuture(CommandResult.success());
        OfflineParticipant offlineParticipant = allParticipants.get(uniqueId);
        if (offlineParticipant != null) {
            chain = chain.thenComposeAsync(compositeResult -> {
                CompletableFuture<CommandResult> joinFuture = leaveParticipant(offlineParticipant);
                return joinFuture.thenApply(compositeResult::and);
            }, mainThreadExecutor);
        }
        return chain.thenApplyAsync(compositeResult -> {
            try {
                gameStateStorageUtil.addAdmin(uniqueId);
            } catch (ConfigIOException | SQLException e) {
                context.reportGameStateException("adding new admin", e);
                return compositeResult.and(CommandResult.failure(
                        Component.text("error occurred adding new admin, see console for details.")));
            }
            
            org.bukkit.scoreboard.Team adminTeam = mctScoreboard.getTeam(GameManager.ADMIN_TEAM);
            if (adminTeam != null) {
                adminTeam.addPlayer(newAdmin);
            } else {
                Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", GameManager.ADMIN_TEAM));
                ;
            }
            if (newAdmin.isOnline()) {
                newAdmin.sendMessage(Component.text("You were added as an admin"));
                onAdminJoin(newAdmin);
            }
            
            return compositeResult.and(CommandResult.success(Component.empty()
                    .append(Component.text("Added "))
                    .append(Component.text(newAdmin.getName())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" as an admin"))));
        }, mainThreadExecutor);
    }
    
    public @NotNull CompletableFuture<CommandResult> removeAdmin(@NotNull UUID uuid, @NotNull String ign) {
        // TODO: there are two removeAdmin methods, use only one
        if (!context.isAdmin(uuid)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an admin. Nothing happened."))
            ).asFuture();
        }
        context.getOnlineAdmins().stream()
                .filter(admin -> admin.getUniqueId().equals(uuid))
                .findFirst().ifPresent(this::onAdminQuit);
        return gameStateStorageUtil.removeAdmin(uuid)
                .thenApplyAsync(v -> CommandResult.success(Component.empty()
                        .append(Component.text(ign)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is no longer an admin"))), mainThreadExecutor)
                .exceptionally(e -> CommandResult.throwable("remove an admin", e))
                ;
        
        // TODO: remove them from the scoreboard in a different context
        
    }
    
    /**
     * Removes the given player from the admins
     * @param offlineAdmin The admin to remove
     */
    public @NotNull CompletableFuture<CommandResult> removeAdmin(@NotNull OfflinePlayer offlineAdmin, @NotNull String adminName) {
        // TODO: there are two removeAdmin methods, use only one
        if (!context.isAdmin(offlineAdmin.getUniqueId())) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(adminName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an admin. Nothing happened."))
            ).asFuture();
        }
        if (offlineAdmin.isOnline()) {
            Player onlineAdmin = offlineAdmin.getPlayer();
            if (onlineAdmin != null) {
                onlineAdmin.sendMessage(Component.text("You were removed as an admin"));
                onAdminQuit(onlineAdmin);
            }
        }
        UUID adminUniqueId = offlineAdmin.getUniqueId();
        return gameStateStorageUtil.removeAdmin(adminUniqueId)
                .thenApplyAsync(v -> {
                    org.bukkit.scoreboard.Team adminTeam = mctScoreboard.getTeam(GameManager.ADMIN_TEAM);
                    if (adminTeam != null) {
                        adminTeam.removePlayer(offlineAdmin);
                    } else {
                        Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", GameManager.ADMIN_TEAM));
                    }
                    
                    return CommandResult.success(Component.empty()
                            .append(Component.text(adminName)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is no longer an admin")));
                }, mainThreadExecutor)
                .exceptionally(e -> CommandResult.throwable("remove an admin", e));
    }
    
    public void onAdminDamage(@NotNull EntityDamageEvent event, @NotNull Player admin) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "GameManagerState.onAdminDamage()->admin");
        event.setCancelled(true);
    }
    // admin stop
    
    // event handlers start
    public void onParticipantDeath(PlayerDeathEvent event, MCTParticipant participant) {
        GameManagerUtils.replaceWithDisplayName(event, participant);
        if (participant.getKiller() != null) {
            MCTParticipant killer = onlineParticipants.get(participant.getKiller().getUniqueId());
            if (killer != null) {
                GameManagerUtils.replaceWithDisplayName(event, killer);
            }
        }
        GameManagerUtils.deColorLeatherArmor(event.getDrops());
    }
    
    public boolean isParticipantInGame(Participant participant) {
        return isParticipantInGame(participant.getUniqueId());
    }
    
    public boolean isParticipantInGame(UUID uuid) {
        return participantGames.containsKey(uuid);
    }
    
    public boolean isAdminInGame(Player admin) {
        return isAdminInGame(admin.getUniqueId());
    }
    
    public boolean isAdminInGame(UUID uuid) {
        return adminGames.containsKey(uuid);
    }
    
    public void onParticipantRespawn(PlayerRespawnEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        event.setRespawnLocation(config.getSpawn());
    }
    
    public void onParticipantDamage(@NotNull EntityDamageEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "GameManagerState.onParticipantDamage()->participant is in hub cancelled");
        event.setCancelled(true);
    }
    
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            Material blockType = clickedBlock.getType();
            if (config.getPreventInteractions().contains(blockType)) {
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }
    
    public void onParticipantInventoryClick(@NotNull InventoryClickEvent event, MCTParticipant participant) {
        // do nothing
    }
    
    public void onParticipantDropItem(@NotNull PlayerDropItemEvent event, MCTParticipant participant) {
        // do nothing
    }
    
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    public void onParticipantMove(@NotNull PlayerMoveEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        Location location = participant.getLocation();
        if (location.getY() < config.getYLimit()) {
            participant.teleport(config.getSpawn());
            participant.sendMessage("You fell out of the hub boundary");
        }
    }
    
    public double getMultiplier() {
        return 1.0;
    }
    
    public List<GameType> getVotingPool() {
        return Collections.emptyList();
    }
    // event handlers stop
}
