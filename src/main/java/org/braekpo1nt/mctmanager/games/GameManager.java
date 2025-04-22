package org.braekpo1nt.mctmanager.games;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.event.EventManager;
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
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.states.GameManagerState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.MaintenanceState;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.participant.ColorAttributes;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.OfflinePlayer;
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
import java.util.function.Function;
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
    private final EventManager eventManager;
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
    @Getter
    private final TabList tabList;
    
    public GameManager(Main plugin, 
                       Scoreboard mctScoreboard, 
                       @NotNull GameStateStorageUtil gameStateStorageUtil,
                       @NotNull SidebarFactory sidebarFactory) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.mctScoreboard = mctScoreboard;
        this.gameStateStorageUtil = gameStateStorageUtil;
        this.timerManager = new TimerManager(plugin);
        this.tabList = new TabList(plugin);
        this.sidebarFactory = sidebarFactory;
        this.eventManager = new EventManager(plugin, this, new VoteManager(this, plugin));
        this.state = new MaintenanceState(this, teams, onlineParticipants, onlineAdmins);
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
            for (Participant member : team.getOnlineMembers()) {
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
     * @param gameType the {@link GameType} to instantiate the {@link MCTGame} for
     * @return a new {@link MCTGame} instance for the given type. Null if the given type is null. 
     */
    private MCTGame instantiateGame(
            @NotNull GameType gameType, 
            Component title, 
            String configFile, 
            Collection<Team> newTeams, 
            Collection<Participant> newParticipants, 
            List<Player> newAdmins) throws ConfigIOException, ConfigInvalidException {
        return switch (gameType) {
            case SPLEEF -> {
                SpleefConfig config = new SpleefConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new SpleefGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case CLOCKWORK -> {
                ClockworkConfig config = new ClockworkConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ClockworkGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case SURVIVAL_GAMES -> {
                SurvivalGamesConfig config = new SurvivalGamesConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new SurvivalGamesGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case FARM_RUSH -> {
                FarmRushConfig config = new FarmRushConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new FarmRushGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case FOOT_RACE -> {
                FootRaceConfig config = new FootRaceConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new FootRaceGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case PARKOUR_PATHWAY -> {
                ParkourPathwayConfig config = new ParkourPathwayConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ParkourPathwayGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case CAPTURE_THE_FLAG -> {
                CaptureTheFlagConfig config = new CaptureTheFlagConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new CaptureTheFlagGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case EXAMPLE -> {
                ExampleConfig config = new ExampleConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                yield new ExampleGame(plugin, this, title, config, newTeams, newParticipants, newAdmins);
            }
            case FINAL -> {
                ColossalCombatConfig config = new ColossalCombatConfigController(plugin.getDataFolder(), gameType.getId()).getConfig(configFile);
                List<Team> sortedTeams = newTeams.stream()
                        .sorted((t1, t2) -> {
                            int scoreComparison = t2.getScore() - t1.getScore();
                            if (scoreComparison != 0) {
                                return scoreComparison;
                            }
                            return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
                        }).toList();
                yield new ColossalCombatGame(plugin, this, title, config, sortedTeams.getFirst(), sortedTeams.get(1), sortedTeams, newParticipants, newAdmins);
            }
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
        Participant killed = onlineParticipants.get(event.getPlayer().getUniqueId());
        if (killed == null) {
            return;
        }
        replaceWithDisplayName(event, killed);
        if (killed.getKiller() != null) {
            Participant killer = onlineParticipants.get(killed.getKiller().getUniqueId());
            if (killer != null) {
                replaceWithDisplayName(event, killer);
            }
        }
        GameManagerUtils.deColorLeatherArmor(event.getDrops());
    }
    
    /**
     * Takes in a {@link PlayerDeathEvent} and replaces all instances of the given player's name with the given player's display name
     * @param event the event
     * @param participant the player whose name should be replaced with their display name. 
     */
    private static void replaceWithDisplayName(PlayerDeathEvent event, Participant participant) {
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Component newDeathMessage = GameManagerUtils.replaceWithDisplayName(participant.getPlayer(), deathMessage);
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
            EquipmentSlot equipmentSlot = GameManagerUtils.toEquipmentSlot(event.getSlotType());
            if (equipmentSlot == null) {
                return;
            }
            participant.getEquipment().setItem(equipmentSlot, newItem);
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
        if (!onlineParticipants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (!isLeatherArmor(itemStack)) {
            return;
        }
        GameManagerUtils.deColorLeatherArmor(itemStack);
    }
    
    // TODO: handle block dispensing items
//    @EventHandler
//    public void onBlockDispenseItem(BlockDispenseArmorEvent event) {
//    }
    
    @EventHandler
    public void onParticipantInteract(PlayerInteractEvent event) {
        if (!onlineParticipants.containsKey(event.getPlayer().getUniqueId())) {
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
    
    public Scoreboard getMctScoreboard() {
        return mctScoreboard;
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
        gameStateStorageUtil.setupScoreboard(mctScoreboard, plugin.getServer());
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
        return true;
    }
    
    public void saveGameState() {
        try {
            gameStateStorageUtil.saveGameState();
        } catch (ConfigException e) {
            reportGameStateException("adding score to player", e);
        }
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Starts the given game
     * @param gameType The game to start
     * @param configFile the config file to use for the game
     * @param sender The sender to send messages and alerts to
     * @return true if the game started successfully, false otherwise
     */
    public boolean startGame(@NotNull GameType gameType, @NotNull String configFile, @NotNull CommandSender sender) {
        if (activeGames.containsKey(gameType)) {
            sender.sendMessage(Component.text("There is already a ")
                    .append(Component.text(gameType.getTitle()))
                    .append(Component.text(" game running."))
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
        
        
        Collection<MCTTeam> onlineTeams = MCTTeam.getOnlineTeams(teams);
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
            case FINAL -> {
                if (onlineTeams.size() < 2) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text(GameType.FINAL.getTitle()))
                            .append(Component.text(" needs at least 2 teams online to play")));
                }
            }
        }
        
        Component title = createNewTitle(gameType.getTitle());
        for (Participant participant : onlineParticipants.values()) {
            tabList.hidePlayer(participant);
        }
        
        try {
            MCTGame newGame = instantiateGame(gameType, title, configFile, new HashSet<>(onlineTeams), onlineParticipants.values().stream().map(p -> (Participant) p).toList(), onlineAdmins);
            for (MCTParticipant participant : onlineParticipants.values()) {
                participant.setCurrentGame(gameType);
            }
            activeGames.put(gameType, newGame);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for game %s", gameType), e);
            Component message = Component.text("Can't start ")
                    .append(Component.text(gameType.name())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED);
            sender.sendMessage(message);
            messageAdmins(message);
            for (Participant participant : onlineParticipants.values()) {
                tabList.showPlayer(participant);
            }
            return false;
        }
        state.updateScoreVisuals(onlineTeams, onlineParticipants.values());
        return true;
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
        return teams.get(participant.getTeamId());
    }
    
    /**
     * @param teamId the teamId to get the {@link Team} for
     * @return the team with the given teamId, if one exists, null otherwise
     */
    public @Nullable Team getTeam(@NotNull String teamId) {
        return teams.get(teamId);
    }
    
    private @NotNull Component createNewTitle(String baseTitle) {
        if (!eventManager.eventIsActive() || !eventManager.shouldDisplayGameNumber()) {
            return Component.empty()
                    .append(Component.text(baseTitle))
                    .color(NamedTextColor.BLUE);
        }
        int currentGameNumber = eventManager.getCurrentGameNumber();
        int maxGames = eventManager.getMaxGames();
        return Component.empty()
                .append(Component.text(baseTitle)
                        .color(NamedTextColor.BLUE))
                .append(Component.space())
                .append(Component.empty()
                        .append(Component.text("["))
                        .append(Component.text(currentGameNumber))
                        .append(Component.text("/"))
                        .append(Component.text(maxGames))
                        .append(Component.text("]"))
                        .color(NamedTextColor.GRAY));
    }
    
    public void stopAllGames() {
        for (MCTGame game : activeGames.values()) {
            game.stop();
        }
    }
    
    /**
     * If a game is currently going on, manually stops the game.
     * @throws NullPointerException if no game is currently running. 
     * Check if a game is running with isGameRunning()
     */
    public void manuallyStopGame(GameType gameType) {
        MCTGame game = activeGames.get(gameType);
        if (game == null) {
            return;
        }
        game.stop();
    }
    
    /**
     * Meant to be called by the active game when the game is over.
     * If an event is running, calls {@link EventManager#gameIsOver(GameType)}
     */
    public void gameIsOver(@NotNull GameType gameType, @NotNull Collection<UUID> gameParticipants) {
        MCTGame game = activeGames.remove(gameType);
        if (game == null) {
            return;
        }
        for (UUID uuid : gameParticipants) {
            MCTParticipant participant = onlineParticipants.get(uuid);
            participant.setCurrentGame(null);
            tabList.showPlayer(participant);
        }
        if (eventManager.eventIsActive()) {
            eventManager.gameIsOver(game.getType());
        }
    }
    
    public void startEditor(@NotNull GameType gameType, @NotNull String configFile, @NotNull CommandSender sender) {
        if (eventManager.eventIsActive()) {
            sender.sendMessage(Component.text("Can't start an editor while an event is going on")
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
            selectedEditor.loadConfig(configFile);
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
    
    public void validateEditor(@NotNull CommandSender sender, @NotNull String configFile) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        try {
            activeEditor.configIsValid(configFile);
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
    public void saveEditor(@NotNull CommandSender sender, @NotNull String configFile, boolean force) {
        if (!editorIsRunning()) {
            sender.sendMessage(Component.text("No editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (!force) {
            try {
                activeEditor.configIsValid(configFile);
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
            activeEditor.saveConfig(configFile);
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
    
    public void loadEditor(@NotNull String configFile, @NotNull CommandSender sender) {
        try {
            activeEditor.loadConfig(configFile);
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
    
    //====================================================
    // GameStateStorageUtil accessors and helpers
    //====================================================
    
    /**
     * Remove the given team from the game
     * @param sender   the sender of the command, who will receive success/error messages
     * @param teamId The internal name of the team to remove
     */
    public void removeTeam(CommandSender sender, String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            sender.sendMessage(Component.text("Team ")
                    .append(Component.text(teamId))
                    .append(Component.text(" does not exist."))
                    .color(NamedTextColor.RED));
            return;
        }
        Set<OfflineParticipant> members = team.getMemberUUIDs().stream().map(allParticipants::get).collect(Collectors.toSet());
        for (OfflineParticipant member : members) {
            leaveParticipant(sender, member);
        }
        teams.remove(team.getTeamId());
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
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (removeTeam)", teamId));
        }
        if (eventManager.eventIsActive()) {
            eventManager.updateTeamScores();
        }
    }
    
    /**
     * Add a team to the game.
     * @param teamId The teamId of the team. If a team with the given id already exists, nothing happens.
     * @param teamDisplayName The display name of the team.
     * @param colorString the string representing the color
     * @return the newly created team, or null if the given team already exists or could not be created
     */
    public Team addTeam(String teamId, String teamDisplayName, String colorString) {
        if (teams.containsKey(teamId)) {
            return null;
        }
        try {
            gameStateStorageUtil.addTeam(teamId, teamDisplayName, colorString);
        } catch (ConfigIOException e) {
            reportGameStateException("adding score to player", e);
        }
        
        NamedTextColor color = ColorMap.getNamedTextColor(colorString);
        ColorAttributes colorAttributes = ColorMap.getColorAttributes(colorString);
        MCTTeam team = new MCTTeam(teamId, teamDisplayName, color, colorAttributes, 0);
        teams.put(teamId, team);
        
        org.bukkit.scoreboard.Team newTeam = mctScoreboard.registerNewTeam(teamId);
        newTeam.displayName(Component.text(teamDisplayName));
        newTeam.color(color);
        tabList.addTeam(teamId, teamDisplayName, color);
        state.updateScoreVisuals(Collections.singletonList(team), Collections.emptyList());
        return team;
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
     * @param sender the sender of the command, who will receive success/error messages
     * @param offlinePlayer The player to join to the given team
     * @param name The name of the participant to join to the given team
     * @param teamId The internal teamId of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinParticipantToTeam(CommandSender sender, @NotNull OfflinePlayer offlinePlayer, @NotNull String name, @NotNull String teamId) {
        MCTTeam team = teams.get(teamId);
        if (team == null) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(teamId).decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid teamId")));
            return;
        }
        if (isAdmin(offlinePlayer.getUniqueId())) {
            removeAdmin(sender, offlinePlayer, name);
        }
        OfflineParticipant existingParticipant = allParticipants.get(offlinePlayer.getUniqueId());
        if (existingParticipant != null) {
            if (existingParticipant.getTeamId().equals(teamId)) {
                sender.sendMessage(Component.text()
                        .append(existingParticipant.displayName())
                        .append(Component.text(" is already a member of "))
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(". Nothing happened.")));
                return;
            }
            leaveParticipant(sender, existingParticipant);
        }
        
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(team.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.addPlayer(offlinePlayer);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (joinParticipantToTeam)", team.getTeamId()));
        }
        
        Component displayName = team.createDisplayName(name);
        OfflineParticipant offlineParticipant = new OfflineParticipant(offlinePlayer.getUniqueId(), name, displayName, teamId, 0);
        allParticipants.put(offlineParticipant.getUniqueId(), offlineParticipant);
        team.joinMember(offlineParticipant.getUniqueId());
        tabList.joinParticipant(
                offlineParticipant.getParticipantID(), 
                offlineParticipant.getName(), 
                offlineParticipant.getTeamId(), 
                false);
        addParticipantToGameState(offlineParticipant);
        // TODO: update leaderboards here
        sender.sendMessage(Component.text("Joined ")
                .append(offlineParticipant.displayName())
                .append(Component.text(" to "))
                .append(team.getFormattedDisplayName()));
        
        // if they are online
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            MCTParticipant participant = new MCTParticipant(offlineParticipant, player);
            participant.sendMessage(Component.text("You've been joined to team ")
                    .append(team.getFormattedDisplayName()));
            state.onParticipantJoin(participant);
        }
    }
    
    /**
     * Adds the new participant to the {@link GameStateStorageUtil} and joins them the given team. 
     * @param participant the participant to add to the game state
     */
    private void addParticipantToGameState(@NotNull OfflineParticipant participant) {
        try {
            gameStateStorageUtil.addNewPlayer(participant.getUniqueId(), participant.getName(), participant.getTeamId());
        } catch (ConfigIOException e) {
            reportGameStateException("adding new player", e);
            messageAdmins(Component.text("error occurred adding new player, see console for details.")
                    .color(NamedTextColor.RED));
        }
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
     * @param sender the sender of the command, who will receive success/error messages
     * @param offlineParticipant The participant to remove from their team
     */
    public void leaveParticipant(CommandSender sender, @NotNull OfflineParticipant offlineParticipant) {
        MCTTeam team = teams.get(offlineParticipant.getTeamId());
        MCTParticipant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
        if (participant != null) {
            state.onParticipantQuit(participant);
            participant.sendMessage(Component.text("You've been removed from ")
                    .append(team.getFormattedDisplayName()));
            onlineParticipants.remove(participant.getUniqueId());
        }
        team.leaveMember(offlineParticipant.getUniqueId());
        allParticipants.remove(offlineParticipant.getUniqueId());
        try {
            gameStateStorageUtil.leavePlayer(offlineParticipant.getUniqueId());
        } catch (ConfigIOException e) {
            reportGameStateException("leaving player", e);
            sender.sendMessage(Component.text("error occurred leaving player, see console for details.")
                    .color(NamedTextColor.RED));
        }
        // TODO: update leaderboards here
        tabList.leaveParticipant(offlineParticipant.getParticipantID());
        sender.sendMessage(Component.text("Removed ")
                .append(offlineParticipant.displayName())
                .append(Component.text(" from team "))
                .append(team.getFormattedDisplayName()));
        org.bukkit.scoreboard.Team scoreboardTeam = mctScoreboard.getTeam(offlineParticipant.getTeamId());
        if (scoreboardTeam != null) {
            if (offlineParticipant.getPlayer() != null) {
                scoreboardTeam.removePlayer(offlineParticipant.getPlayer());
            }
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (leaveParticipant)", offlineParticipant.getTeamId()));
        }
    }
    
    /**
     * Add the given scores to the given teams and participants, save the game state, update
     * the UI, etc.
     *
     * @param newTeamScores        map of teamId to score to add
     * @param newParticipantScores map of UUID to score to add
     * @param gameType             the type of the game
     */
    public void addScores(Map<String, Integer> newTeamScores, Map<UUID, Integer> newParticipantScores, GameType gameType) {
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
        try {
            gameStateStorageUtil.updateScores(teams.values(), allParticipants.values());
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, gameStateStorageUtil::saveGameState);
            } else {
                gameStateStorageUtil.saveGameState();
            }
        } catch (ConfigIOException e) {
            reportGameStateException("updating scores", e);
        }
        if (eventManager.eventIsActive()) {
            eventManager.trackScores(teamScores, participantScores, gameType);
        }
        state.updateScoreVisuals(teams.values(), onlineParticipants.values());
        displayStats(teamScores, participantScores);
    }
    
    private void displayStats(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores) {
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
                    .append(Component.text(i+1))
                    .append(Component.text(". "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScores.get(team.getTeamId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.newline());
        }
        everyone.append(Component.text("\nTop 5 Participants:"))
                .append(Component.newline());
        for (int i = 0; i < Math.min(sortedParticipants.size(), 5); i++) {
            OfflineParticipant participant = sortedParticipants.get(i);
            everyone
                    .append(Component.text("  "))
                    .append(Component.text(i+1))
                    .append(Component.text(". "))
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(Component.text(participantScores.get(participant.getUniqueId()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(" ("))
                    .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(" x "))
                    .append(Component.text(getMultiplier()))
                    .append(Component.text(")"))
                    .append(Component.newline());
        }
        Audience.audience(
                Audience.audience(sortedTeams),
                Audience.audience(onlineAdmins),
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
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text(" ("))
                            .append(Component.text((int) (participantScores.get(participant.getUniqueId()) / getMultiplier()))
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text(" x "))
                            .append(Component.text(getMultiplier()))
                            .append(Component.text(")"))
                            .append(Component.newline());
                    i++;
                }
            }
            team.sendMessage(message.build());
        }
        
        for (OfflineParticipant offlineParticipant : sortedParticipants) {
            Participant participant = onlineParticipants.get(offlineParticipant.getUniqueId());
            if (participant != null) {
                participant.sendMessage(
                        Component.empty()
                                .append(Component.text("Personal")
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(": "))
                                .append(Component.text(participantScores.get(offlineParticipant.getUniqueId()))
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(" ("))
                                .append(Component.text((int) (participantScores.get(offlineParticipant.getUniqueId()) / getMultiplier()))
                                        .color(NamedTextColor.GOLD))
                                .append(Component.text(" x "))
                                .append(Component.text(getMultiplier()))
                                .append(Component.text(")"))
                );
            }
        }
    }
    
    /**
     * @return the event manager's point multiplier, if there is a match going on. 1.0 otherwise.
     */
    public double getMultiplier() {
        if (!eventManager.eventIsActive()) {
            return 1.0;
        }
        return eventManager.matchProgressPointMultiplier();
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
        OfflineParticipant offlineParticipant = allParticipants.get(uniqueId);
        if (offlineParticipant != null) {
            leaveParticipant(sender, offlineParticipant);
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
        if (adminTeam != null) {
            adminTeam.addPlayer(newAdmin);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", ADMIN_TEAM));;
        }
        if (newAdmin.isOnline()) {
            newAdmin.sendMessage(Component.text("You were added as an admin"));
            state.onAdminJoin(newAdmin);
        }
    }
    
    /**
     * Removes the given player from the admins
     * @param sender the sender of the command, who will receive success/error messages
     * @param offlineAdmin The admin to remove
     */
    public void removeAdmin(@NotNull CommandSender sender, @NotNull OfflinePlayer offlineAdmin, String adminName) {
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
                state.onAdminQuit(onlineAdmin);
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
        if (adminTeam != null) {
            adminTeam.removePlayer(offlineAdmin);
        } else {
            Main.logger().warning(String.format("mctScoreboard could not find team \"%s\" (addAdmin)", ADMIN_TEAM));;
        }
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
    private void reportGameStateException(String attemptedOperation, ConfigException e) {
        Main.logger().severe(String.format("error while %s. See console log for error message.", attemptedOperation));
        messageAdmins(Component.empty()
                .append(Component.text("error while "))
                .append(Component.text(attemptedOperation))
                .append(Component.text(". See console log for error message.")));
        throw new RuntimeException(e);
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
