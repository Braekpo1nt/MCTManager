package org.braekpo1nt.mctmanager.games.colossalcombat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.colossalcombat.config.ColossalCombatConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ColossalCombatGame implements Listener, Configurable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final @NotNull BattleTopbar topbar;
    private final ColossalCombatConfigController configController;
    private ColossalCombatConfig config;
    private final String title = ChatColor.BLUE+"Colossal Combat";
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private Map<UUID, Integer> killCounts = new HashMap<>();
    private Map<UUID, Integer> deathCounts = new HashMap<>();
    private List<Player> admins = new ArrayList<>();
    private List<ColossalCombatRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    private int firstPlaceRoundWins = 0;
    private int secondPlaceRoundWins = 0;
    private String firstTeamName;
    private String secondTeamName;
    private boolean descriptionShowing = false;
    private boolean gameActive = false;
    private final TimerManager timerManager;
    
    public ColossalCombatGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new ColossalCombatConfigController(plugin.getDataFolder());
        this.topbar = new BattleTopbar();
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
        if (gameActive) {
            for (ColossalCombatRound round : rounds) {
                round.setConfig(this.config);
            }
        }
    }
    
    /**
     * Start the game with the first and second place teams, and the spectators. 
     * @param newFirstPlaceParticipants The participants in the first place team
     * @param newSecondPlaceParticipants The participants in the second place team
     * @param newSpectators The participants who are third place and on, who should spectate the game
     * @param newAdmins The admins
     */
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators, List<Player> newAdmins) {
        firstTeamName = gameManager.getTeamName(newFirstPlaceParticipants.get(0).getUniqueId());
        secondTeamName = gameManager.getTeamName(newSecondPlaceParticipants.get(0).getUniqueId());
        firstPlaceRoundWins = 0;
        secondPlaceRoundWins = 0;
        closeGates();
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        spectators = new ArrayList<>(newSpectators.size());
        killCounts = new HashMap<>(newFirstPlaceParticipants.size() + newSecondPlaceParticipants.size());
        deathCounts = new HashMap<>(newFirstPlaceParticipants.size() + newSecondPlaceParticipants.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        int numOfRounds = (config.getRequiredWins() * 2) - 1;
        rounds = new ArrayList<>(numOfRounds);
        for (int i = 0; i < numOfRounds; i++) {
            rounds.add(new ColossalCombatRound(plugin, gameManager, this, config, sidebar, adminSidebar, topbar));
        }
        currentRoundIndex = 0;
        for (Player first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Player second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Player spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        initializeSidebar();
        setupTeamOptions();
        startAdmins(newAdmins);
        gameActive = true;
        startDescriptionPeriod();
        displayDescription();
        Bukkit.getLogger().info("Started Colossal Combat");
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        first.teleport(config.getFirstPlaceSpawn());
        first.setBedSpawnLocation(config.getFirstPlaceSpawn(), true);
        initializeParticipant(first);
        initializeKillCount(first);
    }
    
    private void initializeKillCount(Player participant) {
        killCounts.putIfAbsent(participant.getUniqueId(), 0);
        deathCounts.putIfAbsent(participant.getUniqueId(), 0);
        int kills = killCounts.get(participant.getUniqueId());
        int deaths = deathCounts.get(participant.getUniqueId());
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
    }
    
    private void initializeSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        second.teleport(config.getSecondPlaceSpawn());
        second.setBedSpawnLocation(config.getSecondPlaceSpawn(), true);
        initializeParticipant(second);
        initializeKillCount(second);
    }
    
    private void initializeSpectator(Player spectator) {
        spectators.add(spectator);
        spectator.teleport(config.getSpectatorSpawn());
        spectator.setBedSpawnLocation(config.getSpectatorSpawn(), true);
        initializeParticipant(spectator);
    }
    
    /**
     * General initialization for every participant, first, second, and spectator
     * @param participant the participant
     */
    private void initializeParticipant(Player participant) {
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        updateRoundWinSidebar();
        adminSidebar.updateLines(admin.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round: %s", currentRoundIndex+1))
        );
    }
    
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getSpectatorSpawn());
    }
    
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        timerManager.start(Timer.builder()
                        .duration(config.getDescriptionDuration())
                        .withSidebar(adminSidebar, "timer")
                        .withTopbar(topbar)
                        .sidebarPrefix(Component.text("Starting soon: "))
                        .onCompletion(() -> {
                            descriptionShowing = false;        
                            startNextRound();
                        })
                        .build());
    }
    
    private void startNextRound() {
        ColossalCombatRound nextRound = rounds.get(currentRoundIndex);
        setUpTopbarForRound();
        nextRound.start(firstPlaceParticipants, secondPlaceParticipants, spectators, firstTeamName, secondTeamName);
        sidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
        adminSidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
    }
    
    private void setUpTopbarForRound() {
        topbar.removeAllTeamPairs();
        NamedTextColor firstColor = gameManager.getTeamNamedTextColor(firstTeamName);
        NamedTextColor secondColor = gameManager.getTeamNamedTextColor(secondTeamName);
        topbar.addTeam(firstTeamName, firstColor);
        topbar.addTeam(secondTeamName, secondColor);
        topbar.linkTeamPair(firstTeamName, secondTeamName);
        for (Player firstPlaceParticipant : firstPlaceParticipants) {
            topbar.linkToTeam(firstPlaceParticipant.getUniqueId(), firstTeamName);
        }
        for (Player secondPlaceParticipant : secondPlaceParticipants) {
            topbar.linkToTeam(secondPlaceParticipant.getUniqueId(), secondTeamName);
        }
        for (Player spectator : spectators) {
            topbar.linkToTeam(spectator.getUniqueId(), firstTeamName);
        }
        topbar.setMembers(firstTeamName, firstPlaceParticipants.size(), 0);
        topbar.setMembers(secondTeamName, secondPlaceParticipants.size(), 0);
    }
    
    public void onFirstPlaceWinRound() {
        firstPlaceRoundWins++;
        updateRoundWinSidebar();
        if (firstPlaceRoundWins >= config.getRequiredWins()) {
            stop(firstTeamName);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void onSecondPlaceWinRound() {
        secondPlaceRoundWins++;
        updateRoundWinSidebar();
        if (secondPlaceRoundWins >= config.getRequiredWins()) {
            stop(secondTeamName);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void stop(@Nullable String winningTeam) {
        gameActive = false;
        descriptionShowing = false;
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        if (currentRoundIndex < rounds.size()) {
            ColossalCombatRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.stop();
            }
        }
        rounds.clear();
        removeConcrete();
        for (Player participant : firstPlaceParticipants) {
            resetParticipant(participant);
        }
        firstPlaceParticipants.clear();
        for (Player participant : secondPlaceParticipants) {
            resetParticipant(participant);
        }
        secondPlaceParticipants.clear();
        for (Player participant : spectators) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        killCounts.clear();
        deathCounts.clear();
        spectators.clear();
        gameManager.getEventManager().colossalCombatIsOver(winningTeam);
        Bukkit.getLogger().info("Stopping Colossal Combat");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.removePlayer(participant.getUniqueId());
        topbar.hidePlayer(participant.getUniqueId());
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
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    public void onParticipantJoin(Player participant) {
        if (!gameActive) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (firstTeamName.equals(teamName)) {
            if (descriptionShowing) {
                initializeFirstPlaceParticipant(participant);
            } else {
                firstPlaceParticipants.add(participant);
                participant.setGameMode(GameMode.SPECTATOR);
                participant.teleport(config.getFirstPlaceSpawn());
                participant.setBedSpawnLocation(config.getFirstPlaceSpawn(), true);
                sidebar.addPlayer(participant);
                topbar.showPlayer(participant);
                topbar.linkToTeam(participant.getUniqueId(), firstTeamName);
                initializeKillCount(participant);
            }
        } else if (secondTeamName.equals(teamName)) {
            if (descriptionShowing) {
                initializeSecondPlaceParticipant(participant);
            } else {
                secondPlaceParticipants.add(participant);
                participant.setGameMode(GameMode.SPECTATOR);
                participant.teleport(config.getSecondPlaceSpawn());
                participant.setBedSpawnLocation(config.getSecondPlaceSpawn(), true);
                sidebar.addPlayer(participant);
                topbar.showPlayer(participant);
                topbar.linkToTeam(participant.getUniqueId(), secondTeamName);
                initializeKillCount(participant);
            }
        } else {
            if (descriptionShowing) {
                initializeSpectator(participant);
            } else {
                spectators.add(participant);
                participant.teleport(config.getSpectatorSpawn());
                participant.setBedSpawnLocation(config.getSpectatorSpawn(), true);
                sidebar.addPlayer(participant);
                topbar.showPlayer(participant);
                topbar.linkToTeam(participant.getUniqueId(), firstTeamName);
            }
        }
        if ( 0 <= currentRoundIndex && currentRoundIndex < rounds.size()) {
            ColossalCombatRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantJoin(participant);
            }
        }
        updateRoundWinSidebar();
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round: %s", currentRoundIndex+1))
        );
    }
    
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
            return;
        }
        if (0 <= currentRoundIndex && currentRoundIndex < rounds.size()) {
            ColossalCombatRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantQuit(participant);
            }
        }
        resetParticipant(participant);
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (firstTeamName.equals(teamName)) {
            firstPlaceParticipants.remove(participant);
        } else if (secondTeamName.equals(teamName)) {
            secondPlaceParticipants.remove(participant);
        } else {
            spectators.remove(participant);
        }
    }
    
    @EventHandler
    public void onSpectatorGetDamaged(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!spectators.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
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
        Player participant = ((Player) event.getWhoClicked());
        if (spectators.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (firstPlaceParticipants.contains(participant) 
                || secondPlaceParticipants.contains(participant)) {
            // don't let them drop items from their inventory
            if (GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
                event.setCancelled(true);
                return;
            }
            // don't let them remove their armor
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!gameActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (firstPlaceParticipants.contains(participant)
                || secondPlaceParticipants.contains(participant)
                || spectators.contains(participant)) {
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
        Player participant = event.getPlayer();
        if (!firstPlaceParticipants.contains(participant)
                && !secondPlaceParticipants.contains(participant)
                && !spectators.contains(participant)) {
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
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorArea() == null){
            return;
        }
        Player participant = event.getPlayer();
        if (!firstPlaceParticipants.contains(participant)
                && !secondPlaceParticipants.contains(participant)
                && !spectators.contains(participant)) {
            return;
        }
        if (!participant.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            participant.teleport(config.getSpectatorSpawn());
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
        Player participant = event.getPlayer();
        if (!firstPlaceParticipants.contains(participant)
                && !secondPlaceParticipants.contains(participant)
                && !spectators.contains(participant)) {
            return;
        }
        if (!participant.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    private void updateRoundWinSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        sidebar.updateLine("firstWinCount", String.format("%s: %s/%s", firstDisplayName, firstPlaceRoundWins, config.getRequiredWins()));
        sidebar.updateLine("secondWinCount", String.format("%s: %s/%s", secondDisplayName, secondPlaceRoundWins, config.getRequiredWins()));
        adminSidebar.updateLine("firstWinCount", String.format("%s: %s/%s", firstDisplayName, firstPlaceRoundWins, config.getRequiredWins()));
        adminSidebar.updateLine("secondWinCount", String.format("%s: %s/%s", secondDisplayName, secondPlaceRoundWins, config.getRequiredWins()));
    }
    
    private void initializeAdminSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", String.format("%s: 0/%s", firstDisplayName, config.getRequiredWins())),
                new KeyLine("secondWinCount", String.format("%s: 0/%s", secondDisplayName, config.getRequiredWins())),
                new KeyLine("round", "Round: 1"),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        sidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", String.format("%s: 0/%s", firstDisplayName, config.getRequiredWins())),
                new KeyLine("secondWinCount", String.format("%s: 0/%s", secondDisplayName, config.getRequiredWins())),
                new KeyLine("round", "Round: 1")
        );
        topbar.setMiddle(Component.empty());
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
        topbar.removeAllTeamPairs();
        topbar.hideAllPlayers();
    }
    
    /**
     * @param playerUUID the player to add a kill to
     */
    void addKill(@NotNull UUID playerUUID) {
        int oldKillCount = killCounts.get(playerUUID);
        int newKillCount = oldKillCount + 1;
        killCounts.put(playerUUID, newKillCount);
        topbar.setKills(playerUUID, newKillCount);
    }
    
    /**
     * @param playerUUID the player to add a death to
     */
    void addDeath(@NotNull UUID playerUUID) {
        int oldDeathCount = deathCounts.get(playerUUID);
        int newDeathCount = oldDeathCount + 1;
        deathCounts.put(playerUUID, newDeathCount);
        topbar.setDeaths(playerUUID, newDeathCount);
    }
    
    void closeGates() {
        closeGate(
                config.getFirstPlaceClearArea(), 
                config.getFirstPlaceStone(), 
                config.getFirstPlacePlaceArea(), 
                gameManager.getTeamPowderColor(firstTeamName)
        );
        closeGate(
                config.getSecondPlaceClearArea(), 
                config.getSecondPlaceStone(), 
                config.getSecondPlacePlaceArea(), 
                gameManager.getTeamPowderColor(secondTeamName)
        );
        placeConcrete();
    }
    
    void placeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getFirstPlaceFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(firstTeamName));
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSecondPlaceFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(secondTeamName));
        }
    }
    
    void removeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getFirstPlaceFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(firstTeamName),
                    config.getReplaceBlock());
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSecondPlaceFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(secondTeamName),
                    config.getReplaceBlock());
        }
    }
    
    private void closeGate(BoundingBox clearArea, BoundingBox stoneArea, BoundingBox placeArea, Material teamPowderColor) {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(config.getWorld(), clearArea, powderColor, Material.AIR);
        }
        //place stone under the powder area
        BlockPlacementUtils.createCube(config.getWorld(), stoneArea, Material.STONE);
        //replace air with team powder color
        BlockPlacementUtils.createCubeReplace(config.getWorld(), placeArea, Material.AIR, teamPowderColor);
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
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : firstPlaceParticipants) {
            participant.sendMessage(message);
        }
        for (Player participant : secondPlaceParticipants) {
            participant.sendMessage(message);
        }
        for (Player participant : spectators) {
            participant.sendMessage(message);
        }
    }
    
    public boolean isActive() {
        return gameActive;
    }
}
