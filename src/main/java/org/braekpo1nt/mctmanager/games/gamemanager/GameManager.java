package org.braekpo1nt.mctmanager.games.gamemanager;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.mojang.brigadier.arguments.ArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.braekpo1nt.mctmanager.database.entities.ScoreEventEntity;
import org.braekpo1nt.mctmanager.database.exceptions.EventStillInUseException;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.GameManagerState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.MaintenanceState;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Responsible for overall game management.
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager implements Listener {
    
    public @NotNull GameManagerState state;
    public static final String ADMIN_TEAM = "_Admins";
    public static final NamedTextColor ADMIN_COLOR = NamedTextColor.DARK_RED;
    private final Main plugin;
    @Getter
    private final ScoreService scoreService;
    @Getter
    private final EventService eventService;
    @Getter
    private final Executor mainThreadExecutor;
    @Getter
    private final GameStateService gameStateService;
    // TODO: remove these getter and setter and make this a map like activeGames
    @Getter
    @Setter
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
    private final Map<GameInstanceId, MCTGame> activeGames = new HashMap<>();
    /**
     * A reference to which participant is in which game
     */
    protected final Map<UUID, GameInstanceId> participantGames = new HashMap<>();
    /**
     * A reference to which admin is in which game
     */
    protected final Map<UUID, GameInstanceId> adminGames = new HashMap<>();
    /**
     * A reference to which admin is in which editor<br>
     * If an admin's UUID is a key in this map, that admin is in
     * an editor.
     */
    protected final Map<UUID, GameInstanceId> adminEditors = new HashMap<>();
    private final TabList tabList; // TODO: make tabList a thing of each state, so you can have no scores and not list all names during practice mode
    private final @NotNull List<LeaderboardManager> leaderboardManagers;
    private final Sidebar sidebar; // TODO: make sidebar a thing of each state, not central
    @Getter
    private @NotNull HubConfig config;
    
    public GameManager(@NotNull Main plugin,
                       @NotNull Scoreboard mctScoreboard,
                       @NotNull GameStateStorageUtil gameStateStorageUtil,
                       @NotNull SidebarFactory sidebarFactory,
                       @NotNull HubConfig config,
                       @NotNull Database database,
                       @NotNull GameStateService gameStateService,
                       @NotNull Executor mainThreadExecutor) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = gameStateStorageUtil;
        this.timerManager = new TimerManager(plugin);
        String databaseMode = plugin.getConfig().getString("database.mode", "prod");
        this.scoreService = new ScoreService(
                databaseMode,
                database
        );
        this.eventService = new EventService(
                databaseMode,
                database
        );
        this.mainThreadExecutor = mainThreadExecutor;
        this.gameStateService = gameStateService;
        this.sidebarFactory = sidebarFactory;
        this.config = config;
        this.tabList = createTabList(plugin);
        this.leaderboardManagers = createLeaderboardManagers();
        this.sidebar = sidebarFactory.createSidebar();
        ContextReference contextReference = ContextReference.builder()
                .tabList(this.tabList)
                .mctScoreboard(this.mctScoreboard)
                .activeGames(this.activeGames)
                .teams(this.teams)
                .allParticipants(this.allParticipants)
                .onlineParticipants(this.onlineParticipants)
                .onlineAdmins(this.onlineAdmins)
                .participantGames(this.participantGames)
                .adminGames(this.adminGames)
                .adminEditors(this.adminEditors)
                .plugin(this.plugin)
                .gameStateStorageUtil(this.gameStateStorageUtil)
                .mainThreadExecutor(this.mainThreadExecutor)
                .sidebarFactory(this.sidebarFactory)
                .sidebar(this.sidebar)
                .leaderboardManagers(this.leaderboardManagers)
                .build();
        this.state = new MaintenanceState(this, contextReference);
        this.state.enter();
        setSystemStateDescription(this.state.getSystemStateDescription());
    }
    
    public @NotNull TabList createTabList(Main plugin) {
        return new TabList(plugin);
    }
    
    /**
     * {@link GameManagerState#exit()} will be called on the current state,
     * then the current state will be assigned to the new state, finally {@link GameManagerState#enter()}
     * will be called on the current state
     * @param state the new state
     */
    public void setState(@NotNull GameManagerState state) {
        this.state.exit();
        this.state = state;
        this.state.enter();
        setSystemStateDescription(this.state.getSystemStateDescription());
    }
    
    public void setSystemStateDescription(@NotNull String stateDescription) {
        CompletableFuture.runAsync(() -> {
            try {
                gameStateService.setSystemStateDescription(stateDescription);
            } catch (SQLException e) {
                reportGameStateException("Assign the system_state description", e);
            }
        }, plugin.getDatabaseExecutor());
    }
    
    public CommandResult switchMode(@NotNull Mode mode) {
        return state.switchMode(mode);
    }
    
    public @NotNull Mode getMode() {
        return state.getMode();
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
        state.exit();
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
            EquipmentSlot equipmentSlot = GameManagerUtils.toEquipmentSlot(event.getSlotType()); // TODO: replace with non-deprecated alternative
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
    
    private @NotNull CompletableFuture<Void> resolveUUIDandIGNerrors(Player player) {
        /*
        - check if the player's UUID is in the game state
          - if their uuid IS in the game state
            - check to see if their IGN is correct
              - if it IS, there is nothing to do
              - if it is NOT, we must correct the IGN
                - correct the IGN in allParticipants
                - correct the IGN in onlineParticipants
                - correct the IGN in the gameStateStorageUtil
                - correct the IGN in the database
          - if their uuid is NOT in the game state
            - check if the player's ign is in the game state
              - if it is NOT, then they are a new non-participant entity
                - add the new player to the database alone
              - if it IS, there is a UUID mismatch
                - remove the incorrect UUID with the correct IGN from their team
                - resolve the database clash
                - join the participant with the correct UUID and correct IGN to the same team
         */
        UUID correctUUID = player.getUniqueId();
        String correctIGN = player.getName();
        OfflineParticipant existingParticipant = allParticipants.get(correctUUID);
        if (existingParticipant != null) {
            // if the UUID IS in the game state
            String incorrectIGN = existingParticipant.getName();
            if (incorrectIGN.equals(correctIGN)) {
                // their name is correct, nothing to do
                return CompletableFuture.completedFuture(null);
            }
            // if their name is incorrect
            // update the ign in allParticipants
            TextColor color = teams.get(existingParticipant.getTeamId()).getColor();
            Component displayName = GameManagerUtils.createDisplayName(correctIGN, color);
            allParticipants.put(correctUUID, new OfflineParticipant(existingParticipant, correctIGN, displayName));
            MCTParticipant onlineParticipant = onlineParticipants.get(existingParticipant.getUniqueId());
            if (onlineParticipant != null) {
                // this should not happen when logging in, but may happen with debug commands to correct live errors
                // they're online
                // update the ign in onlineParticipants
                onlineParticipants.put(correctUUID, new MCTParticipant(onlineParticipant, correctIGN, displayName));
            }
            // they're not online
            try {
                // update the ign in gameStateStorageUtil AND database
                // also updates the database
                gameStateStorageUtil.setIGN(correctUUID, correctIGN);
            } catch (SQLException e) {
                reportGameStateException(String.format("migrate the ign of the player with uuid \"%s\" from \"%s\" to the correct ign \"%s\"", correctUUID, incorrectIGN, correctIGN), e);
            }
            return CompletableFuture.completedFuture(null);
        }
        // if the UUID is NOT in the game state
        OfflineParticipant offlineParticipantWithIGN = getOfflineParticipant(correctIGN);
        if (offlineParticipantWithIGN == null) {
            // neither the UUID nor the IGN of the given participant is in the gameState
            try {
                // register the new player in the database
                gameStateService.registerPlayer(correctUUID.toString(), correctIGN);
            } catch (SQLException e) {
                reportGameStateException(String.format("register new player with name \"%s\" and UUID \"%s\" in the database", correctIGN, correctUUID), e);
            }
            return CompletableFuture.completedFuture(null);
        }
        // the participant with the wrong UUID but the correct IGN is in the game state
        // we need to migrate the uuid
        UUID incorrectUUID = offlineParticipantWithIGN.getUniqueId();
        return leaveParticipant(offlineParticipantWithIGN)
                .thenRunAsync(() -> {
                    // resolve them in the database
                    try {
                        gameStateService.migrateUUID(incorrectUUID.toString(), correctUUID.toString(), correctIGN);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, plugin.getDatabaseExecutor())
                .exceptionally(e -> {
                    reportGameStateException(String.format("migrate player \"%s\" from UUID \"%s\" to the correct uuid \"%s\" in the database", correctIGN, incorrectUUID, correctUUID), e);
                    return null;
                })
                .thenComposeAsync(v -> {
                    MCTTeam team = teams.get(offlineParticipantWithIGN.getTeamId());
                    return state.joinOfflineParticipantToTeam(correctUUID, correctIGN, team);
                }, mainThreadExecutor)
                // TODO: the CommandResult of joinOfflineParticipant to team is ignored in this case, do something with it 
                .thenApply(ignored -> null);
    }
    
    /**
     * Take a player and change their UUID, but keep the IGN and presence in the game state.
     * Change this in the game state if they are present, and in the database if they are present.
     * @param incorrectUUID the UUID to change from. Must not be an online player, or a participant in the game state
     * @param correctUUID the UUID to change to. Must not be an online player, or a participant in the game state
     * nothing happens.
     * @param correctIGN The IGN of the player to change the UUID of.
     * happens.
     * @return the result of the change.
     */
    public CommandResult migrateUUID(UUID incorrectUUID, UUID correctUUID, String correctIGN) {
        OfflineParticipant incorrectParticipant = allParticipants.get(incorrectUUID);
        if (incorrectParticipant != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("A participant with the \"from\" uuid "))
                    .append(CommandUtils.copiable(incorrectUUID.toString()))
                    .append(Component.text(" is in the game state ("))
                    .append(CommandUtils.copiable(incorrectParticipant.getName()))
                    .append(Component.text("). Remove them to complete the migration."))
            );
        }
        OfflineParticipant correctParticipant = allParticipants.get(correctUUID);
        if (correctParticipant != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("A participant with the \"to\" uuid "))
                    .append(CommandUtils.copiable(correctUUID.toString()))
                    .append(Component.text(" is in the game state ("))
                    .append(CommandUtils.copiable(correctParticipant.getName()))
                    .append(Component.text("). Remove them to complete the migration."))
            );
        }
        Player incorrectUUIDPlayer = plugin.getServer().getPlayer(incorrectUUID);
        if (incorrectUUIDPlayer != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("A participant with the \"from\" uuid "))
                    .append(CommandUtils.copiable(incorrectUUID.toString()))
                    .append(Component.text(" is online ("))
                    .append(CommandUtils.copiable(incorrectUUIDPlayer.getName()))
                    .append(Component.text("). They must disconnect from the server before this migration."))
            );
        }
        Player correctUUIDPlayer = plugin.getServer().getPlayer(correctUUID);
        if (correctUUIDPlayer != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("A participant with the \"to\" uuid "))
                    .append(CommandUtils.copiable(correctUUID.toString()))
                    .append(Component.text(" is online ("))
                    .append(CommandUtils.copiable(correctUUIDPlayer.getName()))
                    .append(Component.text("). They must disconnect from the server before this migration."))
            );
        }
        try {
            gameStateService.migrateUUID(incorrectUUID.toString(), correctUUID.toString(), correctIGN);
        } catch (SQLException e) {
            return CommandResult.sqlException("migrate player UUID", e);
        }
        return CommandResult.success(Component.empty()
                .append(Component.text("Migrated player UUID from "))
                .append(Component.text(incorrectUUID.toString())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to "))
                .append(Component.text(correctUUID.toString())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" for IGN "))
                .append(Component.text(correctIGN)
                        .decorate(TextDecoration.BOLD))
        );
    }
    
    /**
     * @param correctUUID the UUID of the player to set the IGN of
     * @param correctIGN the IGN to set to
     * @return the result of the operation
     */
    public CommandResult migrateIGN(UUID correctUUID, String correctIGN) {
        OfflineParticipant correctParticipant = allParticipants.get(correctUUID);
        if (correctParticipant != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("The participant with the uuid "))
                    .append(CommandUtils.copiable(correctUUID.toString()))
                    .append(Component.text(" is in the game state ("))
                    .append(CommandUtils.copiable(correctParticipant.getName()))
                    .append(Component.text("). Remove them to complete the migration."))
            );
        }
        OfflineParticipant participantWithIGN = getOfflineParticipant(correctIGN);
        if (participantWithIGN != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("The participant with the IGN "))
                    .append(CommandUtils.copiable(correctIGN))
                    .append(Component.text(" is in the game state ("))
                    .append(CommandUtils.copiable(participantWithIGN.getUniqueId().toString()))
                    .append(Component.text("). Remove them to complete the migration."))
            );
        }
        Player correctUUIDPlayer = plugin.getServer().getPlayer(correctUUID);
        if (correctUUIDPlayer != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("The participant with the uuid "))
                    .append(CommandUtils.copiable(correctUUID.toString()))
                    .append(Component.text(" is online ("))
                    .append(CommandUtils.copiable(correctUUIDPlayer.getName()))
                    .append(Component.text("). They must disconnect from the server before this migration."))
            );
        }
        Player correctIGNPlayer = plugin.getServer().getPlayer(correctIGN);
        if (correctIGNPlayer != null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("The participant with the IGN "))
                    .append(CommandUtils.copiable(correctIGNPlayer.getName()))
                    .append(Component.text(" is online ("))
                    .append(CommandUtils.copiable(correctIGNPlayer.getUniqueId().toString()))
                    .append(Component.text("). They must disconnect from the server before this migration."))
            );
        }
        try {
            gameStateService.migrateIgn(correctUUID.toString(), correctIGN);
        } catch (SQLException e) {
            return CommandResult.sqlException("migrate player IGN", e);
        }
        return CommandResult.success(Component.empty()
                .append(Component.text("Migrated IGN of player with UUID "))
                .append(Component.text(correctUUID.toString())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to "))
                .append(Component.text(correctIGN)
                        .decorate(TextDecoration.BOLD))
        );
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        resolveUUIDandIGNerrors(player)
                .thenComposeAsync(v -> {
                    if (isAdmin(player.getUniqueId())) {
                        state.onAdminJoin(event, player);
                        return CompletableFuture.completedFuture(null);
                    }
                    OfflineParticipant offlineParticipant = allParticipants.get(player.getUniqueId());
                    if (offlineParticipant != null) {
                        MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
                        return state.onParticipantJoin(event, participant);
                    }
                    state.onNonJoin(player);
                    return CompletableFuture.completedFuture(null);
                }, mainThreadExecutor)
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, String.format("An error occurred when the player %s joined (with UUID %s)", player.getName(), player.getUniqueId()), e);
                    return null;
                });
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_DAMAGE_CAUSES.contains(event.getCause())) {
            return;
        }
        MCTParticipant participant = onlineParticipants.get(event.getEntity().getUniqueId());
        if (participant != null) {
            state.onParticipantDamage(event, participant);
            return;
        }
        if (!(event.getEntity() instanceof Player admin)) {
            return;
        }
        if (!onlineAdmins.contains(admin)) {
            return;
        }
        state.onAdminDamage(event, admin);
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
    
    public CompletableFuture<CommandResult> joinParticipantToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull UUID uuid) {
        MCTParticipant mctParticipant = onlineParticipants.get(uuid);
        if (mctParticipant == null) {
            return CommandResult.failure(Component.text("You are not a participant")).asFuture();
        }
        return state.joinParticipantToGame(gameType, configFile, mctParticipant);
    }
    
    public @NotNull List<String> getActiveConfigFiles(@NotNull GameType gameType) {
        return activeGames.keySet().stream()
                .filter(gameInstanceId -> gameInstanceId.getGameType().equals(gameType))
                .map(GameInstanceId::getConfigFile)
                .toList();
    }
    
    /**
     * Expensive operation, searches the file system, should be performed on separate thread
     * @param gameId the {@link GameType} to search for the config files of
     * @return a list of the config files in the directory associated with the given gameId
     */
    public @NotNull List<String> getConfigFiles(@NotNull GameType gameId) {
        return CommandUtils.getGameConfigs(plugin, gameId);
    }
    
    public @Nullable List<String> tabCompleteActiveGame(@NotNull String[] args) {
        return state.tabCompleteActiveGames(args);
    }
    
    public CommandResult joinAdminToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull Player admin) {
        if (!isAdmin(admin.getUniqueId())) {
            return CommandResult.failure("You are not an admin");
        }
        return state.joinAdminToGame(gameType, configFile, admin);
    }
    
    public CompletableFuture<CommandResult> returnParticipantToHub(@NotNull UUID uuid) {
        MCTParticipant mctParticipant = onlineParticipants.get(uuid);
        if (mctParticipant == null) {
            return CommandResult.failure(Component.text("You are not a participant")).asFuture();
        }
        return state.returnParticipantToHub(mctParticipant);
    }
    
    public CommandResult returnAdminToHub(@NotNull Player admin) {
        if (!isAdmin(admin.getUniqueId())) {
            return CommandResult.failure("You are not an admin");
        }
        return state.returnAdminToHub(admin);
    }
    
    /**
     * @return a new sidebar
     */
    public Sidebar createSidebar() {
        return state.createSidebar();
    }
    
    public @NotNull CompletableFuture<CommandResult> loadGameState() {
        return state.loadGameState();
    }
    
    public boolean eventIsActive() {
        return state.eventIsActive();
    }
    
    public CompletableFuture<CommandResult> startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        return state.startGame(teamIds, gameAdmins, gameType, configFile);
    }
    
    /**
     * @return a list of all eventIds in the database, or empty list if there are no
     * entries in the database
     * @throws SQLException if there is an issue connecting to the database
     */
    public @NotNull List<String> getEventIds() throws SQLException {
        return eventService.getEventIds();
    }
    
    public CompletableFuture<CommandResult> createEvent(String eventId, Date eventDate, String plainTextName, Component componentName, boolean canonical) {
        Date now = new Date();
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        boolean uniqueKey = eventService.addEventInfo(EventInfo.builder()
                                .eventId(eventId)
                                .plainTextName(plainTextName)
                                .componentName(componentName)
                                .eventDate(eventDate)
                                .createdAt(now)
                                .modifiedAt(now)
                                .startedAt(null)
                                .endedAt(null)
                                .winnerTeamId(null)
                                .canonical(canonical)
                                .standingsVersion(0)
                                .build());
                        if (!uniqueKey) {
                            return CommandResult.failure(Component.empty()
                                    .append(Component.text("An event already exists with the given id: "))
                                    .append(Component.text(eventId)
                                            .decorate(TextDecoration.BOLD))
                            );
                        }
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                    return CommandResult.success(Component.empty()
                            .append(Component.text("Created event \""))
                            .append(Component.text(eventId))
                            .append(Component.text("\" with plain-text name \""))
                            .append(Component.text(plainTextName))
                            .append(Component.text("\" and Component name \""))
                            .append(componentName)
                            .append(Component.text("\""))
                    );
                }, plugin.getDatabaseExecutor())
                .exceptionally(e -> CommandResult.throwable("add event info", e));
    }
    
    /**
     * Remove the event with the given ID from the database
     * @param eventId the id of the event to remove
     * @return a command result detailing the result of the operation
     */
    public CommandResult deleteEvent(@NotNull String eventId) {
        try {
            boolean eventExisted = eventService.deleteEvent(eventId);
            if (!eventExisted) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No event with the id "))
                        .append(Component.text(eventId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" was found"))
                );
            }
            return CommandResult.success(Component.empty()
                    .append(Component.text("Event with id "))
                    .append(Component.text(eventId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" deleted"))
            );
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("An error occurred while trying to remove the event with id \"%s\" from the database", eventId), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("An error occurred trying to delete the event with id "))
                    .append(Component.text(eventId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details."))
            );
        } catch (EventStillInUseException e) {
            Main.logger().log(Level.WARNING, String.format("Tried to delete event with id \"%s\" when there are other tables still referencing it", eventId), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("You can't delete event "))
                    .append(Component.text(eventId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because there are still score values and/or participants referencing it. This would destroy history."))
            );
        }
    }
    
    public CompletableFuture<CommandResult> startEvent(@NotNull EventInfo eventInfo, int maxGames, int currentGameNumber) {
        return state.startEvent(eventInfo, maxGames, currentGameNumber);
    }
    
    public @NotNull CompletableFuture<CommandResult> stopEvent() {
        return state.stopEvent();
    }
    
    /**
     * Starts the given game with all teams and all admins
     * @param gameType The game to start
     * @param configFile the config file to use for the game
     * @return a CommandResult indicating the success or failure of starting the game,
     * including a reason why the game didn't start if so.
     */
    public CompletableFuture<CommandResult> startGame(@NotNull GameType gameType, @NotNull String configFile) {
        return state.startGame(teams.keySet(), onlineAdmins, gameType, configFile); // TODO: this should call the other startGame() in this class or vice versa
    }
    
    /**
     * @deprecated just meant for a temporary test to make sure my fake thread safe checks work. Delete when found again
     * and the associated test.
     */
    @Deprecated
    public CompletableFuture<Void> illegalMove(Player player) {
        return CompletableFuture.runAsync(() -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1, 1));
                }, plugin.getDatabaseExecutor())
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, "An error occurred during illegalMove", e);
                    throw new CompletionException("an error occurred during illegalMove", e);
                });
    }
    
    public CommandResult startEditor(@NotNull GameType gameType, @NotNull String configFile) {
        return state.startEditor(gameType, configFile);
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
    public @Nullable GameInstanceId getTeamActiveGame(String teamId) {
        for (Map.Entry<GameInstanceId, MCTGame> entry : activeGames.entrySet()) {
            MCTGame game = entry.getValue();
            if (game.containsTeam(teamId)) {
                return entry.getKey();
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
    
    public CompletableFuture<CommandResult> stopGame(@NotNull GameType gameType, @Nullable String configFile) {
        return state.stopGame(gameType, configFile);
    }
    
    /**
     * Stop all active games, if there are any
     */
    public CompletableFuture<CommandResult> stopAllGames() {
        return state.stopAllGames();
    }
    
    /**
     * Called by an active game when the game is over.
     */
    public void gameIsOver(int gameSessionId, @NotNull GameInstanceId id, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants, @NotNull List<Player> gameAdmins) {
        state.gameIsOver(gameSessionId, id, teamScores, participantScores, gameParticipants, gameAdmins);
    }
    
    /**
     * Called by an active editor when the editor is ended
     * @param editorAdmins the admins who were in the editor when it ended
     */
    public void editorIsOver(@NotNull Collection<Player> editorAdmins) {
        state.editorIsOver(editorAdmins);
    }
    
    /**
     * @param id the {@link GameInstanceId} to check
     * @return true if the given game instance id is currently being played, false otherwise
     */
    public boolean gameIsActive(@NotNull GameInstanceId id) {
        return activeGames.containsKey(id);
    }
    
    /**
     * @param gameType the GameType to check for active games during
     * @return true if there are any active games with the given GameType
     */
    public boolean gameIsActive(@NotNull GameType gameType) {
        return activeGames.keySet().stream()
                .anyMatch(gameInstanceId -> gameInstanceId.getGameType().equals(gameType));
    }
    
    // editor start
    public CommandResult stopEditor() {
        return state.stopEditor();
    }
    
    public boolean editorIsRunning() {
        return state.editorIsRunning();
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
        return state.validateEditor(configFile);
    }
    
    /**
     * @param skipValidation if true, validation will be skipped and the config will be saved even if invalid, if false
     * the config will only save if it is valid
     */
    public CommandResult saveEditor(@NotNull String configFile, boolean skipValidation) {
        return state.saveEditor(configFile, skipValidation);
    }
    
    public CommandResult loadEditor(@NotNull String configFile) {
        return state.loadEditor(configFile);
    }
    // editor end
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
    /**
     * Remove the given team from the game
     * @param teamId teamId of the team to remove
     */
    public CompletableFuture<CommandResult> removeTeam(String teamId) {
        return state.removeTeam(teamId);
    }
    
    /**
     * Add a team to the game.
     * @param teamId The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     * @return the newly created team, or null if the given team already exists or could not be created
     */
    public CompletableFuture<Team> addTeam(String teamId, String teamDisplayName, String colorString) {
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
     * Joins the given player to the team with the given teamId. If the player was on a team already (not teamId) they
     * will be removed from that team and added to the other team.
     * Note, this will not join a player to a team if that player is an admin.
     * @param uuid The UUID of the participant to join to the given team
     * @param ign The name of the participant to join to the given team
     * @param teamId The teamId of the team to join the participant to.
     */
    public CompletableFuture<CommandResult> joinOfflineParticipant(@NotNull UUID uuid, @NotNull String ign, @NotNull String teamId) {
        MCTTeam team = teams.get(teamId);
        return state.joinOfflineParticipantToTeam(uuid, ign, team);
    }
    
    public CompletableFuture<CommandResult> joinOnlineParticipant(@NotNull Player player, @NotNull String teamId) {
        MCTTeam team = teams.get(teamId);
        return state.joinOnlineParticipantToTeam(player, team);
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
    public CompletableFuture<CommandResult> leaveParticipant(@NotNull OfflineParticipant offlineParticipant) {
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
     * * the online participants. Unmodifiable.
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
    public @Nullable OfflineParticipant getOfflineParticipant(@NotNull UUID uuid) {
        return allParticipants.get(uuid);
    }
    
    /**
     * @param ign the in game name of the participant
     * @return the OfflineParticipant, or null if they don't exist
     */
    public @Nullable OfflineParticipant getOfflineParticipant(@NotNull String ign) {
        return allParticipants.values().stream()
                .filter(offlineParticipant -> offlineParticipant.getName().matches(ign))
                .findFirst()
                .orElse(null);
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
    
    public void logScoreEvent(
            @NotNull ScoreEvent scoreEventDTO
    ) {
        scoreService.logScoreEvent(scoreEventDTO.toScoreEvent(
                state.getEventId(),
                state.getMode())
        );
    }
    
    public void logScoreEvents(
            @NotNull Collection<ScoreEvent> scoreEventDTOs
    ) {
        List<ScoreEventEntity> scoreEvents = scoreEventDTOs.stream()
                .map(scoreEventDTO -> scoreEventDTO.toScoreEvent(
                        state.getEventId(),
                        state.getMode()
                ))
                .toList();
        scoreService.logScoreEvents(scoreEvents);
    }
    
    /**
     * Adds the given score to the participant with the given UUID
     * @param participant The participant to add the score to
     * @param score The score to add. Could be positive or negative.
     * @param description a description of why the score changed
     * @return the new score of the participant
     */
    public CompletableFuture<Integer> addScore(OfflineParticipant participant, int score, @NotNull String description) {
        return setScore(participant, participant.getScore() + score, description);
    }
    
    /**
     * Adds the given score to the given team
     * @param team The team to add the score to
     * @param score The score to add. Could be positive or negative.
     * @param description a description of why the score changed
     * @return the new score of the team
     */
    public CompletableFuture<Integer> addScore(@NotNull Team team, int score, @NotNull String description) {
        return setScore(team, team.getScore() + score, description);
    }
    
    /**
     * Sets the participant's score to the given actualDelta, or 0 if the actualDelta is negative
     * @param participant the participant to set the score of
     * @param value the score to set the participant to
     * @param description a description of why the score changed
     * @return the new score of the participant in a future after database operations are complete
     */
    public CompletableFuture<Integer> setScore(@NotNull OfflineParticipant participant, int value, @NotNull String description) {
        int oldScore = participant.getScore();
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
        gameStateStorageUtil.persistScore(updated);
        state.updateScoreVisuals(Collections.singletonList(teams.get(updated.getTeamId())), updatedList);
        int newScore = updated.getScore();
        return CompletableFuture.runAsync(() -> {
                    int actualDelta = score - oldScore;
                    logScoreEvents(List.of(
                            ScoreEvent.builder()
                                    .sourceType(ScoreEvent.SourceType.ADMIN)
                                    .gameSessionId(null)
                                    .participantUUID(participant.getUniqueId().toString())
                                    .teamId(participant.getTeamId())
                                    .pointsBase(actualDelta)
                                    .description(description)
                                    .createdAt(new Date())
                                    .build(),
                            ScoreEvent.builder()
                                    .sourceType(ScoreEvent.SourceType.ADMIN)
                                    .gameSessionId(null)
                                    .participantUUID(null)
                                    .teamId(participant.getTeamId())
                                    .pointsBase(-actualDelta)
                                    .description(String.format("%s (team adjustment)", description))
                                    .createdAt(new Date())
                                    .build()
                    ));
                }, plugin.getDatabaseExecutor())
                .thenApply(v -> newScore);
    }
    
    /**
     * Sets the score of the team with the given name to the given actualDelta
     * @param team The UUID of the participant to set the score to
     * @param value The score to set to. If the score is negative, the score will be set to 0.
     * @param description a description of why the score changed
     * @return the new score of the team
     */
    public CompletableFuture<Integer> setScore(Team team, int value, @NotNull String description) {
        int oldScore = team.getScore();
        int score = Math.max(0, value);
        MCTTeam old = teams.get(team.getTeamId());
        if (old == null) {
            return CompletableFuture.completedFuture(0);
        }
        MCTTeam updated = new MCTTeam(old, score);
        teams.put(old.getTeamId(), updated);
        gameStateStorageUtil.persistScore(updated);
        state.updateScoreVisuals(Collections.singletonList(updated), Collections.emptyList());
        int newScore = updated.getScore();
        return CompletableFuture.runAsync(() -> {
                    int actualDelta = score - oldScore;
                    logScoreEvent(ScoreEvent.builder()
                            .sourceType(ScoreEvent.SourceType.ADMIN)
                            .gameSessionId(null)
                            .participantUUID(null)
                            .teamId(team.getTeamId())
                            .pointsBase(actualDelta)
                            .description(description)
                            .createdAt(new Date())
                            .build());
                }, plugin.getDatabaseExecutor())
                .thenApply(v -> newScore);
    }
    
    /**
     * Small helper record for passing score data to the database
     */
    @AllArgsConstructor
    @Getter
    private static class AllScoreEntity {
        /**
         * the uuid of the participant or null if it's just a team
         */
        private final @Nullable String uuid;
        /**
         * the teamId of the team, or the teamId of the participant
         */
        private final @NotNull String teamId;
        /**
         * the actual delta score
         * (the difference between what the score was and what it is being set to)
         */
        @Setter
        private int actualDelta;
    }
    
    /**
     * Set all the teams and players scores to the given score
     * @param value the score to set to. If the score is negative, the score will be set to 0.
     * @return a future containing database operations
     */
    public CompletableFuture<Void> setScoreAll(int value, @NotNull String description) {
        int score = Math.max(0, value);
        // this list is for passing info to the logScoreEvents call below, and nothing else
        Map<String, AllScoreEntity> teamDeltas = new HashMap<>(teams.size());
        for (MCTTeam team : teams.values()) {
            int actualDelta = score - team.getScore();
            teamDeltas.put(team.getTeamId(),
                    new AllScoreEntity(
                            null,
                            team.getTeamId(),
                            actualDelta
                    ));
            teams.put(team.getTeamId(), new MCTTeam(team, score));
        }
        List<AllScoreEntity> actualDeltas = new ArrayList<>(allParticipants.size() + teams.size());
        for (OfflineParticipant participant : allParticipants.values()) {
            int actualDelta = score - participant.getScore();
            actualDeltas.add(new AllScoreEntity(
                    participant.getUniqueId().toString(),
                    participant.getTeamId(),
                    actualDelta
            ));
            AllScoreEntity teamEntry = teamDeltas.get(participant.getTeamId());
            teamEntry.setActualDelta(teamEntry.getActualDelta() - actualDelta);
            allParticipants.put(participant.getUniqueId(), new OfflineParticipant(participant, score));
            MCTParticipant online = onlineParticipants.get(participant.getUniqueId());
            if (online != null) {
                onlineParticipants.put(participant.getUniqueId(), new MCTParticipant(online, score));
            }
        }
        actualDeltas.addAll(teamDeltas.values());
        gameStateStorageUtil.updateScores(teams.values(), allParticipants.values());
        state.updateScoreVisuals(teams.values(), onlineParticipants.values());
        return CompletableFuture.runAsync(() -> {
            Date date = new Date();
            List<ScoreEvent> scoreEvents = actualDeltas.stream()
                    .map(actualDelta -> ScoreEvent.builder()
                            .sourceType(ScoreEvent.SourceType.ADMIN)
                            .gameSessionId(null)
                            .participantUUID(actualDelta.getUuid())
                            .teamId(actualDelta.getTeamId())
                            .pointsBase(actualDelta.getActualDelta())
                            .description(description)
                            .createdAt(date)
                            .build())
                    .toList();
            try {
                gameStateStorageUtil.persistScores(teams.values(), allParticipants.values());
                logScoreEvents(scoreEvents);
            } catch (Exception e) {
                reportGameStateException("setting all scores", e);
            }
        }, plugin.getDatabaseExecutor());
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
     * Adds the given player as an admin. If the player is already an admin, nothing happens. If the player is a
     * participant, they are removed from their team and added as an admin.
     * @param newAdmin The player to add
     */
    public CompletableFuture<CommandResult> addAdmin(Player newAdmin) {
        return state.addAdmin(newAdmin);
    }
    
    /**
     * Removes the given player from the admins
     * @param offlineAdmin The admin to remove
     * @param adminName Something to use as the admin name for console output, even if it's just the UUID of the offline
     * player
     */
    public @NotNull CompletableFuture<CommandResult> removeAdmin(@NotNull OfflinePlayer offlineAdmin, @NotNull String adminName) {
        return state.removeAdmin(offlineAdmin, adminName);
    }
    
    /**
     * Note, this is reaches out to the database and should be called in a separate thread
     * @return a list of all the names of all the admins, online or not, or an empty list
     * if there are no admins or if there is an error connecting to the database
     */
    public @NotNull CompletableFuture<List<String>> getAllAdminNames() {
        return gameStateStorageUtil.getAllAdminNames()
                .thenApply(map -> map.values().stream().toList());
    }
    
    public @Nullable OfflinePlayer getOfflineAdmin(@NotNull UUID uuid) {
        if (isAdmin(uuid)) {
            return plugin.getServer().getOfflinePlayer(uuid);
        }
        return null;
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
    
    // commands start
    public CommandResult top(@NotNull UUID uuid) {
        MCTParticipant participant = onlineParticipants.get(uuid);
        if (participant == null) {
            return CommandResult.failure(Component.text("Not a participant"));
        }
        return state.top(participant);
    }
    // commands end
    
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
    
    public List<Integer> getGameSessionIds(
            @Nullable String eventId,
            @NotNull GameType gameType,
            @NotNull String configFile,
            @NotNull Mode gameMode
    ) throws SQLException {
        return scoreService.getGameSessionIds(
                eventId,
                gameType,
                configFile,
                gameMode
        );
    }
    
    public CommandResult undoGame(int gameSessionId) {
        return state.undoGame(gameSessionId);
    }
    
    public CommandResult redoGame(int gameSessionId) {
        return state.redoGame(gameSessionId);
    }
    
    public CompletableFuture<CommandResult> modifyMaxGames(int newMaxGames) {
        return state.modifyMaxGames(newMaxGames);
    }
    
    public CommandResult whitelist(boolean whitelist) {
        return state.whitelist(whitelist);
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
    
    public Component printGameState() {
        return gameStateStorageUtil.printGameState();
    }
    
    // Test methods
    public void reportGameStateException(String attemptedOperation, Throwable e) {
        Main.logger().severe(String.format("error while %s. See console log for error message.", attemptedOperation));
        messageAdmins(Component.empty()
                .append(Component.text("error while "))
                .append(Component.text(attemptedOperation))
                .append(Component.text(". See console log for error message.")));
        throw new RuntimeException(e);
    }
    
    public @NotNull List<GameInstanceId> getActiveGameIds() {
        return new ArrayList<>(activeGames.keySet());
    }
    
    public @Nullable MCTGame getActiveGame(@NotNull GameInstanceId id) {
        return activeGames.get(id);
    }
    
    public @NotNull List<GameType> getVotingPool() {
        return state.getVotingPool();
    }
    
    /**
     * Sets the visibility of the main TabList to the given actualDelta for the given player.
     * This is used to allow players to see the player list as default.
     * @param uuid the UUID of the player who is currently viewing the TabList to set the visibility of
     * @param visible true if the player should see the TabList content, false otherwise.
     */
    public void setTabListVisibility(@NotNull UUID uuid, boolean visible) {
        tabList.setVisibility(uuid, visible);
    }
    
    /**
     * @deprecated replace this once you figure out how to mock the {@link ArgumentTypes#player()}
     */
    @Deprecated
    public ArgumentType<?> getPlayerArgumentType() {
        return ArgumentTypes.player();
    }
    
    /**
     * @deprecated replace this once you figure out how to mock the {@link ArgumentTypes#component()}
     */
    @Deprecated
    public ArgumentType<?> getComponentArgumentType() {
        return ArgumentTypes.component();
    }
}
