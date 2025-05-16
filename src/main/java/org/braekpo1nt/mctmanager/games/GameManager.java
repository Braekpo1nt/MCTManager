package org.braekpo1nt.mctmanager.games;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.GameManagerState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.MaintenanceState;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.participant.ColorAttributes;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager implements Listener {
    
    @Setter
    public @NotNull GameManagerState state;
    public static final String ADMIN_TEAM = "_Admins";
    public static final NamedTextColor ADMIN_COLOR = NamedTextColor.DARK_RED;
    private final Main plugin;
    private final Map<GameType, MCTGame> activeGames = new HashMap<>();
    private GameEditor activeEditor = null;
    @Getter
    private final SidebarFactory sidebarFactory;
    private final GameStateStorageUtil gameStateStorageUtil;
    /**
     * Scoreboard for holding the teams. This private scoreboard can't be
     * modified using the normal /team command, and thus can't be unsynced
     * with the game state.
     */
    private final Scoreboard mctScoreboard;
    /**
     * This should be used to register all timers in games and events, 
     * so that they can be easily paused, resumed, skipped, etc. in bulk.
     */
    @Getter
    private final TimerManager timerManager;
    /**
     * Contains the list of all teams. Updated when teams are added/removed or when participants are 
     * joined/left or quit/join
     */
    private final Map<String, MCTTeam> teams = new HashMap<>();
    /**
     * Contains the list of all participants, offline and online.
     */
    private final Map<UUID, OfflineParticipant> allParticipants = new HashMap<>();
    /**
     * Contains the list of online participants. Updated when participants are added/removed or quit/join
     */
    private final Map<UUID, MCTParticipant> onlineParticipants = new HashMap<>();
    @Getter
    private final List<Player> onlineAdmins = new ArrayList<>();
    /**
     * A reference to which participant is in which game
     */
    protected final Map<UUID, GameType> participantGames = new HashMap<>();
    private final TabList tabList;
    private final @NotNull List<LeaderboardManager> leaderboardManagers;
    private final Sidebar sidebar; // TODO: make sidebar a thing of each state, not central
    private final @NotNull HubConfig config;
    
    public GameManager(Main plugin, 
                       Scoreboard mctScoreboard, 
                       @NotNull GameStateStorageUtil gameStateStorageUtil,
                       @NotNull SidebarFactory sidebarFactory,
                       @NotNull HubConfig config) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = gameStateStorageUtil;
        this.timerManager = new TimerManager(plugin);
        this.sidebarFactory = sidebarFactory;
        this.config = config;
        this.tabList = new TabList(plugin);
        this.leaderboardManagers = createLeaderboardManagers();
        this.sidebar = sidebarFactory.createSidebar();
        this.state = new MaintenanceState(this, ContextReference.builder()
                .tabList(this.tabList)
                .mctScoreboard(this.mctScoreboard)
                .activeGames(this.activeGames)
                .teams(this.teams)
                .allParticipants(this.allParticipants)
                .onlineParticipants(this.onlineParticipants)
                .onlineAdmins(this.onlineAdmins)
                .participantGames(this.participantGames)
                .plugin(this.plugin)
                .config(this.config)
                .gameStateStorageUtil(this.gameStateStorageUtil)
                .sidebarFactory(this.sidebarFactory)
                .sidebar(this.sidebar)
                .leaderboardManagers(this.leaderboardManagers)
                .build());
    }
    
    public CommandResult switchMode(@NotNull String mode) {
        return state.switchMode(mode);
    }
    
    public List<LeaderboardManager> createLeaderboardManagers() {
        List<LeaderboardManager> results = new ArrayList<>(config.getLeaderboards().size());
        for (HubConfig.Leaderboard leaderboard : config.getLeaderboards()) {
            LeaderboardManager leaderboardManager = new LeaderboardManager(
                    this,
                    leaderboard.getTitle(),
                    leaderboard.getLocation(),
                    leaderboard.getTopPlayers()
            );
            leaderboardManager.updateScores();
            results.add(leaderboardManager);
        }
        return results;
    }
    
    public void updateLeaderboards() {
        this.leaderboardManagers.forEach(LeaderboardManager::updateScores);
    }
    
    public void cleanup() {
        state.cleanup();
    }
    
    @SuppressWarnings("unused")
    private void printStateToConsole() {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("====teams (")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(teams.size()))
                .append(Component.text("):"))
                .append(Component.newline());
        for (Map.Entry<String, MCTTeam> entry : teams.entrySet()) {
            MCTTeam team = entry.getValue();
            builder.append(Component.text("--"))
                    .append(Component.text(entry.getKey()))
                    .append(Component.text(" ("))
                    .append(Component.text(team.getMemberUUIDs().size()))
                    .append(Component.text("):\n"));
            for (MCTParticipant member : team.getOnlineMembers()) {
                builder.append(Component.text("----"))
                        .append(member.displayName())
                        .append(Component.newline());
            }
        }
        builder.append(Component.text("====allParticipants (")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(allParticipants.size()))
                .append(Component.text("):"))
                .append(Component.newline());
        for (OfflineParticipant p : allParticipants.values()) {
            builder.append(Component.text("--"))
                    .append(p.displayName())
                    .append(Component.newline());
        }
        builder.append(Component.text("====onlineParticipants (")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(onlineParticipants.size()))
                .append(Component.text("):"))
                .append(Component.newline());
        for (OfflineParticipant p : onlineParticipants.values()) {
            builder.append(Component.text("--"))
                    .append(p.displayName())
                    .append(Component.newline());
        }
        plugin.getServer().getConsoleSender().sendMessage(builder.build());
    }
    
    /**
     * @param gameType the game type to get the {@link GameEditor} for
     * @return the {@link GameEditor} associated with the given type, or null if there is no editor for the
     * given type (or if the type is null).
     */
    private @Nullable GameEditor instantiateEditor(GameType gameType) {
        return switch (gameType) {
            case PARKOUR_PATHWAY -> new ParkourPathwayEditor(plugin, this);
            case FOOT_RACE -> new FootRaceEditor(plugin, this);
            default -> null;
        };
    }
    
    @EventHandler(priority = EventPriority.LOWEST) // happens first
    public void onPlayerDeath(PlayerDeathEvent event) {
        MCTParticipant participant = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantDeath(event, participant);
    }
    
    @EventHandler
    public void onPlayerChangeArmor(PlayerArmorChangeEvent event) {
        Participant participant = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        ItemStack newItem = event.getNewItem();
        if (GameManagerUtils.isLeatherArmor(newItem)) {
            GameManagerUtils.colorLeatherArmor(newItem, teams.get(participant.getTeamId()).getBukkitColor());
            EquipmentSlot equipmentSlot = GameManagerUtils.toEquipmentSlot(event.getSlotType());
            if (equipmentSlot == null) {
                return;
            }
            participant.getEquipment().setItem(equipmentSlot, newItem);
        }
        ItemStack oldItem = event.getOldItem();
        if (GameManagerUtils.isLeatherArmor(oldItem)) {
            GameManagerUtils.deColorLeatherArmor(oldItem);
            ItemStack cursor = participant.getOpenInventory().getCursor();
            ItemStack mainHand = participant.getInventory().getItemInMainHand();
            ItemStack offHand = participant.getInventory().getItemInOffHand();
            if (GameManagerUtils.isLeatherArmor(cursor)) {
                GameManagerUtils.deColorLeatherArmor(cursor);
            }
            if (GameManagerUtils.isLeatherArmor(mainHand)) {
                GameManagerUtils.deColorLeatherArmor(mainHand);
            }
            if (GameManagerUtils.isLeatherArmor(offHand)) {
                GameManagerUtils.deColorLeatherArmor(offHand);
            }
            GameManagerUtils.deColorLeatherArmor(Arrays.asList(participant.getInventory().getStorageContents()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        MCTParticipant participant = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (GameManagerUtils.isLeatherArmor(itemStack)) {
            GameManagerUtils.deColorLeatherArmor(itemStack);
        }
        state.onParticipantDropItem(event, participant);
    }
    
    // TODO: handle block dispensing items
//    @EventHandler
//    public void onBlockDispenseItem(BlockDispenseArmorEvent event) {
//    }
    
    @EventHandler
    public void onParticipantInteract(PlayerInteractEvent event) {
        MCTParticipant participant = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantInteract(event, participant);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            state.onAdminQuit(event, player);
            return;
        }
        MCTParticipant participant = onlineParticipants.get(player.getUniqueId());
        if (participant != null) {
            state.onParticipantQuit(event, participant);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            state.onAdminJoin(event, player);
            return;
        }
        OfflineParticipant offlineParticipant = allParticipants.get(player.getUniqueId());
        if (offlineParticipant != null) {
            MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
            state.onParticipantJoin(event, participant);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_DAMAGE_CAUSES.contains(event.getCause())) {
            return;
        }
        MCTParticipant participant = onlineParticipants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantDamage(event, participant);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        MCTParticipant participant = onlineParticipants.get(event.getWhoClicked().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantInventoryClick(event, participant);
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        MCTParticipant participant = onlineParticipants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantFoodLevelChange(event, participant);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        MCTParticipant participant = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantMove(event, participant);
    }
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
    }
    
    public CommandResult joinParticipantToGame(@NotNull GameType gameType, @NotNull UUID uuid) {
        MCTParticipant mctParticipant = onlineParticipants.get(uuid);
        if (mctParticipant == null) {
            return CommandResult.failure(Component.text("You are not a participant"));
        }
        return state.joinParticipantToGame(gameType, mctParticipant);
    }
    
    public CommandResult returnParticipantToHub(@NotNull UUID uuid) {
        MCTParticipant mctParticipant = onlineParticipants.get(uuid);
        if (mctParticipant == null) {
            return CommandResult.failure(Component.text("You are not a participant"));
        }
        return state.returnParticipantToHub(mctParticipant);
    }
    
    /**
     * @return a new sidebar
     */
    public Sidebar createSidebar() {
        return state.createSidebar();
    }
    
    public boolean loadGameState() {
        try {
            gameStateStorageUtil.loadGameState();
        } catch (ConfigException e) {
            reportGameStateException("loading game state", e);
            return false;
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
        for (Player player : onlinePlayers.values()) {
            if (isAdmin(player.getUniqueId())) {
                state.onAdminJoin(player);
            }
            OfflineParticipant offlineParticipant = allParticipants.get(player.getUniqueId());
            if (offlineParticipant != null) {
                MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
                state.onParticipantJoin(participant);
            }
        }
        
        // sidebar start
        state.updateSidebarTeamScores();
        state.updateSidebarPersonalScores(onlineParticipants.values());
        // sidebar stop
        return true;
    }
    
    public void saveGameState() {
        try {
            gameStateStorageUtil.saveGameState();
        } catch (ConfigException e) {
            reportGameStateException("adding score to player", e);
        }
    }
    
    public boolean eventIsActive() {
        return state.eventIsActive();
    }
    
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull GameType gameType, @NotNull String configFile) {
        return state.startGame(teamIds, gameType, configFile);
    }
    
    public CommandResult startEvent(int maxGames, int currentGameNumber) {
        return state.startEvent(maxGames, currentGameNumber);
    }
    
    public CommandResult stopEvent() {
        return state.stopEvent();
    }
    
    /**
     * Starts the given game
     *
     * @param gameType   The game to start
     * @param configFile the config file to use for the game
     * @return a CommandResult indicating the success or failure of starting the game,
     * including a reason why the game didn't start if so.
     */
    public CommandResult startGame(@NotNull GameType gameType, @NotNull String configFile) {
        return state.startGame(teams.keySet(), gameType, configFile);
    }
    
    /**
     * @return all teams
     * @deprecated use context's getTeams() when we are in a state design pattern
     */
    @Deprecated
    public @NotNull Collection<Team> getTeams() {
        return teams.values().stream().map(mctTeam -> (Team) mctTeam).toList();
    }
    
    /**
     * @return the teams sorted from highest to lowest score, with alphabetical order
     * of the team display name as the tie-breaker
     */
    public @NotNull List<Team> getSortedTeams() {
        return teams.values().stream().map(t -> (Team) t).sorted((t1, t2) -> {
            int scoreComparison = t2.getScore() - t1.getScore();
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
        }).toList();
    }
    
    /**
     * @param participant the participant to get the team of
     * @return the team that the given participant belongs to. If the Participant is not in the GameManager,
     * the team may not exist
     */
    public Team getTeam(@NotNull Participant participant) {
        return getTeam(participant.getTeamId());
    }
    
    /**
     * @param teamId the teamId to get the {@link Team} for
     * @return the team with the given teamId, if one exists, null otherwise
     */
    public @Nullable Team getTeam(@NotNull String teamId) {
        return teams.get(teamId);
    }
    
    /**
     * @param teamId the teamId to check
     * @return true if the given teamId is in any active games
     */
    public boolean teamIsInGame(String teamId) {
        for (MCTGame game : activeGames.values()) {
            if (game.containsTeam(teamId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param teamId the team to get the game type of
     * @return the type of active game the given teamId is in,
     * or null if the team is not in a game
     */
    public @Nullable GameType getTeamActiveGame(String teamId) {
        for (MCTGame game : activeGames.values()) {
            if (game.containsTeam(teamId)) {
                return game.getType();
            }
        }
        return null;
    }
    
    public boolean teamIsOnline(String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return false;
        }
        return team.isOnline();
    }
    
    /**
     * If a game of the specified type is currently going on, manually stops the game.
     */
    public CommandResult stopGame(GameType gameType) {
        return state.stopGame(gameType);
    }
    
    /**
     * Stop all active games, if there are any
     */
    public CommandResult stopAllGames() {
        return state.stopAllGames();
    }
    
    /**
     * Called by an active game when the game is over.
     */
    public void gameIsOver(@NotNull GameType gameType, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants) {
        state.gameIsOver(gameType, teamScores, participantScores, gameParticipants);
    }
    
    /**
     * @param gameType the game type to check
     * @return true if the given game type is currently being played, false otherwise
     */
    public boolean gameIsActive(@NotNull GameType gameType) {
        return activeGames.containsKey(gameType);
    }
    
    public CommandResult startEditor(@NotNull GameType gameType, @NotNull String configFile) {
        if (!activeGames.isEmpty()) {
            return CommandResult.failure(Component.text("Can't start an editor while any games are active"));
        }
        
        if (onlineParticipants.isEmpty()) {
            return CommandResult.failure(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
        }
        
        if (editorIsRunning()) {
            return CommandResult.failure(Component.text("An editor is already running. You must stop it before you can start another one."));
        }
        
        GameEditor selectedEditor = instantiateEditor(gameType);
        if (selectedEditor == null) {
            return CommandResult.failure(Component.text("Can't find editor for game type " + gameType));
        }
        
        // make sure config loads
        try {
            selectedEditor.loadConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for editor %s", selectedEditor), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.text(gameType.name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        
        selectedEditor.start(Participant.toPlayersList(onlineParticipants.values()));
        activeEditor = selectedEditor;
        return CommandResult.success();
    }
    
    public CommandResult stopEditor() {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        activeEditor.stop();
        activeEditor = null;
        return CommandResult.success();
    }
    
    public boolean editorIsRunning() {
        return activeEditor != null;
    }
    
    /**
     * @return the type of the active editor, or null if no editor is active
     */
    public @Nullable GameType getEditorType() {
        if (activeEditor == null) {
            return null;
        }
        return activeEditor.getType();
    }
    
    public CommandResult validateEditor(@NotNull String configFile) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        try {
            activeEditor.configIsValid(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error validating config for editor %s", activeEditor.getType()), e);
            return CommandResult.failure(Component.text("Config is not valid for ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        return CommandResult.success(Component.text("Config is valid."));
    }
    
    /**
     * @param skipValidation if true, validation will be skipped and the config will be saved even if invalid, if false the config will only save if it is valid
     */
    public CommandResult saveEditor(@NotNull String configFile, boolean skipValidation) {
        if (!editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        if (!skipValidation) {
            try {
                activeEditor.configIsValid(configFile);
            } catch (ConfigException e) {
                Main.logger().log(Level.SEVERE, String.format("Error validating config for editor %s", activeEditor.getType()), e);
                return CommandResult.failure(
                        Component.text("Config is not valid for ")
                                .append(Component.text(activeEditor.getType().name())
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(". See console for details:\n"))
                                .append(Component.text(e.getMessage()))
                ).and(CommandResult.failure(
                        Component.text("Skipping save. If you wish to force the save, use ")
                                .append(Component.text("/mct edit save true")
                                        .clickEvent(ClickEvent.suggestCommand("/mct edit save true"))
                                        .decorate(TextDecoration.BOLD))
                ));
            }
        }
        try {
            activeEditor.saveConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error saving config for editor %s", activeEditor.getType()), e);
            return CommandResult.failure(Component.text("An error occurred while attempting to save the config for ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        if (skipValidation) {
            return CommandResult.success(Component.text("Skipping validation"))
                    .and(CommandResult.success(Component.text("Config is saved.")));
        } else {
            return CommandResult.success(Component.text("Config is saved."));
        }
    }
    
    public CommandResult loadEditor(@NotNull String configFile) {
        try {
            activeEditor.loadConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for editor %s", activeEditor.getType()), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        return CommandResult.success(Component.text("Config loaded."));
    }
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
    /**
     * Remove the given team from the game
     * @param teamId teamId of the team to remove
     */
    public CommandResult removeTeam(String teamId) {
        return state.removeTeam(teamId);
    }
    
    /**
     * Add a team to the game.
     * @param teamId The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     * @return the newly created team, or null if the given team already exists or could not be created
     */
    public Team addTeam(String teamId, String teamDisplayName, String colorString) {
        return state.addTeam(teamId, teamDisplayName, colorString);
    }
    
    /**
     * A list of all the teams in the game
     * @return A list containing the internal names of all the teams in the game. 
     * Empty list if there are no teams
     */
    public Set<String> getTeamIds() {
        return gameStateStorageUtil.getTeamIds();
    }
    
    /**
     * Joins the given player to the team with the given teamId. If the player was on a team already (not teamId) they will be removed from that team and added to the other team. 
     * Note, this will not join a player to a team if that player is an admin. 
     * @param offlinePlayer The player to join to the given team
     * @param name The name of the participant to join to the given team
     * @param teamId The internal teamId of the team to join the player to.
     */
    public CommandResult joinParticipantToTeam(@NotNull OfflinePlayer offlinePlayer, @NotNull String name, @NotNull String teamId) {
        return state.joinParticipantToTeam(offlinePlayer, name, teamId);
    }
    
    /**
     * @param teamId the teamId of the team to get the {@link OfflineParticipant}s from
     * @return a collection of {@link OfflineParticipant}s who are on the given team
     */
    public @NotNull Collection<OfflineParticipant> getParticipantsOnTeam(String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return Collections.emptyList();
        }
        return team.getMemberUUIDs().stream().map(allParticipants::get).toList();
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well. 
     * @param offlineParticipant The participant to remove from their team
     */
    public CommandResult leaveParticipant(@NotNull OfflineParticipant offlineParticipant) {
        return state.leaveParticipant(offlineParticipant);
    }
    
    /**
     * @return the event manager's point multiplier, if there is a match going on. 1.0 otherwise.
     */
    public double getMultiplier() {
        return state.getMultiplier();
    }
    
    /**
     * @return a copy of the list of online participants. Modifying this will not change
     *      * the online participants. Unmodifiable.
     */
    public @NotNull Collection<Participant> getOnlineParticipants() {
        return Collections.unmodifiableCollection(onlineParticipants.values());
    }
    
    /**
     * @param uuid the UUID of the participant to get
     * @return the Participant with the given UUID, if they are online
     */
    @SuppressWarnings("unused")
    public @Nullable Participant getOnlineParticipant(UUID uuid) {
        return onlineParticipants.get(uuid);
    }
    
    /**
     * Gets all the names of the participants in the game state, regardless of 
     * whether they're offline or online. 
     * @return a list of the names of all participants in the game state
     */
    public List<@NotNull String> getAllParticipantNames() {
        return allParticipants.values().stream().map(OfflineParticipant::getName).toList();
    }
    
    /**
     * @param uuid the UUID of the participant
     * @return the OfflineParticipant, or null if they don't exist
     */
    public @Nullable OfflineParticipant getOfflineParticipant(UUID uuid) {
        return allParticipants.get(uuid);
    }
    
    /**
     * @return A list {@link OfflinePlayer}s representing all participants in the {@link GameStateStorageUtil}. Unmodifiable. 
     * These players could be offline or online, have logged in at least once or not
     */
    public Collection<OfflineParticipant> getOfflineParticipants() {
        return Collections.unmodifiableCollection(allParticipants.values());
    }
    
    /**
     * @param teamId the teamId to get the members of
     * @return A list {@link OfflinePlayer}s representing all participants in the {@link GameStateStorageUtil}
     * who are on the given team. 
     * These players could be offline or online, have logged in at least once or not
     */
    public @NotNull Collection<OfflineParticipant> getOfflineParticipants(@NotNull String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return Collections.emptyList();
        }
        return team.getMemberUUIDs().stream().map(allParticipants::get).collect(Collectors.toSet());
    }
    
    /**
     * Adds the given score to the participant with the given UUID
     * @param participant The participant to add the score to
     * @param score The score to add. Could be positive or negative.
     * @return the new score of the participant
     */
    public int addScore(OfflineParticipant participant, int score) {
        return setScore(participant, participant.getScore() + score);
    }
    
    /**
     * Adds the given score to the given team
     * @param team The team to add the score to
     * @param score The score to add. Could be positive or negative.
     * @return the new score of the team
     */
    public int addScore(@NotNull Team team, int score) {
        return setScore(team, team.getScore() + score);
    }
    
    /**
     * Sets the participant's score to the given value, or 0 if the value is negative
     * @param participant the participant to set the score of
     * @param value the score to set the participant to
     * @return the new score of the participant
     */
    public int setScore(@NotNull OfflineParticipant participant, int value) {
        int score = Math.max(0, value);
        OfflineParticipant updated = new OfflineParticipant(participant, score);
        allParticipants.put(participant.getUniqueId(), updated);
        MCTParticipant oldOnline = onlineParticipants.get(participant.getUniqueId());
        List<MCTParticipant> updatedList;
        if (oldOnline != null) {
            MCTParticipant updatedOnline = new MCTParticipant(oldOnline, score);
            onlineParticipants.put(participant.getUniqueId(), updatedOnline);
            updatedList = Collections.singletonList(updatedOnline);
        } else {
            updatedList = Collections.emptyList();
        }
        try {
            gameStateStorageUtil.updateScore(updated);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, gameStateStorageUtil::saveGameState);
            state.updateScoreVisuals(Collections.singletonList(teams.get(updated.getTeamId())), updatedList);
        } catch (ConfigIOException e) {
            reportGameStateException("setting a player's score", e);
        }
        return updated.getScore();
    }
    
    /**
     * Sets the score of the team with the given name to the given value
     * @param team The UUID of the participant to set the score to
     * @param value The score to set to. If the score is negative, the score will be set to 0.
     * @return the new score of the team
     */
    public int setScore(Team team, int value) {
        int score = Math.max(0, value);
        MCTTeam old = teams.get(team.getTeamId());
        if (old == null) {
            return 0;
        }
        MCTTeam updated = new MCTTeam(old, score);
        teams.put(old.getTeamId(), updated);
        try {
            gameStateStorageUtil.updateScore(updated);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, gameStateStorageUtil::saveGameState);
            state.updateScoreVisuals(Collections.singletonList(updated), Collections.emptyList());
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to team", e);
        }
        return updated.getScore();
    }
    
    /**
     * Set all the teams and players scores to the given score
     * @param value the score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScoreAll(int value) {
        int score = Math.max(0, value);
        for (MCTTeam team : teams.values()) {
            teams.put(team.getTeamId(), new MCTTeam(team, score));
        }
        for (OfflineParticipant participant : allParticipants.values()) {
            allParticipants.put(participant.getUniqueId(), new OfflineParticipant(participant, score));
            MCTParticipant online = onlineParticipants.get(participant.getUniqueId());
            if (online != null) {
                onlineParticipants.put(participant.getUniqueId(), new MCTParticipant(online, score));
            }
        }
        try {
            gameStateStorageUtil.updateScores(teams.values(), allParticipants.values());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, gameStateStorageUtil::saveGameState);
            state.updateScoreVisuals(teams.values(), onlineParticipants.values());
        } catch (ConfigIOException e) {
            reportGameStateException("setting all scores", e);
        }
    }
    
    /**
     * Checks if the given player is an admin
     * @param adminUniqueId The unique id of the admin to check
     * @return True if the given player is an admin, false otherwise
     */
    public boolean isAdmin(UUID adminUniqueId) {
        return gameStateStorageUtil.isAdmin(adminUniqueId);
    }
    
    /**
     * Adds the given player as an admin. If the player is already an admin, nothing happens. If the player is a participant, they are removed from their team and added as an admin.
     * @param newAdmin The player to add
     */
    public CommandResult addAdmin(Player newAdmin) {
        return state.addAdmin(newAdmin);
    }
    
    /**
     * Removes the given player from the admins
     * @param offlineAdmin The admin to remove
     */
    public CommandResult removeAdmin(@NotNull OfflinePlayer offlineAdmin, String adminName) {
        return state.removeAdmin(offlineAdmin, adminName);
    }
    
    public void messageAdmins(Component message) {
        Audience.audience(
                Audience.audience(onlineAdmins),
                plugin.getServer().getConsoleSender()
        ).sendMessage(message);
    }
    
    public void playSoundForAdmins(@NotNull String sound, float volume, float pitch) {
        for (Player admin : onlineAdmins) {
            admin.playSound(admin.getLocation(), sound, volume, pitch);
        }
    }
    
    public void messageOnlineParticipants(Component message) {
        Audience.audience(onlineParticipants.values()).sendMessage(message);
    }
    
    // event start
    public CommandResult readyUpParticipant(@NotNull UUID uuid) {
        MCTParticipant participant = onlineParticipants.get(uuid);
        if (participant == null) {
            return CommandResult.failure(Component.text("Not a participant"));
        }
        return state.readyUpParticipant(participant);
    }
    
    public CommandResult unReadyParticipant(@NotNull UUID uuid) {
        MCTParticipant participant = onlineParticipants.get(uuid);
        if (participant == null) {
            return CommandResult.failure(Component.text("Not a participant"));
        }
        return state.unReadyParticipant(participant);
    }
    
    public CommandResult openHubMenu(@NotNull UUID uuid) {
        MCTParticipant participant = onlineParticipants.get(uuid);
        if (participant == null) {
            return CommandResult.failure(Component.text("Not a participant"));
        }
        return state.openHubMenu(participant);
    }
    
    public int getGameIterations(@NotNull GameType gameType) {
        return state.getGameIterations(gameType);
    }
    
    public CommandResult undoGame(@NotNull GameType gameType, int iterationIndex) {
        return state.undoGame(gameType, iterationIndex);
    }
    
    public CommandResult modifyMaxGames(int newMaxGames) {
        return state.modifyMaxGames(newMaxGames);
    }
    
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
        return state.addGameToVotingPool(gameToAdd);
    }
    
    public CommandResult removeGameFromVotingPool(@NotNull GameType gameToRemove) {
        return state.removeGameFromVotingPool(gameToRemove);
    }
    
    public CommandResult listReady(@Nullable String teamId) {
        return state.listReady(teamId);
    }
    
    public void setWinner(@Nullable String teamId) {
        if (teamId == null) {
            return;
        }
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            return;
        }
        state.setWinner(team);
    }
    // event end
    
    // Test methods
    public void reportGameStateException(String attemptedOperation, ConfigException e) {
        Main.logger().severe(String.format("error while %s. See console log for error message.", attemptedOperation));
        messageAdmins(Component.empty()
                .append(Component.text("error while "))
                .append(Component.text(attemptedOperation))
                .append(Component.text(". See console log for error message.")));
        throw new RuntimeException(e);
    }
    
    public @NotNull Set<GameType> getActiveGames() {
        return activeGames.keySet();
    }
    
    public @Nullable MCTGame getActiveGame(@NotNull GameType gameType) {
        return activeGames.get(gameType);
    }
    
    /**
     * Sets the visibility of the main TabList to the given value for the given player. 
     * This is used to allow players to see the player list as default. 
     * @param uuid the UUID of the player who is currently viewing the TabList to set the visibility of
     * @param visible true if the player should see the TabList content, false otherwise.
     */
    public void setTabListVisibility(@NotNull UUID uuid, boolean visible) {
        tabList.setVisibility(uuid, visible);
    }
}
