package org.braekpo1nt.mctmanager.games;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.hub.HubManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager implements Listener {
    
    private final Logger LOGGER;
    public static final String ADMIN_TEAM = "_Admins";
    public static final NamedTextColor ADMIN_COLOR = NamedTextColor.DARK_RED;
    private final Main plugin;
    private MCTGame activeGame = null;
    private GameEditor activeEditor = null;
    private final HubManager hubManager;
    private SidebarFactory sidebarFactory;
    private GameStateStorageUtil gameStateStorageUtil;
    /**
     * Scoreboard for holding the teams. This private scoreboard can't be
     * modified using the normal /team command, and thus can't be unsynced
     * with the game state.
     */
    private final Scoreboard mctScoreboard;
    private boolean shouldTeleportToHub = true;
    private final VoteManager voteManager;
    private final EventManager eventManager;
    private final TimerManager timerManager;
    /**
     * Contains the list of all teams. Updated when teams are added/removed or when participants are 
     * joined/left or quit/join
     */
    private final Map<String, Team> teams = new HashMap<>();
    /**
     * Contains the list of online participants. Updated when participants are added/removed or quit/join
     */
    private final Map<UUID, Participant> onlineParticipants = new HashMap<>();
    private final List<Player> onlineAdmins = new ArrayList<>();
    private final TabList tabList;
    
    public GameManager(Main plugin, Scoreboard mctScoreboard) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.LOGGER = plugin.getLogger();
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.voteManager = new VoteManager(this, plugin);
        this.timerManager = new TimerManager(plugin);
        this.tabList = new TabList(plugin);
        this.sidebarFactory = new SidebarFactory();
        this.hubManager = initializeHubManager(plugin, this);
        this.eventManager = new EventManager(plugin, this, voteManager);
    }
    
    /**
     * @return the TimerManager associated with this GameManager. This should be used to register all timers in games and events, so that they can be easily paused, resumed, skipped, etc. in bulk.
     */
    public TimerManager getTimerManager() {
        return this.timerManager;
    }
    
    protected HubManager initializeHubManager(Main plugin, GameManager gameManager) {
        return new HubManager(plugin, gameManager);
    }
    
    /**
     * @param gameType the {@link GameType} to instantiate the {@link MCTGame} for
     * @return a new {@link MCTGame} instance for the given type. Null if the given type is null. 
     */
    @Contract("null -> null")
    private MCTGame instantiateGame(GameType gameType) {
        return switch (gameType) {
            case SPLEEF -> new SpleefGame(plugin, this);
            case CLOCKWORK -> new ClockworkGame(plugin, this);
            case SURVIVAL_GAMES -> new SurvivalGamesGame(plugin, this);
            case FARM_RUSH -> new FarmRushGame(plugin, this);
            case FOOT_RACE -> new FootRaceGame(plugin, this);
            case PARKOUR_PATHWAY -> new ParkourPathwayGame(plugin, this);
            case CAPTURE_THE_FLAG -> new CaptureTheFlagGame(plugin, this);
            case null -> null;
        };
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
    public void onParticipantDeath(PlayerDeathEvent event) {
        Player killed = event.getPlayer();
        if (isParticipant(killed.getUniqueId())) {
            replaceWithDisplayName(event, killed);
        }
        Player killer = killed.getKiller();
        if (killer != null) {
            if (isParticipant(killer.getUniqueId())) {
                replaceWithDisplayName(event, killer);
            }
        }
        GameManagerUtils.deColorLeatherArmor(event.getDrops());
    }
    
    /**
     * Takes in a {@link PlayerDeathEvent} and replaces all instances of the given player's name with the given player's display name
     * @param event the event
     * @param player the player whose name should be replaced with their display name. 
     */
    private static void replaceWithDisplayName(PlayerDeathEvent event, Player player) {
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Component newDeathMessage = GameManagerUtils.replaceWithDisplayName(player, deathMessage);
            event.deathMessage(newDeathMessage);
        }
    }
    
    @EventHandler
    public void onPlayerChangeArmor(PlayerArmorChangeEvent event) {
        Participant participant = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        ItemStack newItem = event.getNewItem();
        if (isLeatherArmor(newItem)) {
            GameManagerUtils.colorLeatherArmor(newItem, teams.get(participant.getTeamId()).getBukkitColor());
            participant.getEquipment().setItem(toEquipmentSlot(event.getSlotType()), newItem);
        }
        ItemStack oldItem = event.getOldItem();
        if (isLeatherArmor(oldItem)) {
            GameManagerUtils.deColorLeatherArmor(oldItem);
            ItemStack cursor = participant.getOpenInventory().getCursor();
            ItemStack mainHand = participant.getInventory().getItemInMainHand();
            ItemStack offHand = participant.getInventory().getItemInOffHand();
            if (isLeatherArmor(cursor)) {
                GameManagerUtils.deColorLeatherArmor(cursor);
            }
            if (isLeatherArmor(mainHand)) {
                GameManagerUtils.deColorLeatherArmor(mainHand);
            }
            if (isLeatherArmor(offHand)) {
                GameManagerUtils.deColorLeatherArmor(offHand);
            }
            GameManagerUtils.deColorLeatherArmor(Arrays.asList(participant.getInventory().getStorageContents()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player participant = event.getPlayer();
        if (!isParticipant(participant.getUniqueId())) {
            return;
        }
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (!isLeatherArmor(itemStack)) {
            return;
        }
        GameManagerUtils.deColorLeatherArmor(itemStack);
    }
    
    public EquipmentSlot toEquipmentSlot(@NotNull PlayerArmorChangeEvent.SlotType slotType) {
        switch (slotType) {
            case HEAD -> {
                return EquipmentSlot.HEAD;
            }
            case CHEST -> {
                return EquipmentSlot.CHEST;
            }
            case LEGS -> {
                return EquipmentSlot.LEGS;
            }
            case FEET -> {
                return EquipmentSlot.FEET;
            }
            default -> {
                // won't return null if slotType is not null
                return null;
            }
        }
    }

        // TODO: handle block dispensing items
//    @EventHandler
//    public void onBlockDispenseItem(BlockDispenseArmorEvent event) {
//    }
    
    @EventHandler
    public void onParticipantInteract(PlayerInteractEvent event) {
        Player participant = event.getPlayer();
        if (!onlineParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !GameManagerUtils.SIGNS.contains(clickedBlock.getType())) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * @param item the item in question. If this is null, will return false. 
     * @return true if the item is of a leather armor type, false otherwise. False if the given item is null. 
     */
    @Contract("null -> false")
    private boolean isLeatherArmor(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getItemMeta() instanceof LeatherArmorMeta;
    }
    
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            event.quitMessage(GameManagerUtils.replaceWithDisplayName(player, event.quitMessage()));
            onAdminQuit(player);
            return;
        }
        Participant participant = onlineParticipants.get(player.getUniqueId());
        if (participant != null) {
            event.quitMessage(GameManagerUtils.replaceWithDisplayName(player, event.quitMessage()));
            onParticipantQuit(participant);
        }
    }
    
    private void onAdminQuit(@NotNull Player admin) {
        onlineAdmins.remove(admin);
        if (gameIsRunning()) {
            activeGame.onAdminQuit(admin);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onAdminQuit(admin);
        } else if (voteManager.isVoting()) {
            voteManager.onAdminQuit(admin);
        }
        hubManager.onAdminQuit(admin);
        tabList.hidePlayer(admin.getUniqueId());
        Component displayName = Component.text(admin.getName(), NamedTextColor.WHITE);
        admin.displayName(displayName);
        admin.playerListName(displayName);
    }
    
    /**
     * Handles when a participant leaves the event.
     * Should be called when a participant disconnects (quits/leaves) from the server 
     * (see {@link GameManager#playerQuitEvent(PlayerQuitEvent)}),
     * or when they are removed from the participants list
     * @param participant The participant who left the event
     * @see GameManager#leaveParticipant(CommandSender, OfflinePlayer, String) 
     */
    private void onParticipantQuit(@NotNull Participant participant) {
        Team team = teams.get(participant.getTeamId());
        team.quitOnlineMember(participant.getUniqueId());
        onlineParticipants.remove(participant.getUniqueId());
        if (gameIsRunning()) {
            activeGame.onParticipantQuit(participant);
            if (team.getOnlineMembers().isEmpty()) { // if there are no more online members of this team
                activeGame.onTeamQuit(new Team(team));
            }
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onParticipantQuit(participant);
        } else if (voteManager.isVoting()) {
            voteManager.onParticipantQuit(participant);
        }
        hubManager.onParticipantQuit(participant);
        Component displayName = Component.text(participant.getName(), 
                NamedTextColor.WHITE);
        participant.displayName(displayName);
        participant.playerListName(displayName);
        GameManagerUtils.deColorLeatherArmor(participant.getInventory());
        tabList.hidePlayer(participant.getUniqueId());
        tabList.setParticipantGrey(participant.getUniqueId(), true);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            onAdminJoin(player);
            event.joinMessage(GameManagerUtils.replaceWithDisplayName(player, event.joinMessage()));
            return;
        }
        if (isParticipant(player.getUniqueId())) {
            Participant participant = createNewParticipant(player);
            onParticipantJoin(participant);
            event.joinMessage(GameManagerUtils.replaceWithDisplayName(player, event.joinMessage()));
            return;
        }
        if (isOfflineIGN(player.getName())) {
            onOfflineIGNJoin(player);
            event.joinMessage(GameManagerUtils.replaceWithDisplayName(player, event.joinMessage()));
        }
    }
    
    /**
     * @param player a player whose UUID is in the {@link GameStateStorageUtil} as a participant
     * @return a new {@link Participant} from the given {@link Player} object
     * @throws IllegalStateException if the given player's UUID is not a participant in the
     * {@link GameStateStorageUtil} (see {@link GameStateStorageUtil#containsPlayer(UUID)})
     */
    private @NotNull Participant createNewParticipant(@NotNull Player player) {
        String teamId = getTeamId(player.getUniqueId());
        return new Participant(player, teamId);
    }
    
    private void onAdminJoin(@NotNull Player admin) {
        onlineAdmins.add(admin);
        admin.setScoreboard(mctScoreboard);
        admin.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.empty()
                .append(Component.text("[Admin]")
                        .color(ADMIN_COLOR))
                .append(Component.text(admin.getName()));
        admin.displayName(displayName);
        admin.playerListName(displayName);
        hubManager.onAdminJoin(admin);
        tabList.showPlayer(admin);
        if (gameIsRunning()) {
            activeGame.onAdminJoin(admin);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onAdminJoin(admin);
        } else if (voteManager.isVoting()) {
            voteManager.onAdminJoin(admin);
        }
    }
    
    /**
     * Handles when a participant joins
     * @param participant the participant who joined
     */
    private void onParticipantJoin(@NotNull Participant participant) {
        Main.logger().info(String.format("GameManager.onParticipantJoin %s, team %s, teams.size()=%d", 
                participant.getName(), participant.getTeamId(), teams.size()));
        Team team = teams.get(participant.getTeamId());
        team.joinOnlineMember(participant);
        onlineParticipants.put(participant.getUniqueId(), participant);
        participant.getPlayer().setScoreboard(mctScoreboard);
        participant.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.text(participant.getName(), team.getColor());
        participant.displayName(displayName);
        participant.playerListName(displayName);
        hubManager.onParticipantJoin(participant);
        if (gameIsRunning()) {
            hubManager.removeParticipantsFromHub(Collections.singletonList(participant));
            if (team.getOnlineMembers().size() == 1) { // if this is the first participant of this team to be online
                activeGame.onTeamJoin(new Team(team));
            }
            activeGame.onParticipantJoin(participant);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            hubManager.removeParticipantsFromHub(Collections.singletonList(participant));
            eventManager.onParticipantJoin(participant);
        } else if (voteManager.isVoting()) {
            voteManager.onParticipantJoin(participant);
        }
        GameManagerUtils.colorLeatherArmor(this, participant);
        tabList.showPlayer(participant);
        tabList.setParticipantGrey(participant.getUniqueId(), false);
        updatePersonalScore(participant);
        updateTeamScore(team);
    }
    
    /**
     * Handles when a participant who's in-game-name (IGN) matches that of an OfflinePlayer in the GameState,
     * meaning they have joined the server for the first time.
     * The OfflinePlayer is transitioned to a normal participant in the game state, the player is joined to the 
     * appropriate team, and they are alerted to their new status
     * @param participant the participant who has joined for the first time
     */
    private void onOfflineIGNJoin(@NotNull Player participant) {
        String team = getOfflineIGNTeamId(participant.getName());
        if (team == null) {
            // this shouldn't happen
            return;
        }
        leaveOfflineIGN(Bukkit.getConsoleSender(), participant.getName());
        joinParticipantToTeam(plugin.getServer().getConsoleSender(), participant, team);
        messageAdmins(Component.empty()
                .append(participant.displayName())
                .append(Component.text(" was joined to "))
                .append(getFormattedTeamDisplayName(team))
                .append(Component.text(" because their IGN was listed in the GameState's offlinePlayers list.")));
    }
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
    }
    
    /**
     * @return a new sidebar. Adjusts the title based on whether an event is running. 
     */
    public Sidebar createSidebar() {
        if (eventManager.eventIsActive()) {
            return sidebarFactory.createSidebar(eventManager.getTitle());
        } else {
            return sidebarFactory.createSidebar();
        }
    }
    
    /**
     * Load the hub config
     * @throws ConfigInvalidException if the loaded config is invalid
     * @throws ConfigIOException if there are any IO errors when loading the config.
     */
    public void loadHubConfig() throws ConfigIOException, ConfigInvalidException {
        hubManager.loadConfig();
    }
    
    /**
     * Attempts to load the config for the active game. If false is returned, no change was made and nothing happens other than error messages are sent to the sender.
     * @param sender the sender
     * @return false if there is no game running, the game is not configurable, or the config could not be loaded. true if the config was loaded.
     */
    public boolean loadGameConfig(CommandSender sender) {
        if (!gameIsRunning()) {
            sender.sendMessage(Component.text("No game is running.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (!(activeGame instanceof Configurable configurable)) {
            sender.sendMessage(Component.text("This game is not configurable.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        try {
            configurable.loadConfig();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for game %s", activeGame.getType()), e);
            sender.sendMessage(Component.text("Error loading config file for ")
                    .append(Component.text(activeGame.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        sender.sendMessage(Component.text("Game config was loaded.")
                .color(NamedTextColor.GREEN));
        return true;
    }
    
    public boolean loadGameState() {
        try {
            gameStateStorageUtil.loadGameState();
        } catch (ConfigException e) {
            reportGameStateException("loading game state", e);
            return false;
        }
        gameStateStorageUtil.setupScoreboard(mctScoreboard);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.setScoreboard(mctScoreboard); // TODO: this might be redundant because participants and admins are added to the scoreboard in their respective join methods
        }
        teams.clear();
        onlineParticipants.clear();
        onlineAdmins.clear();
        // TODO: see if you can move this tab list initialization to after the team and player creation so as to make use of them in the creation of the tab list
        // TabList start
        tabList.clear();
        for (String teamId : gameStateStorageUtil.getTeamIds()) {
            String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamId);
            NamedTextColor teamColor = gameStateStorageUtil.getTeamColor(teamId);
            int teamScore = gameStateStorageUtil.getTeamScore(teamId);
            tabList.addTeam(teamId, teamDisplayName, teamColor);
            tabList.setScore(teamId, teamScore);
        }
        for (UUID uuid : gameStateStorageUtil.getPlayerUniqueIds()) {
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
            String name = getOfflineParticipantIGN(offlinePlayer);
            String teamId = gameStateStorageUtil.getPlayerTeamId(uuid);
            boolean grey = !offlinePlayer.isOnline();
            if (teamId != null) { // this will not be null, formality
                tabList.joinParticipant(uuid, name, teamId, grey);
            }
        }
        for (UUID uuid : gameStateStorageUtil.getOfflinePlayerUniqueIds()) {
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
            String name = getOfflineParticipantIGN(offlinePlayer);
            String teamId = gameStateStorageUtil.getPlayerTeamId(uuid);
            if (teamId != null) { // this will not be null, formality
                tabList.joinParticipant(uuid, name, teamId, true);
            }
        }
        // TabList stop
        for (String teamId : gameStateStorageUtil.getTeamIds()) {
            String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamId);
            NamedTextColor teamColor = gameStateStorageUtil.getTeamColor(teamId);
            List<UUID> members = gameStateStorageUtil.getParticipantUUIDsOnTeam(teamId);
            Team team = new Team(teamId, teamDisplayName, teamColor, members);
            teams.put(teamId, team);
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isAdmin(player.getUniqueId())) {
                onAdminJoin(player);
            }
            if (isParticipant(player.getUniqueId())) {
                Participant participant = createNewParticipant(player);
                onParticipantJoin(participant);
            }
        }
        return true;
    }
    
    public void saveGameState() {
        try {
            gameStateStorageUtil.saveGameState();
        } catch (ConfigException e) {
            reportGameStateException("adding score to player", e);
        }
    }
    
    /**
     * For the "/mct game vote" command. Starts the vote with the specified voting pool.
     * @param sender The sender of the command
     * @param votingPool The games to vote between
     */
    public void manuallyStartVote(@NotNull CommandSender sender, List<GameType> votingPool, int duration) {
        if (gameIsRunning()) {
            sender.sendMessage(Component.text("There is a game running. You must stop the game before you start a vote.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (editorIsRunning()) {
            sender.sendMessage(Component.text("There is an editor running. You must stop the editor before you start a vote.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        voteManager.startVote(onlineParticipants.values(), votingPool, duration, (gameType) -> startGame(gameType, sender), onlineAdmins);
    }
    
    /**
     * Cancel the vote if a vote is in progress
     */
    public void cancelVote() {
        voteManager.cancelVote();
    }
    
    /**
     * Cancel the return to hub if it's in progress
     */
    public void tearDown() {
        eventManager.cancelAllTasks();
        hubManager.tearDown();
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public void removeParticipantsFromHub(Map<UUID, Participant> participantsToRemove) {
        removeParticipantsFromHub(participantsToRemove.values());
    }
    
    public void removeParticipantsFromHub(Collection<Participant> participantsToRemove) {
        hubManager.removeParticipantsFromHub(participantsToRemove);
    }
    
    /**
     * Starts the given game
     * @param gameType The game to start
     * @param sender The sender to send messages and alerts to
     * @return true if the game started successfully, false otherwise
     */
    public boolean startGame(@NotNull GameType gameType, @NotNull CommandSender sender) {
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("Can't start a game while a vote is going on.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (gameIsRunning()) {
            sender.sendMessage(Component.text("There is already a game running. You must stop the game before you start a new one.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (editorIsRunning()) {
            sender.sendMessage(Component.text("There is an editor running. You must stop the editor before you start a game.")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return false;
        }
        
        MCTGame selectedGame = instantiateGame(gameType);
        if (selectedGame == null) {
            sender.sendMessage(Component.text("Can't find game for type " + gameType));
            return false;
        }
        
        // make sure config loads
        if (selectedGame instanceof Configurable configurable) {
            try {
                configurable.loadConfig();
            } catch (ConfigException e) {
                Main.logger().log(Level.SEVERE, String.format("Error loading config for game %s", selectedGame.getType()), e);
                Component message = Component.text("Can't start ")
                        .append(Component.text(gameType.name())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(". Error loading config file. See console for details:\n"))
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED);
                sender.sendMessage(message);
                messageAdmins(message);
                return false;
            }
        }
        
        // TODO: Team change this to get teams with online members
        Collection<Team> onlineTeams = getParticipantTeams(onlineParticipants.values());
        Main.logger().info(String.format("GameManager.startGame(): onlineTeams.size()=%d, onlineParticipants.size()=%d", onlineTeams.size(), onlineParticipants.size()));
        // make sure the player and team count requirements are met
        switch (gameType) {
            case SURVIVAL_GAMES -> {
                if (onlineTeams.size() < 2) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text(GameType.SURVIVAL_GAMES.getTitle()))
                            .append(Component.text(" doesn't end correctly unless there are 2 or more teams online. use ")
                            .append(Component.text("/mct game stop")
                                    .clickEvent(ClickEvent.suggestCommand("/mct game stop"))
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" to stop the game."))
                            .color(NamedTextColor.RED)));
                }
            }
            case CAPTURE_THE_FLAG -> {
                if (onlineTeams.size() < 2) {
                    sender.sendMessage(Component.text("Capture the Flag needs at least 2 teams online to play.").color(NamedTextColor.RED));
                    return false;
                }
            }
        }
        
        if (eventManager.eventIsActive() && eventManager.shouldDisplayGameNumber()) {
            Component newTitle = createNewTitle(selectedGame);
            selectedGame.setTitle(newTitle);
        }
        
        hubManager.removeParticipantsFromHub(onlineParticipants.values());
        selectedGame.start(Team.deepCopy(onlineTeams), onlineParticipants.values(), onlineAdmins);
        activeGame = selectedGame;
        updatePersonalScores(onlineParticipants.values());
        updateTeamScores(onlineTeams);
        return true;
    }
    
    /**
     * @param participants the participants whose teams to retrieve
     * @return a set of the teams which the collective participants are members of
     */
    public @NotNull Set<Team> getParticipantTeams(@NotNull Collection<@NotNull Participant> participants) {
        return getTeams(Participant.getTeamIds(participants));
    }
    
    /**
     * @param teamIds the teamIds of the teams to get. Ignores any invalid teamIds.
     * @return a collection of the {@link Team}s represented by the given teamIds
     */
    public @NotNull Set<Team> getTeams(@NotNull Collection<@NotNull String> teamIds) {
        return teamIds.stream().map(teams::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    
    /**
     * @return all teams
     */
    public @NotNull Collection<Team> getTeams() {
        return teams.values();
    }
    
    /**
     * @param participant the participant to get the team of
     * @return the team that the given participant belongs to. If the Participant is not in the GameManager,
     * the team may not exist
     */
    public Team getTeam(@NotNull Participant participant) {
        return teams.get(participant.getTeamId());
    }
    
    /**
     * @param teamId the teamId to get the {@link Team} for
     * @return the team with the given teamId, if one exists, null otherwise
     */
    public @Nullable Team getTeam(@NotNull String teamId) {
        return teams.get(teamId);
    }
    
    private @NotNull Component createNewTitle(MCTGame game) {
        int currentGameNumber = eventManager.getCurrentGameNumber();
        int maxGames = eventManager.getMaxGames();
        Component baseTitle = game.getBaseTitle();
        return Component.empty()
                .append(baseTitle)
                .append(Component.space())
                .append(Component.empty()
                        .append(Component.text("["))
                        .append(Component.text(currentGameNumber))
                        .append(Component.text("/"))
                        .append(Component.text(maxGames))
                        .append(Component.text("]"))
                        .color(NamedTextColor.GRAY));
    }
    
    public void updateGameTitle() {
        if (!gameIsRunning()) {
            return;
        }
        if (eventManager.eventIsActive() && eventManager.shouldDisplayGameNumber()) {
            Component newTitle = createNewTitle(activeGame);
            activeGame.setTitle(newTitle);
        }
    }
    
    /**
     * Checks if a game is currently running
     * @return True if a game is running, false if not
     */
    public boolean gameIsRunning() {
        return activeGame != null;
    }
    
    /**
     * If a game is currently going on, manually stops the game.
     * @throws NullPointerException if no game is currently running. 
     * Check if a game is running with isGameRunning()
     */
    public void manuallyStopGame(boolean shouldTeleportToHub) {
        this.shouldTeleportToHub = shouldTeleportToHub;
        activeGame.stop();
    }
    
    /**
     * Meant to be called by the active game when the game is over.
     * If an event is running, calls {@link EventManager#gameIsOver(GameType)}
     */
    public void gameIsOver() {
        if (eventManager.eventIsActive()) {
            eventManager.gameIsOver(activeGame.getType());
            activeGame = null;
            return;
        }
        activeGame = null;
        if (!shouldTeleportToHub) {
            shouldTeleportToHub = true;
            return;
        }
        hubManager.returnParticipantsToHub(onlineParticipants.values(), onlineAdmins, true);
    }
    
    public void startEditor(GameType gameType, @NotNull CommandSender sender) {
        if (voteManager.isVoting()) {
            sender.sendMessage(Component.text("Can't start a game while a vote is going on.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (gameIsRunning()) {
            sender.sendMessage(Component.text("There is a game running. You must stop the game before you start an editor.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (eventManager.eventIsActive()) {
            sender.sendMessage(Component.text("Can't start an editor while an event is going on")
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (eventManager.colossalCombatIsActive()) {
            sender.sendMessage(Component.text("Can't start an editor while colossal combat is running")
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        
        if (editorIsRunning()) {
            sender.sendMessage(Component.text("An editor is already running. You must stop it before you can start another one.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        GameEditor selectedEditor = instantiateEditor(gameType);
        if (selectedEditor == null) {
            sender.sendMessage(Component.text("Can't find editor for game type " + gameType)
                    .color(NamedTextColor.RED));
            return;
        }
        
        // make sure config loads
        try {
            selectedEditor.loadConfig();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for editor %s", selectedEditor), e);
            Component message = Component.text("Can't start ")
                    .append(Component.text(gameType.name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED);
            sender.sendMessage(message);
            messageAdmins(message);
            return;
        }
        
        selectedEditor.start(Participant.toPlayersList(onlineParticipants.values()));
        activeEditor = selectedEditor;
    }
    
    public void stopEditor(@NotNull CommandSender sender) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        activeEditor.stop();
        activeEditor = null;
    }
    
    public boolean editorIsRunning() {
        return activeEditor != null;
    }
    
    public void validateEditor(@NotNull CommandSender sender) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        try {
            activeEditor.configIsValid();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error validating config for editor %s", activeEditor.getType()), e);
            sender.sendMessage(Component.text("Config is not valid for ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Config is valid.")
                .color(NamedTextColor.GREEN));
    }
    
    /**
     * @param sender the sender
     * @param force if true, validation will be skipped and the config will be saved even if invalid, if false the config will only save if it is valid
     */
    public void saveEditor(@NotNull CommandSender sender, boolean force) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!force) {
            try {
                activeEditor.configIsValid();
            } catch (ConfigException e) {
                Main.logger().log(Level.SEVERE, String.format("Error validating config for editor %s", activeEditor.getType()), e);
                sender.sendMessage(Component.text("Config is not valid for ")
                        .append(Component.text(activeEditor.getType().name())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(". See console for details:\n"))
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED));
                sender.sendMessage(Component.text("Skipping save. If you wish to force the save, use ")
                        .append(Component.text("/mct edit save true")
                                .clickEvent(ClickEvent.suggestCommand("/mct edit save true"))
                                .decorate(TextDecoration.BOLD))
                        .color(NamedTextColor.RED));
                return;
            }
        } else {
            sender.sendMessage("Skipping validation.");
        }
        try {
            activeEditor.saveConfig();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error saving config for editor %s", activeEditor.getType()), e);
            sender.sendMessage(Component.text("An error occurred while attempting to save the config for ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Config is saved.")
                .color(NamedTextColor.GREEN));
    }
    
    public void loadEditor(@NotNull CommandSender sender) {
        try {
            activeEditor.loadConfig();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for editor %s", activeEditor.getType()), e);
            Component message = Component.text("Can't start ")
                    .append(Component.text(activeEditor.getType().name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED);
            sender.sendMessage(message);
            return;
        }
        sender.sendMessage(Component.text("Config loaded.")
                .color(NamedTextColor.GREEN));
    }
    
    public void returnAllParticipantsToHub() {
        hubManager.returnParticipantsToHub(onlineParticipants.values(), onlineAdmins, false);
    }
    
    /**
     * Instantly returns the given participant to the hub
     * @param participant the participant to be returned to the hub
     */
    public void returnParticipantToHubInstantly(Participant participant) {
        hubManager.returnParticipantsToHub(Collections.singletonList(participant), Collections.emptyList(), false);
    }
    
    public void returnAllParticipantsToPodium(String winningTeam) {
        List<Participant> winningTeamParticipants = getOnlineParticipantsOnTeam(winningTeam);
        List<Participant> otherParticipants = new ArrayList<>();
        for (Participant participant : onlineParticipants.values()) {
            if (!winningTeamParticipants.contains(participant)) {
                otherParticipants.add(participant);
            }
        }
        hubManager.sendAllParticipantsToPodium(winningTeamParticipants, otherParticipants, onlineAdmins);
    }
    
    /**
     * @param participant the participant
     * @param winner whether they are a winner
     */
    public void returnParticipantToPodium(Participant participant, boolean winner) {
        hubManager.sendParticipantToPodium(participant, winner);
    }
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
    /**
     * Remove the given team from the game
     * @param sender   the sender of the command, who will receive success/error messages
     * @param teamId The internal name of the team to remove
     */
    public void removeTeam(CommandSender sender, String teamId) {
        Team team = teams.remove(teamId);
        if (team == null) {
            sender.sendMessage(Component.text("Team ")
                    .append(Component.text(teamId))
                    .append(Component.text(" does not exist."))
                    .color(NamedTextColor.RED));
            return;
        }
        leavePlayersOnTeam(sender, teamId);
        tabList.removeTeam(teamId);
        try {
            gameStateStorageUtil.removeTeam(teamId);
            sender.sendMessage(Component.text("Removed team ")
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(".")));
        } catch (ConfigIOException e) {
            reportGameStateException("removing team", e);
            sender.sendMessage(Component.text("error occurred removing team, see console for details.")
                    .color(NamedTextColor.RED));
        }
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(teamId);
        if (scoreboardTeam != null) {
            scoreboardTeam.unregister();
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
    }
    
    private void leavePlayersOnTeam(CommandSender sender, String teamId) {
        Collection<UUID> memberUUIDs = teams.get(teamId).getMemberUUIDs();
        for (UUID uuid : memberUUIDs) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(uuid);
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "unknown";
            leaveParticipant(sender, offlinePlayer, name);
        }
        // TODO: OfflineParticipant great place to use this
        List<String> offlineIGNs = gameStateStorageUtil.getOfflineIGNsOnTeam(teamId);
        for (String offlineIGN : offlineIGNs) {
            leaveOfflineIGN(sender, offlineIGN);
        }
    }
    
    /**
     * Add a team to the game.
     * @param teamId The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     */
    public void addTeam(String teamId, String teamDisplayName, String colorString) {
        if (gameStateStorageUtil.containsTeam(teamId)) {
            return;
        }
        try {
            gameStateStorageUtil.addTeam(teamId, teamDisplayName, colorString);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to player", e);
        }
        
        NamedTextColor color = ColorMap.getNamedTextColor(colorString);
        Team team = new Team(teamId, teamDisplayName, color);
        teams.put(teamId, team);
        
        org.bukkit.scoreboard.Team newTeam = mctScoreboard.registerNewTeam(teamId);
        newTeam.displayName(Component.text(teamDisplayName));
        newTeam.color(color);
        tabList.addTeam(teamId, teamDisplayName, color);
        updateTeamScore(team);
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
     * Checks if the team exists in the game state
     * @param teamId The team to look for
     * @return true if the team with the given teamId exists, false otherwise.
     * @deprecated in favor of checking if {@link #getTeam(String)} is non-null
     */
    @Deprecated
    public boolean hasTeam(String teamId) {
        // TODO: Team consider making this check the teams map
        return gameStateStorageUtil.containsTeam(teamId);
    }
    
    /**
     * Checs if the player exists in the game state
     * @param playerUniqueId The UUID of the player to check for
     * @return true if the UUID is in the game state, false otherwise
     */
    public boolean isParticipant(UUID playerUniqueId) {
        return gameStateStorageUtil.containsPlayer(playerUniqueId);
    }
    
    /**
     * @param offlineUUID the UUID of the offline player which may be in the GameState
     * @return true if the given UUID matches one of the offline players in the GameState, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOfflineParticipant(UUID offlineUUID) {
        return gameStateStorageUtil.containsOfflinePlayer(offlineUUID);
    }
    
    /**
     * @param ign the in-game-name of the OfflineParticipant
     * @return true if the ign is that of an OfflineParticipant (one who has never logged in before), false otherwise
     */
    public boolean isOfflineIGN(@NotNull String ign) {
        return gameStateStorageUtil.containsOfflineIGN(ign);
    }
    
    /**
     * Joins the given player to the team with the given teamId. If the player was on a team already (not teamId) they will be removed from that team and added to the other team. 
     * Note, this will not join a player to a team if that player is an admin. 
     * @param sender the sender of the command, who will receive success/error messages
     * @param participant The player to join to the given team
     * @param teamId The internal teamId of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinParticipantToTeam(CommandSender sender, Player participant, String teamId) {
        UUID uuid = participant.getUniqueId();
        if (isAdmin(uuid)) {
            removeAdmin(sender, participant, participant.getName());
        }
        if (isParticipant(uuid)) {
            String originalTeamId = getTeamId(uuid);
            if (originalTeamId.equals(teamId)) {
                sender.sendMessage(Component.text()
                        .append(Component.text(participant.getName())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is already a member of team "))
                        .append(Component.text(teamId))
                        .append(Component.text(". Nothing happened.")));
                return;
            }
            leaveParticipant(sender, participant, participant.getName());
        }
        Team team = teams.get(teamId);
        if (team == null) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(teamId).decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid teamId")));
            return;
        }
        tabList.joinParticipant(participant.getUniqueId(), participant.getName(), teamId, false);
        addNewParticipant(sender, uuid, team);
        hubManager.updateLeaderboards();
        Component displayName = Component.text(participant.getName(), team.getColor());
        participant.displayName(displayName);
        participant.playerListName(displayName);
        participant.sendMessage(Component.text("You've been joined to team ")
                .append(team.getFormattedDisplayName()));
        sender.sendMessage(Component.text("Joined ")
                .append(displayName
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to team "))
                .append(team.getFormattedDisplayName()));
    }
    
    /**
     * @param sender the sender of the command, who will receive success/error messages
     * @param ign The in-game-name of the participant who has never logged in before, which you are joining to the given team
     * @param offlineUUID nullable. The UUID of the offline player with the ign, if you know it. 
     * @param teamId the teamId of the team to join the participant to. Nothing happens if this is not a valid teamId
     */
    public void joinOfflineIGNToTeam(CommandSender sender, @NotNull String ign, @NotNull UUID offlineUUID, @NotNull String teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            return;
        }
        if (isAdmin(offlineUUID)) {
            OfflinePlayer offlineAdmin = plugin.getServer().getOfflinePlayer(offlineUUID);
            removeAdmin(sender, offlineAdmin, ign);
        }
        // TODO: OfflineParticipant this is a great place for it
        if (isParticipant(offlineUUID)) {
            String originalTeamId = getTeamId(offlineUUID);
            if (originalTeamId.equals(teamId)) {
                sender.sendMessage(Component.text()
                        .append(Component.text(ign)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is already a member of team "))
                        .append(Component.text(teamId))
                        .append(Component.text(". Nothing happened.")));
                return;
            }
            OfflinePlayer offlineParticipant = Bukkit.getOfflinePlayer(offlineUUID);
            leaveParticipant(sender, offlineParticipant, ign);
        }
        if (isOfflineIGN(ign)) {
            String originalTeamId = getOfflineIGNTeamId(ign);
            if (originalTeamId != null) {
                if (originalTeamId.equals(teamId)) {
                    sender.sendMessage(Component.text()
                            .append(Component.text(ign)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is already a member of team "))
                            .append(Component.text(teamId))
                            .append(Component.text(". Nothing happened.")));
                    return;
                }
                leaveOfflineIGN(sender, ign);
            }
        }
        addNewOfflineIGN(sender, ign, offlineUUID, team);
        hubManager.updateLeaderboards();
        tabList.joinParticipant(offlineUUID, ign, teamId, true);
        Component teamDisplayName = getFormattedTeamDisplayName(teamId);
        TextComponent displayName = Component.text(ign)
                .color(team.getColor())
                .decorate(TextDecoration.BOLD);
        sender.sendMessage(Component.text("Joined ")
                .append(displayName)
                .append(Component.text(" to team "))
                .append(teamDisplayName)
                .append(Component.newline())
                .append(Component.empty()
                    .append(displayName)
                    .append(Component.text(" is offline, and will be joined to their team when they log on"))
                    .decorate(TextDecoration.ITALIC))
        );
    }
    
    /**
     * Adds the new participant to the {@link GameStateStorageUtil} and joins them the given team. 
     * If a game is running, and the player is online, joins the player to that game.  
     * @param sender the sender of the command, who will receive success/error messages
     * @param uuid The UUID of the participant to add
     * @param team The team to join the new player to
     */
    private void addNewParticipant(CommandSender sender, UUID uuid, Team team) {
        try {
            gameStateStorageUtil.addNewPlayer(uuid, team.getTeamId());
        } catch (ConfigIOException e) {
            reportGameStateException("adding new player", e);
            sender.sendMessage(Component.text("error occurred adding new player, see console for details.")
                    .color(NamedTextColor.RED));
        }
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(team.getTeamId());
        OfflinePlayer newPlayer = Bukkit.getOfflinePlayer(uuid);
        if (scoreboardTeam != null) {
            scoreboardTeam.addPlayer(newPlayer);
        } else {
            Main.logger().warning(String.format("Something is wrong with the team Scoreboard. Could not find team with name %s\"", team.getTeamId()));
        }
        team.joinMember(uuid);
        Player onlineNewPlayer = newPlayer.getPlayer();
        if (onlineNewPlayer != null) {
            Participant participant = createNewParticipant(onlineNewPlayer);
            onParticipantJoin(participant);
        }
    }
    
    /**
     * Adds the new offline IGN to the game state and joins them the given team. 
     * @param sender the sender of the command, who will receive success/error messages
     * @param ign The in-game-name of the participant who has never logged in before
     * @param offlineUUID nullable. The UUID of the offline player with the ign, if you know it.
     * @param team The team to join the new offline ign to. 
     */
    private void addNewOfflineIGN(CommandSender sender, @NotNull String ign, @NotNull UUID offlineUUID, Team team) {
        try {
            gameStateStorageUtil.addNewOfflineIGN(ign, offlineUUID, team.getTeamId());
        } catch (ConfigIOException e) {
            reportGameStateException("adding new offline IGN", e);
            sender.sendMessage(Component.text("error occurred adding new offline IGN, see console for details.")
                    .color(NamedTextColor.RED));
        }
        team.joinMember(offlineUUID);
    }
    
    /**
     * @param teamId the teamId 
     * @return a list of the online participants who are on the given team
     * @deprecated in favor of {@link Team#getOnlineMembers()}
     */
    @Deprecated
    public List<Participant> getOnlineParticipantsOnTeam(String teamId) {
        // TODO: Team remove this method
        return Participant.getParticipantsOnTeam(onlineParticipants.values(), teamId);
    }
    
    /**
     * @param teamId the team to get the members of
     * @return the UUIDs of the players on that team
     * @deprecated in favor of {@link Team#getMemberUUIDs()}
     */
    @Deprecated
    public List<UUID> getParticipantUUIDsOnTeam(String teamId) {
        return gameStateStorageUtil.getParticipantUUIDsOnTeam(teamId);
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well. 
     * @param sender the sender of the command, who will receive success/error messages
     * @param offlinePlayer The player to remove from the team
     */
    public void leaveParticipant(CommandSender sender, @NotNull OfflinePlayer offlinePlayer, @NotNull String playerName) {
        // TODO: Participant this is a good place to replace for OfflineParticipant, because you would have a reference to the teamId
        UUID playerUniqueId = offlinePlayer.getUniqueId();
        String teamId = gameStateStorageUtil.getPlayerTeamId(playerUniqueId);
        Main.logger().info(String.format("leaveParticipant(%s) from %s", playerName, teamId));
        if (teamId == null) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("Could not find team for UUID "))
                    .append(Component.text(playerUniqueId.toString())
                            .decorate(TextDecoration.BOLD)
                            .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                            .clickEvent(ClickEvent.copyToClipboard(playerUniqueId.toString()))
                    )
            );
            return;
        }
        Team team = teams.get(teamId);
        team.leaveMember(offlinePlayer.getUniqueId());
        if (offlinePlayer.isOnline()) { // TODO: Team replace this check with `isOnlineMember()` or something
            Participant participant = onlineParticipants.get(offlinePlayer.getUniqueId());
            if (participant != null) {
                onParticipantQuit(participant);
                participant.sendMessage(Component.text("You've been removed from ")
                        .append(team.getFormattedDisplayName()));
            }
        }
        try {
            gameStateStorageUtil.leavePlayer(playerUniqueId);
        } catch (ConfigIOException e) {
            reportGameStateException("leaving player", e);
            sender.sendMessage(Component.text("error occurred leaving player, see console for details.")
                    .color(NamedTextColor.RED));
        }
        hubManager.updateLeaderboards();
        tabList.leaveParticipant(playerUniqueId);
        sender.sendMessage(Component.text("Removed ")
                .append(Component.text(playerName)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" from team "))
                .append(team.getFormattedDisplayName()));
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(teamId);
        if (scoreboardTeam != null) {
            scoreboardTeam.removePlayer(offlinePlayer);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\"", teamId));
        }
    }
    
    /**
     * Leaves the offline IGN from the team and removes them from the game state.
     * @param sender the sender of the command, who will receive success/error messages
     * @param ign the in-game-name of a participant who has never logged on. If this is not
     *            in the {@link GameStateStorageUtil}, nothing happens
     */
    public void leaveOfflineIGN(CommandSender sender, @NotNull String ign) {
        UUID uuid = gameStateStorageUtil.getOfflineIGNUniqueId(ign);
        if (uuid == null) {
            return;
        }
        try {
            gameStateStorageUtil.leaveOfflineIGN(ign);
        } catch (ConfigIOException e) {
            reportGameStateException("leaving offline IGN", e);
            sender.sendMessage(Component.text("error occurred leaving offline IGN, see console for details.")
                    .color(NamedTextColor.RED));
        }
        String teamId = gameStateStorageUtil.getOfflineIGNTeamId(ign);
        Team team = teams.get(teamId);
        if (team == null) {
            return;
        }
        team.leaveMember(uuid);
        hubManager.updateLeaderboards();
        tabList.leaveParticipant(uuid);
        TextComponent displayName = Component.text(ign)
                .decorate(TextDecoration.BOLD);
        sender.sendMessage(Component.text("Removed ")
                .append(displayName)
                .append(Component.text(" from team "))
                .append(team.getFormattedDisplayName())
                .append(Component.newline())
                .append(Component.empty()
                        .append(displayName)
                        .append(Component.text(" is offline"))
                        .decorate(TextDecoration.ITALIC)
                )
        );
    }
    
    /**
     * Gets the teamId of the participant with the given UUID
     * @param participantUUID The UUID of the participant to find the team of
     * @return The teamId of the player with the given UUID
     * @throws IllegalStateException if the {@link GameStateStorageUtil} doesn't contain the given UUID
     * @deprecated in favor of {@link Participant#getTeamId()}
     */
    @Contract("null -> null")
    @Deprecated
    public String getTeamId(UUID participantUUID) {
        String teamId = gameStateStorageUtil.getPlayerTeamId(participantUUID);
        if (teamId == null) {
            throw new IllegalStateException(
                    String.format("Can't get teamId for non-participant UUID %s", participantUUID));
        }
        return teamId;
    }
    
    /**
     * @param ign the in-game-name of a participant who has never logged in before
     * @return the teamId of the OfflineParticipant with the given ign. Null if the ign doesn't exist in the GameState
     */
    public @Nullable String getOfflineIGNTeamId(@NotNull String ign) {
        return gameStateStorageUtil.getOfflineIGNTeamId(ign);
    }
    
    /**
     * @param uniqueId the UUID of the offline IGN to get
     * @return the in-game-name of the offlinePlayer in the GameState with the given UUID. Null if the UUID doesn't belong to an offline player 
     */
    public @Nullable String getOfflineIGN(@NotNull UUID uniqueId) {
        return gameStateStorageUtil.getOfflineIGN(uniqueId);
    }
    
    /**
     * Awards the same number of points to each participant in the collection and their respective teams.
     * Also announces to the participants how many points they received.
     * <br>
     * This is used in replacement of looping through each participant and calling 
     * {@link #awardPointsToParticipant(Participant, int)} to reduce the number of changes to the {@link TabList}
     * and file writes to the {@link org.braekpo1nt.mctmanager.games.gamestate.GameState} at once.
     * @param participants must be a list of valid, online participants. For performance reasons, this is not
     *                     checked. You are relied upon to provide only valid participants.
     * @param points the points to award to each participant
     */
    public void awardPointsToParticipants(Collection<Participant> participants, int points) {
        int multipliedPoints = (int) (points * eventManager.matchProgressPointMultiplier());
        Collection<Team> awardedTeams = getParticipantTeams(participants);
        if (activeGame != null) {
            eventManager.trackPointsParticipants(participants, points, activeGame.getType());
            eventManager.trackPointsTeams(Team.getTeamIds(awardedTeams), multipliedPoints, activeGame.getType());
        }
        addScoreParticipants(participants, points);
        addScoreTeams(awardedTeams, multipliedPoints);
        
        Audience.audience(participants).sendMessage(Component.text("+")
                .append(Component.text(multipliedPoints))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
    }
    
    /**
     * Awards points to the participant and their team and announces to that participant how many points they received. 
     * If the participant does not exist, nothing happens.
     * @param participant The participant to award points to
     * @param points The points to award to the participant
     */
    public void awardPointsToParticipant(Participant participant, int points) {
        UUID uuid = participant.getUniqueId();
        if (!gameStateStorageUtil.containsPlayer(uuid)) {
            return;
        }
        String teamId = gameStateStorageUtil.getPlayerTeamId(uuid);
        double multiplier = eventManager.matchProgressPointMultiplier();
        int multipliedPoints = (int) (points * multiplier);
        addScore(uuid, points);
        addScore(teamId, multipliedPoints);
        if (activeGame != null) {
            eventManager.trackPoints(uuid, points, activeGame.getType());
            eventManager.trackPoints(teamId, multipliedPoints, activeGame.getType());
        }
        participant.sendMessage(Component.text("+")
                .append(Component.text(multipliedPoints))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
    }
    
    /**
     * Adds the given points to the given teams, and announces to the teammates how many
     * points they earned. 
     * <br>
     * This is to be used instead of looping through each team and calling
     * {@link #awardPointsToTeam(Team, int)} to reduce the number of modifications to the
     * {@link TabList} and file writes to the {@link org.braekpo1nt.mctmanager.games.gamestate.GameState} at once.
     * @param awardedTeams the teamIds of the teams to give the points to. If this is empty, nothing happens.
     * @param points the points to give to each team
     */
    public void awardPointsToTeams(@NotNull Collection<@NotNull Team> awardedTeams, int points) {
        if (awardedTeams.isEmpty()) {
            return;
        }
        int multipliedPoints = (int) (points * eventManager.matchProgressPointMultiplier());
        addScoreTeams(awardedTeams, multipliedPoints);
        if (activeGame != null) {
            eventManager.trackPointsTeams(Team.getTeamIds(awardedTeams), multipliedPoints, activeGame.getType());
        }
        for (Team team : awardedTeams) {
            team.sendMessage(Component.text("+")
                    .append(Component.text(multipliedPoints))
                    .append(Component.text(" points for "))
                    .append(team.getFormattedDisplayName())
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.GOLD));
        }
    }
    
    /**
     * Adds the given points to the given team, and announces to all online members of that 
     * team how many points the team earned.
     * If the team doesn't exist, nothing happens. 
     * @param team The team to add points to
     * @param points The points to add
     */
    public void awardPointsToTeam(Team team, int points) {
        if (!gameStateStorageUtil.containsTeam(team.getTeamId())) {
            return;
        }
        int multipliedPoints = (int) (points * eventManager.matchProgressPointMultiplier());
        addScore(team, multipliedPoints);
        if (activeGame != null) {
            eventManager.trackPoints(team.getTeamId(), multipliedPoints, activeGame.getType());
        }
        
        team.sendMessage(Component.text("+")
                    .append(Component.text(multipliedPoints))
                    .append(Component.text(" points for "))
                    .append(team.getFormattedDisplayName())
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.GOLD));
    }
    
    /**
     * Gets the team's display name as a Component with the team's text color
     * and in bold
     * @param teamId The internal name of the team
     * @return A Component with the formatted team dislay name
     * @deprecated in favor of {@link Team#getFormattedDisplayName()}
     */
    @Deprecated
    public @NotNull Component getFormattedTeamDisplayName(@NotNull String teamId) {
        // TODO: Team delete this method
        String displayName = gameStateStorageUtil.getTeamDisplayName(teamId);
        NamedTextColor teamColor = gameStateStorageUtil.getTeamColor(teamId);
        return Component.text(displayName).color(teamColor).decorate(TextDecoration.BOLD);
    }
    
    /**
     * @param offlineParticipant the OfflinePlayer with the UUID of a valid participant.
     *                           If the OfflinePlayer's name is null, then the
     *                           UUID will be used as the name. 
     * @return the name of the participant represented by the given offline participant
     */
    public String getOfflineParticipantIGN(@NotNull OfflinePlayer offlineParticipant) {
        String name = offlineParticipant.getName();
        UUID uuid = offlineParticipant.getUniqueId();
        if (name == null) {
            String ign = gameStateStorageUtil.getOfflineIGN(uuid);
            if (ign != null) {
                return ign;
            }
            return uuid.toString();
        }
        return name;
    }
    
    /**
     * @return a copy of the list of online participants. Modifying this will not change
     *      * the online participants
     */
    public @NotNull Collection<Participant> getOnlineParticipants() {
        return onlineParticipants.values();
    }
    
    /**
     * @param uuid the UUID of the participant to get
     * @return the Participant with the given UUID, if they are online
     */
    public @Nullable Participant getOnlineParticipant(UUID uuid) {
        return onlineParticipants.get(uuid);
    }
    
    /**
     * @return a copy of the list of online admins. Modifying this will not change the online admins. 
     */
    public List<Player> getOnlineAdmins() {
        return new ArrayList<>(onlineAdmins);
    }
    
    /**
     * Gets all the names of the participants in the game state, regardless of 
     * whether they're offline or online. 
     * @return a list of the names of all participants in the game state
     */
    public List<@NotNull String> getAllParticipantNames() {
        List<OfflinePlayer> offlinePlayers = getOfflineParticipants();
        List<@NotNull String> playerNames = new ArrayList<>();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            String name = offlinePlayer.getName();
            if (name != null){
                playerNames.add(name);
            } else {
                String ign = gameStateStorageUtil.getOfflineIGN(offlinePlayer.getUniqueId());
                if (ign != null) {
                    playerNames.add(ign);
                } else {
                    playerNames.add(offlinePlayer.getUniqueId().toString());
                }
            }
        }
        return playerNames;
    }
    
    /**
     * @return A list {@link OfflinePlayer}s representing all participants in the {@link GameStateStorageUtil}. 
     * These players could be offline or online, have logged in at least once or not
     */
    public List<OfflinePlayer> getOfflineParticipants() {
        List<UUID> uniqueIds = gameStateStorageUtil.getPlayerUniqueIds();
        List<OfflinePlayer> offlineParticipants = new ArrayList<>();
        for (UUID uniqueId : uniqueIds) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(uniqueId);
            offlineParticipants.add(offlinePlayer);
        }
        List<UUID> offlineUniqueIds = gameStateStorageUtil.getOfflinePlayerUniqueIds();
        for (UUID offlineUniqueId : offlineUniqueIds) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(offlineUniqueId);
            offlineParticipants.add(offlinePlayer);
        }
        return offlineParticipants;
    }
    
    /**
     * @param teamId the teamId to get the members of
     * @return A list {@link OfflinePlayer}s representing all participants in the {@link GameStateStorageUtil}
     * who are on the given team. 
     * These players could be offline or online, have logged in at least once or not
     */
    public List<OfflinePlayer> getOfflineParticipants(@NotNull String teamId) {
        List<OfflinePlayer> offlineParticipants = getOfflineParticipants();
        List<OfflinePlayer> result = new ArrayList<>();
        for (OfflinePlayer offlineParticipant : offlineParticipants) {
            String offlineTeamId = getTeamId(offlineParticipant.getUniqueId());
            if (teamId.equals(offlineTeamId)) {
                result.add(offlineParticipant);
            }
        }
        return result;
    }
    
    public void addScoreParticipants(Collection<Participant> participants, int score) {
        try {
            gameStateStorageUtil.addScorePlayers(participants.stream().map(Participant::getUniqueId).toList(), score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            updatePersonalScores(participants);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to players", e);
        }
    }
    
    /**
     * Adds the given score to the participant with the given UUID
     * @param participantUUID The UUID of the participant to add the score to
     * @param score The score to add. Could be positive or negative.
     */
    public void addScore(UUID participantUUID, int score) {
        try {
            gameStateStorageUtil.addScore(participantUUID, score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            Participant participant = onlineParticipants.get(participantUUID);
            if (participant != null) {
                updatePersonalScore(participant);
            }
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to player", e);
        }
    }
    
    /**
     * Adds the given score to each of the given teams
     * <br>
     * Use this instead of calling {@link #addScore(String, int)} for each teamId because this is more
     * efficient in that it only writes to the {@link org.braekpo1nt.mctmanager.games.gamestate.GameState} once
     * @param updateTeams the teamIds to add the score to. Must all be valid teamIds.
     * @param score the score to add. Could be positive or negative.
     */
    private void addScoreTeams(Collection<Team> updateTeams, int score) {
        Set<String> teamIds = Team.getTeamIds(updateTeams);
        try {
            gameStateStorageUtil.addScoreTeams(teamIds, score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            updateTeamScores(updateTeams);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to teams", e);
        }
    }
    
    /**
     * Adds the given score to the given team
     * @param teamId The team to add the score to
     * @param score The score to add. Could be positive or negative.
     * @deprecated in favor of {@link #addScore(Team, int)}
     */
    @Deprecated
    public void addScore(@NotNull String teamId, int score) {
        // TODO: Team remove this method
        Team team = teams.get(teamId);
        if (team == null) {
            return;
        }
        addScore(team, score);
    }
    
    /**
     * Adds the given score to the given team
     * @param team The team to add the score to
     * @param score The score to add. Could be positive or negative.
     */
    public void addScore(@NotNull Team team, int score) {
        try {
            gameStateStorageUtil.addScore(team.getTeamId(), score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            updateTeamScore(team);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to team", e);
        }
    }
    
    /**
     * Sets the score of the participant with the given UUID to the given value
     * @param participantUUID The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScore(UUID participantUUID, int score) {
        // TODO: OfflineParticipant great place for using the OfflineParticipant.getTeamId()
        Team team = teams.get(getTeamId(participantUUID));
        if (team == null) {
            return;
        }
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(participantUUID, 0);
                return;
            }
            gameStateStorageUtil.setScore(participantUUID, score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            Participant participant = onlineParticipants.get(participantUUID);
            if (participant != null) {
                updatePersonalScore(participant);
            }
            updateTeamScore(team);
        } catch (ConfigIOException e) {
            reportGameStateException("setting a player's score", e);
        }
    }
    
    /**
     * Sets the score of the team with the given name to the given value
     * @param teamId The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScore(String teamId, int score) {
        Team team = teams.get(teamId);
        if (team == null) {
            return;
        }
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(teamId, 0);
                return;
            }
            gameStateStorageUtil.setScore(teamId, score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            updateTeamScore(team);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to team", e);
        }
    }
    
    /**
     * Set all the teams and players scores to the given score
     * @param score the score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScoreAll(int score) {
        try {
            if (score < 0) {
                gameStateStorageUtil.setAllScores(0);
                return;
            }
            gameStateStorageUtil.setAllScores(score);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    gameStateStorageUtil.saveGameState());
            updatePersonalScores(onlineParticipants.values());
            updateTeamScores(teams.values());
        } catch (ConfigIOException e) {
            reportGameStateException("setting all scores", e);
        }
    }
    
    /**
     * Gets the score of the given team
     * @param teamId The team to get the score of
     * @return The score of the given team
     */
    public int getScore(String teamId) {
        return gameStateStorageUtil.getTeamScore(teamId);
    }
    
    /**
     * Gets the score of the participant with the given UUID
     * @param participantUniqueId The UUID of the participant to get the score of
     * @return The score of the participant with the given UUID
     */
    public int getScore(UUID participantUniqueId) {
        return gameStateStorageUtil.getParticipantScore(participantUniqueId);
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
     * @param sender the sender of the command, who will receive success/error messages
     * @param newAdmin The player to add
     */
    public void addAdmin(@NotNull CommandSender sender, Player newAdmin) {
        UUID uniqueId = newAdmin.getUniqueId();
        if (gameStateStorageUtil.isAdmin(uniqueId)) {
            sender.sendMessage(Component.text()
                    .append(Component.text(newAdmin.getName())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is already an admin. Nothing happened.")));
            return;
        }
        if (gameStateStorageUtil.containsPlayer(uniqueId)) {
            leaveParticipant(sender, newAdmin, newAdmin.getName());
        }
        try {
            gameStateStorageUtil.addAdmin(uniqueId);
        } catch (ConfigIOException e) {
            reportGameStateException("adding new admin", e);
            sender.sendMessage(Component.text("error occurred adding new admin, see console for details.")
                    .color(NamedTextColor.RED));
        }
        sender.sendMessage(Component.empty()
                .append(Component.text("Added "))
                .append(Component.text(newAdmin.getName())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" as an admin")));
        org.bukkit.scoreboard.Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
        Preconditions.checkState(adminTeam != null, "mctScoreboard could not find team \"%s\"", ADMIN_TEAM);
        adminTeam.addPlayer(newAdmin);
        if (newAdmin.isOnline()) {
            newAdmin.sendMessage(Component.text("You were added as an admin"));
            onAdminJoin(newAdmin);
        }
    }
    
    /**
     * Removes the given player from the admins
     * @param sender the sender of the command, who will receive success/error messages
     * @param offlineAdmin The admin to remove
     */
    public void removeAdmin(@NotNull CommandSender sender, @NotNull OfflinePlayer offlineAdmin, @NotNull String adminName) {
        if (!isAdmin(offlineAdmin.getUniqueId())) {
            sender.sendMessage(Component.text()
                    .append(Component.text(adminName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an admin. Nothing happened.")));
            return;
        }
        if (offlineAdmin.isOnline()) {
            Player onlineAdmin = offlineAdmin.getPlayer();
            if (onlineAdmin != null) {
                onlineAdmin.sendMessage(Component.text("You were removed as an admin"));
                onAdminQuit(onlineAdmin);
            }
        }
        UUID adminUniqueId = offlineAdmin.getUniqueId();
        try {
            gameStateStorageUtil.removeAdmin(adminUniqueId);
        } catch (ConfigIOException e) {
            reportGameStateException("removing admin", e);
            sender.sendMessage(Component.text("error occurred removing admin, see console for details.")
                    .color(NamedTextColor.RED));
        }
        sender.sendMessage(Component.empty()
                .append(Component.text(adminName)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" is no longer an admin")));
        org.bukkit.scoreboard.Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
        Preconditions.checkState(adminTeam != null, "mctScoreboard could not find team \"%s\"", ADMIN_TEAM);
        adminTeam.removePlayer(offlineAdmin);
    }
    
    @Deprecated
    public Material getTeamPowderColor(@NotNull String teamId) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamId);
        return ColorMap.getConcretePowderColor(colorString);
    }
    
    @Deprecated
    public Material getTeamConcreteColor(@NotNull String teamId) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamId);
        return ColorMap.getConcreteColor(colorString);
    }
    
    @Deprecated
    public Material getTeamStainedGlassColor(@NotNull String teamId) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamId);
        return ColorMap.getStainedGlassColor(colorString);
    }
    
    /**
     * @deprecated in favor of {@link Team#getColor()}
     */
    @Deprecated
    public @NotNull TextColor getTeamColor(@NotNull String teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            return NamedTextColor.WHITE;
        }
        return team.getColor();
    }
    
    @Deprecated
    public Material getTeamBannerColor(@NotNull String teamId) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamId);
        return ColorMap.getBannerColor(colorString);
    }
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        hubManager.setBoundaryEnabled(boundaryEnabled);
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
    
    // Test methods
    
    public void setGameStateStorageUtil(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
    }
    
    private void reportGameStateException(String attemptedOperation, ConfigException e) {
        LOGGER.severe(String.format("error while %s. See console log for error message.", attemptedOperation));
        messageAdmins(Component.empty()
                .append(Component.text("error while "))
                .append(Component.text(attemptedOperation))
                .append(Component.text(". See console log for error message.")));
        throw new RuntimeException(e);
    }
    
    public MCTGame getActiveGame() {
        return activeGame;
    }
    
    public void setSidebarFactory(SidebarFactory sidebarFactory) {
        this.sidebarFactory = sidebarFactory;
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
    
    /**
     * Update the displays of team scores to reflect the current 
     * {@link org.braekpo1nt.mctmanager.games.gamestate.GameState}
     * <br>
     * This includes the header of the current active game, the event sidebar in the hub, and the {@link TabList}
     * @param updateTeams the teamIds to update all displays for
     */
    private void updateTeamScores(Collection<Team> updateTeams) {
        Map<String, Integer> teamIdsToScores = new HashMap<>(updateTeams.size());
        // perform this check and cast one time instead of for each teamId
        Headerable headerable = activeGame instanceof Headerable ? (Headerable) activeGame : null;
        for (Team team : updateTeams) {
            int teamScore = getScore(team.getTeamId());
            teamIdsToScores.put(team.getTeamId(), teamScore);
            if (headerable != null) {
                for (Participant participant : team.getOnlineMembers()) {
                    headerable.updateTeamScore(participant, Component.empty()
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(": "))
                            .append(Component.text(teamScore)
                                    .color(NamedTextColor.GOLD))
                    );
                }
            }
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
        hubManager.updateLeaderboards();
        // update all the scores at once instead of one at a time for each teamId in the above loop
        tabList.setScores(teamIdsToScores);
    }
    
    /**
     * Update the displays of the given team to reflect the current score.
     * <br>
     * This includes the header of the current active game, the event sidebar in the hub, and the {@link TabList} 
     * @param team the teamId to update in all displays
     */
    private void updateTeamScore(@NotNull Team team) {
        int teamScore = getScore(team.getTeamId());
        if (activeGame != null && activeGame instanceof Headerable headerable) {
            for (Participant participant : team.getOnlineMembers()) {
                headerable.updateTeamScore(participant, Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(": "))
                        .append(Component.text(teamScore)
                                .color(NamedTextColor.GOLD))
                );
            }
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
        hubManager.updateLeaderboards();
        tabList.setScore(team.getTeamId(), teamScore);
    }
    
    /**
     * Update the displays of the given participants to reflect their current scores.
     * Also updates their team scores.
     * @param participants the participants to update
     */
    private void updatePersonalScores(Collection<Participant> participants) {
        // perform this check and cast one time instead of for each player
        Headerable headerable = activeGame instanceof Headerable ? (Headerable) activeGame : null;
        for (Participant participant : participants) {
            int score = getScore(participant.getUniqueId());
            Component contents = Component.empty()
                    .append(Component.text("Personal: "))
                    .append(Component.text(score))
                    .color(NamedTextColor.GOLD);
            // TODO: replace this loop with bulk operation update methods
            if (headerable != null) {
                headerable.updatePersonalScore(participant, contents);
            }
            if (eventManager.eventIsActive()) {
                eventManager.updatePersonalScore(participant, contents);
            }
        }
        hubManager.updateLeaderboards();
    }
    
    /**
     * Update the display of the given participant to reflect their current score.
     * Also updates their team scores.
     * @param participant the participant to update
     */
    private void updatePersonalScore(Participant participant) {
        int score = getScore(participant.getUniqueId());
        Component contents = Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(score))
                .color(NamedTextColor.GOLD);
        if (activeGame != null && activeGame instanceof Headerable headerable) {
            headerable.updatePersonalScore(participant, contents);
        }
        if (eventManager.eventIsActive()) {
            eventManager.updatePersonalScore(participant, contents);
        }
        hubManager.updateLeaderboards();
    }
    
    /**
     * @return the event manager's point multiplier, if there is a match going on. 1.0 otherwise.
     */
    public double matchProgressPointMultiplier() {
        if (!eventManager.eventIsActive()) {
            return 1.0;
        }
        return eventManager.matchProgressPointMultiplier();
    }
}
