package org.braekpo1nt.mctmanager.games;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
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
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager implements Listener {
    
    private final Logger LOGGER;
    public static final String ADMIN_TEAM = "_Admins";
    public static final NamedTextColor ADMIN_COLOR = NamedTextColor.DARK_RED; 
    private MCTGame activeGame = null;
    private GameEditor activeEditor = null;
    private final Map<GameType, MCTGame> games;
    private final Map<GameType, GameEditor> editors;
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
     * Contains the list of online participants. Updated when participants are added/removed or quit/join
     */
    private final List<Player> onlineParticipants = new ArrayList<>();
    private final List<Player> onlineAdmins = new ArrayList<>();
    
    public GameManager(Main plugin, Scoreboard mctScoreboard) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.LOGGER = plugin.getLogger();
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.voteManager = new VoteManager(this, plugin);
        this.timerManager = new TimerManager(plugin);
        this.games = new HashMap<>();
        addGame(new FootRaceGame(plugin, this));
        addGame(new SurvivalGamesGame(plugin, this));
        addGame(new SpleefGame(plugin, this));
        addGame(new ParkourPathwayGame(plugin, this));
        addGame(new CaptureTheFlagGame(plugin, this));
        addGame(new ClockworkGame(plugin, this));
        this.editors = new HashMap<>();
        addEditor(new ParkourPathwayEditor(plugin, this));
        addEditor(new FootRaceEditor(plugin, this));
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
     * Adds the given game to the games map, with the key being the game's type
     * @param mctGame the {@link MCTGame} implementation
     * @throws IllegalArgumentException if you attempt to add a game whose {@link GameType} was already added to the games list
     */
    private void addGame(MCTGame mctGame) {
        Preconditions.checkArgument(!this.games.containsKey(mctGame.getType()), "A game with type %s already exists in the games map", mctGame.getType());
        this.games.put(mctGame.getType(), mctGame);
    }
    
    /**
     * Adds the given editor to the editors map, with the key being the editor's type
     * @param editor the {@link GameEditor} implementation
     * @throws IllegalArgumentException if you attempt to add an editor whose {@link GameType} was already added to the editors list
     */
    private void addEditor(GameEditor editor) {
        Preconditions.checkArgument(!this.editors.containsKey(editor.getType()), "An editor with type %s already exists in the games map", editor.getType());
        this.editors.put(editor.getType(), editor);
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
    
    public static final List<Material> LEATHER_ARMOR = List.of(
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (event.useItemInHand().equals(Event.Result.DENY) 
                || (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK))) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && clickedBlock.getType().isInteractable()) {
            return;
        }
        Player participant = event.getPlayer();
        if (!isParticipant(participant.getUniqueId())) {
            return;
        }
        ItemStack item = event.getItem();
        if (!isLeatherArmor(item)) {
            return;
        }
        EquipmentSlot slot = item.getType().getEquipmentSlot();
        Material typeInDestinationSlot = participant.getEquipment().getItem(slot).getType();
        if (typeInDestinationSlot.equals(Material.AIR)) {
            colorLeatherArmor(item, participant.getUniqueId());
        }
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
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerClickInventory(InventoryClickEvent event) {
        if (event.isCancelled() || event.getResult().equals(Event.Result.DENY)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player participant)) {
            return;
        }
        if (!isParticipant(participant.getUniqueId())) {
            return;
        }
        ItemStack currentItem = event.getCurrentItem(); // current item in clicked slot
        ItemStack cursorItem = event.getCursor();
        InventoryType.SlotType clickedSlot = event.getSlotType();
        if (isLeatherArmor(currentItem)) {
            if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                if (!event.getInventory().getType().equals(InventoryType.CRAFTING) 
                        || event.getClickedInventory() == null 
                        || !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                    return;
                }
                if (!clickedSlot.equals(InventoryType.SlotType.ARMOR)) {
                    EquipmentSlot destSlot = currentItem.getType().getEquipmentSlot();
                    Material destSlotMaterial = participant.getEquipment().getItem(destSlot).getType();
                    if (destSlotMaterial.equals(Material.AIR)) {
                        // shift click leather armor from non-armor slot to empty armor slot
                        colorLeatherArmor(currentItem, participant.getUniqueId());
                    }
                } else {
                    if (hasOpenSlot(participant.getInventory().getStorageContents())) {
                        GameManagerUtils.deColorLeatherArmor(currentItem);
                    }
                }
            } else {
                if (clickedSlot.equals(InventoryType.SlotType.ARMOR)) {
                    GameManagerUtils.deColorLeatherArmor(currentItem);
                }
            }
        }
        if (isLeatherArmor(cursorItem)) {
            if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
                return;
            }
            EquipmentSlot destSlot = cursorItem.getType().getEquipmentSlot();
            int destSlotNum = toArmorSlotNumber(destSlot);
            if (event.getSlot() == destSlotNum) {
                // use mouse to place leather armor in armor slot (either empty or replacing another armor)
                colorLeatherArmor(cursorItem, participant.getUniqueId());
            }
        }
    }
    
    /**
     * @param contents the contents to check if it has an open slot or not
     * @return true if even one of the given entries is null or of Material type AIR
     */
    private boolean hasOpenSlot(ItemStack[] contents) {
        for (ItemStack itemStack : contents) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the player's inventory slot number for the given EquipmentSlot. This only works for player inventories, nothing else. 
     * @param slot the equipment slot to get the slot number for
     * @return [39, 38, 37, 36] for [HEAD, CHEST, LEGS, FEET], respectively. -1 for anything else. 
     */
    private int toArmorSlotNumber(EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> {
                return 39;
            }
            case CHEST -> {
                return 38;
            }
            case LEGS -> {
                return 37;
            }
            case FEET -> {
                return 36;
            }
            default -> {
                return -1;
            }
        }
    }
    
    private void colorLeatherArmor(@NotNull ItemStack leatherArmor, @NotNull UUID participantUUID) {
        Color teamColor = getTeamColor(participantUUID);
        GameManagerUtils.colorLeatherArmor(leatherArmor, teamColor);
    }
    
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isAdmin(player.getUniqueId())) {
            event.quitMessage(GameManagerUtils.replaceWithDisplayName(player, event.quitMessage()));
            onAdminQuit(player);
            return;
        }
        if (isParticipant(player.getUniqueId())) {
            event.quitMessage(GameManagerUtils.replaceWithDisplayName(player, event.quitMessage()));
            onParticipantQuit(player);
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
     * @see GameManager#leavePlayer(CommandSender, OfflinePlayer, String) 
     */
    private void onParticipantQuit(@NotNull Player participant) {
        onlineParticipants.remove(participant);
        if (gameIsRunning()) {
            activeGame.onParticipantQuit(participant);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            eventManager.onParticipantQuit(participant);
        } else if (voteManager.isVoting()) {
            voteManager.onParticipantQuit(participant);
        }
        hubManager.onParticipantQuit(participant);
        Component displayName = Component.text(participant.getName(), NamedTextColor.WHITE);
        participant.displayName(displayName);
        participant.playerListName(displayName);
        GameManagerUtils.deColorLeatherArmor(participant);
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
            onParticipantJoin(player);
            event.joinMessage(GameManagerUtils.replaceWithDisplayName(player, event.joinMessage()));
            return;
        }
        if (isOfflineIGN(player.getName())) {
            onOfflineIGNJoin(player);
            event.joinMessage(GameManagerUtils.replaceWithDisplayName(player, event.joinMessage()));
        }
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
     * @param participant a player (who is an official participant) who joined
     */
    private void onParticipantJoin(@NotNull Player participant) {
        onlineParticipants.add(participant);
        participant.setScoreboard(mctScoreboard);
        participant.addPotionEffect(Main.NIGHT_VISION);
        String teamName = getTeamName(participant.getUniqueId());
        Component displayName = getDisplayName(participant);
        participant.displayName(displayName);
        participant.playerListName(displayName);
        hubManager.onParticipantJoin(participant);
        if (gameIsRunning()) {
            hubManager.removeParticipantsFromHub(Collections.singletonList(participant));
            activeGame.onParticipantJoin(participant);
        } else if (eventManager.eventIsActive() || eventManager.colossalCombatIsActive()) {
            hubManager.removeParticipantsFromHub(Collections.singletonList(participant));
            eventManager.onParticipantJoin(participant);
        } else if (voteManager.isVoting()) {
            voteManager.onParticipantJoin(participant);
        }
        GameManagerUtils.colorLeatherArmor(this, participant);
        updateTeamScore(teamName);
        updatePersonalScore(participant);
    }
    
    /**
     * Handles when a participant who's in-game-name (IGN) matches that of an OfflinePlayer in the GameState,
     * meaning they have joined the server for the first time.
     * The OfflinePlayer is transitioned to a normal participant in the game state, the player is joined to the 
     * appropriate team, and they are alerted to their new status
     * @param participant the participant who has joined for the first time
     */
    private void onOfflineIGNJoin(@NotNull Player participant) {
        String team = getOfflineIGNTeamName(participant.getName());
        if (team == null) {
            // this shouldn't happen
            return;
        }
        leaveOfflineIGN(Bukkit.getConsoleSender(), participant.getName());
        joinPlayerToTeam(Bukkit.getConsoleSender(), participant, team);
        messageAdmins(Component.empty()
                .append(participant.displayName())
                .append(Component.text(" was joined to "))
                .append(getFormattedTeamDisplayName(team))
                .append(Component.text(" because their IGN was listed in the GameState's offlinePlayers list.")));
    }
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
    }
    
    public SidebarFactory getSidebarFactory() {
        return sidebarFactory;
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
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mctScoreboard);
        }
        onlineParticipants.clear();
        onlineAdmins.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameStateStorageUtil.isAdmin(player.getUniqueId())) {
                onAdminJoin(player);
            }
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                onParticipantJoin(player);
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
        voteManager.startVote(onlineParticipants, votingPool, duration, (gameType) -> startGame(gameType, sender), onlineAdmins);
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
    
    public void removeParticipantsFromHub(List<Player> participantsToRemove) {
        hubManager.removeParticipantsFromHub(participantsToRemove);
    }
    
    /**
     * Starts the given game
     * @param gameType The game to start
     * @param sender The sender to send messages and alerts to
     * @return true if the game started successfully, false otherwise
     */
    public boolean startGame(GameType gameType, @NotNull CommandSender sender) {
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
        
        MCTGame selectedGame = this.games.get(gameType);
        if (selectedGame == null) {
            sender.sendMessage(Component.text("Can't find game for type " + gameType));
            return false;
        }
        
        // make sure config loads
        if (selectedGame instanceof Configurable configurable) {
            try {
                configurable.loadConfig();
            } catch (ConfigException e) {
                Bukkit.getLogger().severe(e.getMessage());
                e.printStackTrace();
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
        
        List<String> onlineTeams = getTeamNames(onlineParticipants);
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
                if (onlineTeams.size() < 2 || 8 < onlineTeams.size()) {
                    sender.sendMessage(Component.text("Capture the Flag needs at least 2 and at most 8 teams online to play.").color(NamedTextColor.RED));
                    return false;
                }
            }
        }
        
        if (eventManager.eventIsActive() && eventManager.shouldDisplayGameNumber()) {
            int currentGameNumber = eventManager.getCurrentGameNumber();
            int maxGames = eventManager.getMaxGames();
            String baseTitle = selectedGame.getBaseTitle();
            String newTitle = String.format("%s %s[%d/%d]", baseTitle, ChatColor.GRAY, currentGameNumber, maxGames);
            selectedGame.setTitle(newTitle);
        }
        
        hubManager.removeParticipantsFromHub(onlineParticipants);
        selectedGame.start(onlineParticipants, onlineAdmins);
        activeGame = selectedGame;
        for (String teamName : getTeamNames(onlineParticipants)) {
            updateTeamScore(teamName);
        }
        for (Player participant : onlineParticipants) {
            updatePersonalScore(participant);
        }
        return true;
    }
    
    public void updateGameTitle() {
        if (!gameIsRunning()) {
            return;
        }
        if (eventManager.eventIsActive() && eventManager.shouldDisplayGameNumber()) {
            int currentGameNumber = eventManager.getCurrentGameNumber();
            int maxGames = eventManager.getMaxGames();
            String baseTitle = activeGame.getBaseTitle();
            String newTitle = String.format("%s %s[%d/%d]", baseTitle, ChatColor.GRAY, currentGameNumber, maxGames);
            Bukkit.getLogger().info(newTitle);
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
     * If an event is running, calls {@link EventManagerOld#gameIsOver(GameType)}
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
        hubManager.returnParticipantsToHub(onlineParticipants, onlineAdmins, true);
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
        
        GameEditor selectedEditor = this.editors.get(gameType);
        if (selectedEditor == null) {
            sender.sendMessage(Component.text("Can't find editor for game type " + gameType)
                    .color(NamedTextColor.RED));
            return;
        }
        
        // make sure config loads
        try {
            selectedEditor.loadConfig();
        } catch (ConfigException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
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
        
        selectedEditor.start(onlineParticipants);
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
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
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
                Bukkit.getLogger().severe(e.getMessage());
                e.printStackTrace();
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
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
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
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
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
        hubManager.returnParticipantsToHub(onlineParticipants, onlineAdmins, false);
    }
    
    /**
     * Instantly returns the given participant to the hub
     * @param participant the participant to be returned to the hub
     */
    public void returnParticipantToHubInstantly(Player participant) {
        hubManager.returnParticipantsToHub(Collections.singletonList(participant), Collections.emptyList(), false);
    }
    
    public void returnAllParticipantsToPodium(String winningTeam) {
        List<Player> winningTeamParticipants = getOnlinePlayersOnTeam(winningTeam);
        List<Player> otherParticipants = new ArrayList<>();
        for (Player participant : getOnlineParticipants()) {
            if (!winningTeamParticipants.contains(participant)) {
                otherParticipants.add(participant);
            }
        }
        hubManager.sendAllParticipantsToPodium(winningTeamParticipants, otherParticipants, onlineAdmins);
    }
    
    public void returnParticipantToPodium(Player participant, boolean winner) {
        hubManager.sendParticipantToPodium(participant, winner);
    }
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
    /**
     * Remove the given team from the game
     * @param sender   the sender of the command, who will receive success/error messages
     * @param teamName The internal name of the team to remove
     */
    public void removeTeam(CommandSender sender, String teamName) {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            sender.sendMessage(Component.text("Team ")
                    .append(Component.text(teamName))
                    .append(Component.text(" does not exist."))
                    .color(NamedTextColor.RED));
            return;
        }
        leavePlayersOnTeam(sender, teamName);
        try {
            Component formattedTeamDisplayName = getFormattedTeamDisplayName(teamName);
            gameStateStorageUtil.removeTeam(teamName);
            sender.sendMessage(Component.text("Removed team ")
                    .append(formattedTeamDisplayName)
                    .append(Component.text(".")));
        } catch (ConfigIOException e) {
            reportGameStateException("removing team", e);
            sender.sendMessage(Component.text("error occurred removing team, see console for details.")
                    .color(NamedTextColor.RED));
        }
        Team team = mctScoreboard.getTeam(teamName);
        if (team != null) {
            team.unregister();
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
    }
    
    private void leavePlayersOnTeam(CommandSender sender, String teamName) {
        List<UUID> playerUniqueIds = gameStateStorageUtil.getParticipantUUIDsOnTeam(teamName);
        for (UUID playerUniqueId : playerUniqueIds) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUniqueId);
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "unknown";
            leavePlayer(sender, offlinePlayer, name);
        }
        List<String> offlineIGNs = gameStateStorageUtil.getOfflineIGNsOnTeam(teamName);
        for (String offlineIGN : offlineIGNs) {
            leaveOfflineIGN(sender, offlineIGN);
        }
    }
    
    /**
     * Add a team to the game.
     * @param teamName The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     */
    public void addTeam(String teamName, String teamDisplayName, String colorString) {
        if (gameStateStorageUtil.containsTeam(teamName)) {
            return;
        }
        try {
            gameStateStorageUtil.addTeam(teamName, teamDisplayName, colorString);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to player", e);
        }
        Team newTeam = mctScoreboard.registerNewTeam(teamName);
        newTeam.displayName(Component.text(teamDisplayName));
        NamedTextColor color = ColorMap.getNamedTextColor(colorString);
        newTeam.color(color);
        updateTeamScore(teamName);
    }
    
    /**
     * A list of all the teams in the game
     * @return A list containing the internal names of all the teams in the game. 
     * Empty list if there are no teams
     */
    public Set<String> getTeamNames() {
        return gameStateStorageUtil.getTeamNames();
    }
    
    /**
     * Gets a list of all unique team names which the given participants belong to.
     * @param participants The list of participants to get the team names of
     * @return A list of all unique team names which the given participants belong to.
     */
    public List<String> getTeamNames(List<Player> participants) {
        List<String> teamIds = new ArrayList<>();
        for (Player participant : participants) {
            String teamId = getTeamName(participant.getUniqueId());
            if (!teamIds.contains(teamId)){
                teamIds.add(teamId);
            }
        }
        return teamIds;
    }
    
    /**
     * Gets all available unique teamIds of the given UUIDs. Ignores invalid UUIDs.
     * @param uuids the UUIDs to get the teamIds of. Each UUID doesn't strictly have to be a valid
     *              participant UUID, and can be the UUID of an offline player. If a teamId
     *              is not found for a given UUID, it simply will not be included.
     * @return A list of all unique teamIds which the given participant UUIDs belong to
     */
    public List<String> getTeamIdsByUUID(List<UUID> uuids) {
        List<String> teamIds = new ArrayList<>();
        for (UUID uuid : uuids) {
            String teamId = getTeamName(uuid);
            if (!teamIds.contains(teamId)) {
                teamIds.add(teamId);
            }
        }
        return teamIds;
    }
    
    /**
     * Checks if the team exists in the game state
     * @param teamName The team to look for
     * @return true if the team with the given teamName exists, false otherwise.
     */
    public boolean hasTeam(String teamName) {
        return gameStateStorageUtil.containsTeam(teamName);
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
     * Joins the given player to the team with the given teamName. If the player was on a team already (not teamName) they will be removed from that team and added to the other team. 
     * Note, this will not join a player to a team if that player is an admin. 
     * @param sender the sender of the command, who will receive success/error messages
     * @param participant The player to join to the given team
     * @param teamName The internal teamName of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinPlayerToTeam(CommandSender sender, Player participant, String teamName) {
        UUID playerUniqueId = participant.getUniqueId();
        if (isAdmin(playerUniqueId)) {
            removeAdmin(sender, participant, participant.getName());
        }
        if (isParticipant(playerUniqueId)) {
            String originalTeamName = getTeamName(playerUniqueId);
            if (originalTeamName.equals(teamName)) {
                sender.sendMessage(Component.text()
                        .append(Component.text(participant.getName())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is already a member of team "))
                        .append(Component.text(teamName))
                        .append(Component.text(". Nothing happened.")));
                return;
            }
            leavePlayer(sender, participant, participant.getName());
        }
        addNewPlayer(sender, playerUniqueId, teamName);
        hubManager.updateLeaderboards();
        NamedTextColor teamNamedTextColor = getTeamNamedTextColor(teamName);
        Component displayName = Component.text(participant.getName(), teamNamedTextColor);
        participant.displayName(displayName);
        participant.playerListName(displayName);
        Component teamDisplayName = getFormattedTeamDisplayName(teamName);
        participant.sendMessage(Component.text("You've been joined to team ")
                .append(teamDisplayName));
        sender.sendMessage(Component.text("Joined ")
                .append(displayName
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to team "))
                .append(teamDisplayName));
    }
    
    /**
     * @param sender the sender of the command, who will receive success/error messages
     * @param ign The in-game-name of the participant who has never logged in before, which you are joining to the given team
     * @param offlineUniqueId nullable. The UUID of the offline player with the ign, if you know it. 
     * @param teamId the teamId of the team to join the participant to. Must be a valid teamId. 
     */
    public void joinOfflineIGNToTeam(CommandSender sender, @NotNull String ign, @Nullable UUID offlineUniqueId, @NotNull String teamId) {
        if (offlineUniqueId != null) {
            if (isAdmin(offlineUniqueId)) {
                OfflinePlayer offlineAdmin = Bukkit.getOfflinePlayer(offlineUniqueId);
                removeAdmin(sender, offlineAdmin, ign);
            }
            if (isParticipant(offlineUniqueId)) {
                String originalTeamName = getTeamName(offlineUniqueId);
                if (originalTeamName.equals(teamId)) {
                    sender.sendMessage(Component.text()
                            .append(Component.text(ign)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is already a member of team "))
                            .append(Component.text(teamId))
                            .append(Component.text(". Nothing happened.")));
                    return;
                }
                OfflinePlayer offlineParticipant = Bukkit.getOfflinePlayer(offlineUniqueId);
                leavePlayer(sender, offlineParticipant, ign);
            }
        }
        if (isOfflineIGN(ign)) {
            String originalTeamName = getOfflineIGNTeamName(ign);
            if (originalTeamName != null && originalTeamName.equals(teamId)) {
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
        addNewOfflineIGN(sender, ign, offlineUniqueId, teamId);
        hubManager.updateLeaderboards();
        Component teamDisplayName = getFormattedTeamDisplayName(teamId);
        NamedTextColor teamNamedTextColor = getTeamNamedTextColor(teamId);
        TextComponent displayName = Component.text(ign)
                .color(teamNamedTextColor)
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
     * Adds the new player to the game state and joins them the given team. 
     * If a game is running, and the player is online, joins the player to that game.  
     * @param sender the sender of the command, who will receive success/error messages
     * @param playerUniqueId The UUID of the player to add
     * @param teamName The name of the team to join the new player to
     */
    private void addNewPlayer(CommandSender sender, UUID playerUniqueId, String teamName) {
        try {
            gameStateStorageUtil.addNewPlayer(playerUniqueId, teamName);
        } catch (ConfigIOException e) {
            reportGameStateException("adding new player", e);
            sender.sendMessage(Component.text("error occurred adding new player, see console for details.")
                    .color(NamedTextColor.RED));
        }
        Team team = mctScoreboard.getTeam(teamName);
        OfflinePlayer newPlayer = Bukkit.getOfflinePlayer(playerUniqueId);
        Preconditions.checkState(team != null, "Something is wrong with the team Scoreboard. Could not find team with name %s", teamName);
        team.addPlayer(newPlayer);
        Player onlineNewPlayer = newPlayer.getPlayer();
        if (onlineNewPlayer != null) {
            onParticipantJoin(onlineNewPlayer);
        }
    }
    
    /**
     * Adds the new offline IGN to the game state and joins them the given team. 
     * @param sender the sender of the command, who will receive success/error messages
     * @param ign The in-game-name of the participant who has never logged in before
     * @param offlineUniqueId nullable. The UUID of the offline player with the ign, if you know it.
     * @param teamName The valid teamId of the team to join the new player to. 
     */
    private void addNewOfflineIGN(CommandSender sender, @NotNull String ign, @Nullable UUID offlineUniqueId, String teamName) {
        try {
            gameStateStorageUtil.addNewOfflineIGN(ign, offlineUniqueId, teamName);
        } catch (ConfigIOException e) {
            reportGameStateException("adding new offline IGN", e);
            sender.sendMessage(Component.text("error occurred adding new offline IGN, see console for details.")
                    .color(NamedTextColor.RED));
        }
    }
    
    /**
     * Gets the online players who are on the given team. 
     * @param teamName The internal name of the team
     * @return A list of all online players on that team, 
     * or empty list if there are no players on that team or the team doesn't exist.
     */
    public List<Player> getOnlinePlayersOnTeam(String teamName) {
        List<UUID> playerUniqueIds = gameStateStorageUtil.getParticipantUUIDsOnTeam(teamName);
        List<Player> onlinePlayersOnTeam = new ArrayList<>();
        for (UUID playerUniqueId : playerUniqueIds) {
            Player player = Bukkit.getPlayer(playerUniqueId);
            if (player != null && player.isOnline()) {
                onlinePlayersOnTeam.add(player);
            }
        }
        return onlinePlayersOnTeam;
    }
    
    public List<UUID> getParticipantUUIDsOnTeam(String teamName) {
        return gameStateStorageUtil.getParticipantUUIDsOnTeam(teamName);
    }
    
    /**
     * Leaves the player from the team and removes them from the game state.
     * If a game is running, and the player is online, removes that player from the game as well. 
     * @param sender the sender of the command, who will receive success/error messages
     * @param offlinePlayer The player to remove from the team
     */
    public void leavePlayer(CommandSender sender, @NotNull OfflinePlayer offlinePlayer, @NotNull String playerName) {
        UUID playerUniqueId = offlinePlayer.getUniqueId();
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        if (teamName == null) {
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
        Component teamDisplayName = getFormattedTeamDisplayName(teamName);
        if (offlinePlayer.isOnline()) {
            Player onlinePlayer = offlinePlayer.getPlayer();
            if (onlinePlayer != null) {
                onParticipantQuit(onlinePlayer);
                onlinePlayer.sendMessage(Component.text("You've been removed from ")
                        .append(teamDisplayName));
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
        sender.sendMessage(Component.text("Removed ")
                .append(Component.text(playerName)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" from team "))
                .append(teamDisplayName));
        Team team = mctScoreboard.getTeam(teamName);
        Preconditions.checkState(team != null, "mctScoreboard could not find team \"%s\"", teamName);
        team.removePlayer(offlinePlayer);
    }
    
    /**
     * Leaves the offline IGN from the team and removes them from the game state.
     * @param sender the sender of the command, who will receive success/error messages
     * @param ign the in-game-name of a participant who has never logged on
     */
    public void leaveOfflineIGN(CommandSender sender, @NotNull String ign) {
        String teamName = gameStateStorageUtil.getOfflineIGNTeamName(ign);
        Component teamDisplayName;
        if (teamName != null) {
            teamDisplayName = getFormattedTeamDisplayName(teamName);
        } else {
            teamDisplayName = Component.text("null").decorate(TextDecoration.ITALIC);
        }
        try {
            gameStateStorageUtil.leaveOfflineIGN(ign);
        } catch (ConfigIOException e) {
            reportGameStateException("leaving offline IGN", e);
            sender.sendMessage(Component.text("error occurred leaving offline IGN, see console for details.")
                    .color(NamedTextColor.RED));
        }
        hubManager.updateLeaderboards();
        TextComponent displayName = Component.text(ign)
                .decorate(TextDecoration.BOLD);
        sender.sendMessage(Component.text("Removed ")
                .append(displayName)
                .append(Component.text(" from team "))
                .append(teamDisplayName)
                .append(Component.newline())
                .append(Component.empty()
                        .append(displayName)
                        .append(Component.text(" is offline"))
                        .decorate(TextDecoration.ITALIC)
                )
        );
    }
    
    /**
     * Gets the teamId of the player with the given UUID
     * @param playerUniqueId The UUID of the player to find the team of
     * @return The teamId of the player with the given UUID
     * @throws NullPointerException if the game state doesn't contain the player's UUID
     */
    public String getTeamName(UUID playerUniqueId) {
        return gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
    }
    
    /**
     * @param ign the in-game-name of a participant who has never logged in before
     * @return the teamId of the OfflineParticipant with the given ign. Null if the ign doesn't exist in the GameState
     */
    public @Nullable String getOfflineIGNTeamName(@NotNull String ign) {
        return gameStateStorageUtil.getOfflineIGNTeamName(ign);
    }
    
    /**
     * @param uniqueId the UUID of the offline IGN to get
     * @return the in-game-name of the offlinePlayer in the GameState with the given UUID. Null if the UUID doesn't belong to an offline player 
     */
    public @Nullable String getOfflineIGN(@NotNull UUID uniqueId) {
        return gameStateStorageUtil.getOfflineIGN(uniqueId);
    }
    
    /**
     * Awards points to the participant and their team and announces to that participant how many points they received. 
     * If the participant does not exist, nothing happens.
     * @param participant The participant to award points to
     * @param points The points to award to the participant
     */
    public void awardPointsToParticipant(Player participant, int points) {
        UUID participantUUID = participant.getUniqueId();
        if (!gameStateStorageUtil.containsPlayer(participantUUID)) {
            return;
        }
        String teamName = gameStateStorageUtil.getPlayerTeamName(participantUUID);
        double multiplier = eventManager.matchProgressPointMultiplier();
        int multipliedPoints = (int) (points * multiplier);
        addScore(participantUUID, points);
        addScore(teamName, multipliedPoints);
        eventManager.trackPoints(participantUUID, points, activeGame.getType());
        eventManager.trackPoints(teamName, multipliedPoints, activeGame.getType());
        participant.sendMessage(Component.text("+")
                .append(Component.text(multipliedPoints))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
        updateTeamScore(teamName);
        updatePersonalScore(participant);
    }
    
    /**
     * Adds the given points to the given team, and announces to all online members of that 
     * team how many points the team earned.
     * If the team doesn't exist, nothing happens. 
     * @param teamName The team to add points to
     * @param points The points to add
     */
    public void awardPointsToTeam(String teamName, int points) {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            return;
        }
        int multipliedPoints = (int) (points * eventManager.matchProgressPointMultiplier());
        addScore(teamName, multipliedPoints);
        eventManager.trackPoints(teamName, multipliedPoints, activeGame.getType());
        
        Component displayName = getFormattedTeamDisplayName(teamName);
        List<Player> playersOnTeam = getOnlinePlayersOnTeam(teamName);
        for (Player playerOnTeam : playersOnTeam) {
            playerOnTeam.sendMessage(Component.text("+")
                    .append(Component.text(multipliedPoints))
                    .append(Component.text(" points for "))
                    .append(displayName)
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.GOLD));
        }
        updateTeamScore(teamName);
    }
    
    public Color getTeamColor(UUID playerUniqueId) {
        return gameStateStorageUtil.getTeamColor(playerUniqueId);
    }
    
    public @NotNull NamedTextColor getTeamNamedTextColor(@Nullable String teamName) {
        if (teamName == null) {
            return NamedTextColor.WHITE;
        }
        return gameStateStorageUtil.getTeamNamedTextColor(teamName);
    }
    
    /**
     * Gets the team's display name as a Component with the team's text color
     * and in bold
     * @param teamId The internal name of the team
     * @return A Component with the formatted team dislay name
     */
    public @NotNull Component getFormattedTeamDisplayName(@NotNull String teamId) {
        String displayName = gameStateStorageUtil.getTeamDisplayName(teamId);
        NamedTextColor teamColor = gameStateStorageUtil.getTeamNamedTextColor(teamId);
        return Component.text(displayName).color(teamColor).decorate(TextDecoration.BOLD);
    }
    
    /**
     * @param participant must be a valid participant in the GameState
     * @return the display name of the given participant
     */
    public Component getDisplayName(@NotNull Player participant) {
        return getDisplayName(participant.getUniqueId(), participant.getName());
    }
    
    /**
     * @param offlineParticipant an OfflinePlayer with the UUID of a valid participant. 
     *                           If the OfflinePlayer's name is null, then the
     *                           UUID will be used as the name.
     * @return the displayName of the participant represented by the given offline participant
     */
    public Component getDisplayName(@NotNull OfflinePlayer offlineParticipant) {
        String name = offlineParticipant.getName();
        UUID uuid = offlineParticipant.getUniqueId();
        if (name == null) {
            String ign = gameStateStorageUtil.getOfflineIGN(uuid);
            if (ign != null) {
                return getDisplayName(uuid, ign);
            }
            return getDisplayName(uuid, uuid.toString());
        }
        return getDisplayName(uuid, name);
    }
    
    /**
     * Get the display name of the given participant's UUID. Throws an error if this is not a valid participant UUID.
     * @param participantUUID a valid participant's UUID in the GameState
     * @param name the name of the player
     * @return the display name of the given participant's UUID
     */
    public Component getDisplayName(@NotNull UUID participantUUID, @NotNull String name) {
        String teamName = getTeamName(participantUUID);
        NamedTextColor teamNamedTextColor = getTeamNamedTextColor(teamName);
        return Component.text(name, teamNamedTextColor);
    }
    
    public String getTeamDisplayName(String teamName) {
        return gameStateStorageUtil.getTeamDisplayName(teamName);
    }
    
    /**
     * @return a copy of the list of online participants. Modifying this will not change
     * the online participants
     */
    public List<Player> getOnlineParticipants() {
        return new ArrayList<>(onlineParticipants);
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
     * @return a list of the names of all the teams in the game state
     */
    public List<String> getAllTeamNames() {
        return new ArrayList<>(gameStateStorageUtil.getTeamNames());
    }
    
    /**
     * @return A list of all OfflinePlayers in the game state. These players could
     * be offline or online, have names or not
     */
    public List<OfflinePlayer> getOfflineParticipants() {
        List<UUID> uniqueIds = gameStateStorageUtil.getPlayerUniqueIds();
        List<OfflinePlayer> offlineParticipants = new ArrayList<>();
        for (UUID uniqueId : uniqueIds) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
            offlineParticipants.add(offlinePlayer);
        }
        List<UUID> offlineUniqueIds = gameStateStorageUtil.getOfflinePlayerUniqueIds();
        for (UUID offlineUniqueId : offlineUniqueIds) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offlineUniqueId);
            offlineParticipants.add(offlinePlayer);
        }
        return offlineParticipants;
    }
    
    /**
     * Adds the given score to the participant with the given UUID
     * @param participantUUID The UUID of the participant to add the score to
     * @param score The score to add. Could be positive or negative.
     */
    public void addScore(UUID participantUUID, int score) {
        try {
            gameStateStorageUtil.addScore(participantUUID, score);
            Player participant = Bukkit.getPlayer(participantUUID);
            if (participant != null && onlineParticipants.contains(participant)) {
                updatePersonalScore(participant);
            }
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to player", e);
        }
    }
    
    /**
     * Adds the given score to the team with the given name
     * @param teamName The name of the team to add the score to
     * @param score The score to add. Could be positive or negative.
     */
    public void addScore(String teamName, int score) {
        try {
            gameStateStorageUtil.addScore(teamName, score);
            updateTeamScore(teamName);
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
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(participantUUID, 0);
                return;
            }
            gameStateStorageUtil.setScore(participantUUID, score);
            Player participant = Bukkit.getPlayer(participantUUID);
            if (participant != null && onlineParticipants.contains(participant)) {
                updatePersonalScore(participant);
            }
        } catch (ConfigIOException e) {
            reportGameStateException("setting a player's score", e);
        }
    }
    
    /**
     * Sets the score of the team with the given name to the given value
     * @param teamName The UUID of the participant to set the score to
     * @param score The score to set to. If the score is negative, the score will be set to 0.
     */
    public void setScore(String teamName, int score) {
        try {
            if (score < 0) {
                gameStateStorageUtil.setScore(teamName, 0);
                return;
            }
            gameStateStorageUtil.setScore(teamName, score);
            updateTeamScore(teamName);
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
            for (Player participant : getOnlineParticipants()) {
                updatePersonalScore(participant);
            }
            for (String teamName : getTeamNames()) {
                updateTeamScore(teamName);
            }
        } catch (ConfigIOException e) {
            reportGameStateException("setting all scores", e);
        }
    }
    
    /**
     * Gets the score of the given team
     * @param teamName The team to get the score of
     * @return The score of the given team
     */
    public int getScore(String teamName) {
        return gameStateStorageUtil.getTeamScore(teamName);
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
     * @return a map of each participant's UUID to their score
     */
    public @NotNull Map<UUID, Integer> getParticipantScores() {
        return gameStateStorageUtil.getParticipantScores();
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
            leavePlayer(sender, newAdmin, newAdmin.getName());
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
        Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
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
        Team adminTeam = mctScoreboard.getTeam(ADMIN_TEAM);
        Preconditions.checkState(adminTeam != null, "mctScoreboard could not find team \"%s\"", ADMIN_TEAM);
        adminTeam.removePlayer(offlineAdmin);
    }
    
    public Material getTeamPowderColor(@NotNull String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getConcretePowderColor(colorString);
    }
    
    public Material getTeamConcreteColor(@NotNull String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getConcreteColor(colorString);
    }
    
    public Material getTeamStainedGlassColor(@NotNull String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getStainedGlassColor(colorString);
    }
    
    public ChatColor getTeamChatColor(@NotNull String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getChatColor(colorString);
    }
    
    public Material getTeamBannerColor(@NotNull String teamName) {
        String colorString = gameStateStorageUtil.getTeamColorString(teamName);
        return ColorMap.getBannerColor(colorString);
    }
    
    public void setBoundaryEnabled(boolean boundaryEnabled) {
        hubManager.setBoundaryEnabled(boundaryEnabled);
    }
    
    public void messageAdmins(Component message) {
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player admin : onlineAdmins) {
            admin.sendMessage(message);
        }
    }
    
    public void playSoundForAdmins(@NotNull String sound, float volume, float pitch) {
        for (Player admin : onlineAdmins) {
            admin.playSound(admin.getLocation(), sound, volume, pitch);
        }
    }
    
    public void messageOnlineParticipants(Component message) {
        for (Player participant : onlineParticipants) {
            participant.sendMessage(message);
        }
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
    
    private void updateTeamScore(String team) {
        String displayName = getTeamDisplayName(team);
        ChatColor teamChatColor = getTeamChatColor(team);
        int teamScore = getScore(team);
        if (activeGame != null && activeGame instanceof Headerable headerable) {
            for (Player participant : getOnlinePlayersOnTeam(team)) {
                headerable.updateTeamScore(participant, String.format("%s%s: %s%s", teamChatColor, displayName, ChatColor.GOLD, teamScore));
            }
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
        hubManager.updateLeaderboards();
    }
    
    private void updatePersonalScore(Player participant) {
        int score = getScore(participant.getUniqueId());
        String contents = String.format("%sPersonal: %s", ChatColor.GOLD, score);
        if (activeGame != null && activeGame instanceof Headerable headerable) {
            headerable.updatePersonalScore(participant, contents);
        }
        if (eventManager.eventIsActive()) {
            eventManager.updatePersonalScore(participant, contents);
        }
        hubManager.updateLeaderboards();
    }
}
