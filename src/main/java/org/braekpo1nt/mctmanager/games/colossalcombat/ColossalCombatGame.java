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
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
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
    private final Component title = Component.text("Colossal Combat").color(NamedTextColor.BLUE);
    private Map<UUID, Participant> firstPlaceParticipants = new HashMap<>();
    private Map<UUID, Participant> secondPlaceParticipants = new HashMap<>();
    private Map<UUID, Participant> spectators = new HashMap<>();
    private Map<UUID, Integer> killCounts = new HashMap<>();
    private Map<UUID, Integer> deathCounts = new HashMap<>();
    private List<Player> admins = new ArrayList<>();
    private List<ColossalCombatRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    private int firstPlaceRoundWins = 0;
    private int secondPlaceRoundWins = 0;
    private String firstTeamId;
    private String secondTeamId;
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
    public void start(Collection<Participant> newFirstPlaceParticipants, Collection<Participant> newSecondPlaceParticipants, Collection<Participant> newSpectators, List<Player> newAdmins) {
        firstTeamId = gameManager.getTeamId(newFirstPlaceParticipants.stream().findFirst().orElseThrow().getUniqueId());
        secondTeamId = gameManager.getTeamId(newSecondPlaceParticipants.stream().findFirst().orElseThrow().getUniqueId());
        firstPlaceRoundWins = 0;
        secondPlaceRoundWins = 0;
        closeGates();
        firstPlaceParticipants = new HashMap<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new HashMap<>(newSecondPlaceParticipants.size());
        spectators = new HashMap<>(newSpectators.size());
        killCounts = new HashMap<>(newFirstPlaceParticipants.size() + newSecondPlaceParticipants.size());
        deathCounts = new HashMap<>(newFirstPlaceParticipants.size() + newSecondPlaceParticipants.size());
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        int numOfRounds = (config.getRequiredWins() * 2) - 1;
        rounds = new ArrayList<>(numOfRounds);
        for (int i = 0; i < numOfRounds; i++) {
            rounds.add(new ColossalCombatRound(plugin, gameManager, this, config, sidebar, adminSidebar, topbar));
        }
        currentRoundIndex = 0;
        for (Participant first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Participant second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Participant spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        initializeSidebar();
        setupTeamOptions();
        startAdmins(newAdmins);
        gameActive = true;
        startDescriptionPeriod();
        displayDescription();
        Main.logger().info("Started Colossal Combat");
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeFirstPlaceParticipant(Participant first) {
        firstPlaceParticipants.put(first.getUniqueId(), first);
        first.teleport(config.getFirstPlaceSpawn());
        first.setRespawnLocation(config.getFirstPlaceSpawn(), true);
        initializeParticipant(first);
        initializeKillCount(first);
    }
    
    private void initializeKillCount(Participant participant) {
        killCounts.putIfAbsent(participant.getUniqueId(), 0);
        deathCounts.putIfAbsent(participant.getUniqueId(), 0);
        int kills = killCounts.get(participant.getUniqueId());
        int deaths = deathCounts.get(participant.getUniqueId());
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
    }
    
    private void initializeSecondPlaceParticipant(Participant second) {
        secondPlaceParticipants.put(second.getUniqueId(), second);
        second.teleport(config.getSecondPlaceSpawn());
        second.setRespawnLocation(config.getSecondPlaceSpawn(), true);
        initializeParticipant(second);
        initializeKillCount(second);
    }
    
    private void initializeSpectator(Participant spectator) {
        spectators.put(spectator.getUniqueId(), spectator);
        spectator.teleport(config.getSpectatorSpawn());
        spectator.setRespawnLocation(config.getSpectatorSpawn(), true);
        initializeParticipant(spectator);
    }
    
    /**
     * General initialization for every participant, first, second, and spectator
     * @param participant the participant
     */
    private void initializeParticipant(Participant participant) {
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
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
        topbar.showPlayer(admin);
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
        nextRound.start(firstPlaceParticipants.values(), secondPlaceParticipants.values(), spectators.values(), firstTeamId, secondTeamId);
        sidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
        adminSidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
    }
    
    private void setUpTopbarForRound() {
        topbar.removeAllTeamPairs();
        NamedTextColor firstColor = gameManager.getTeamColor(firstTeamId);
        NamedTextColor secondColor = gameManager.getTeamColor(secondTeamId);
        topbar.addTeam(firstTeamId, firstColor);
        topbar.addTeam(secondTeamId, secondColor);
        topbar.linkTeamPair(firstTeamId, secondTeamId);
        for (Participant firstPlaceParticipant : firstPlaceParticipants.values()) {
            topbar.linkToTeam(firstPlaceParticipant.getUniqueId(), firstTeamId);
        }
        for (Participant secondPlaceParticipant : secondPlaceParticipants.values()) {
            topbar.linkToTeam(secondPlaceParticipant.getUniqueId(), secondTeamId);
        }
        for (Participant spectator : spectators.values()) {
            topbar.linkToTeam(spectator.getUniqueId(), firstTeamId);
        }
        topbar.setMembers(firstTeamId, firstPlaceParticipants.size(), 0);
        topbar.setMembers(secondTeamId, secondPlaceParticipants.size(), 0);
    }
    
    public void onFirstPlaceWinRound() {
        firstPlaceRoundWins++;
        updateRoundWinSidebar();
        if (firstPlaceRoundWins >= config.getRequiredWins()) {
            stop(firstTeamId);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void onSecondPlaceWinRound() {
        secondPlaceRoundWins++;
        updateRoundWinSidebar();
        if (secondPlaceRoundWins >= config.getRequiredWins()) {
            stop(secondTeamId);
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
        for (Participant participant : firstPlaceParticipants.values()) {
            resetParticipant(participant);
        }
        firstPlaceParticipants.clear();
        for (Participant participant : secondPlaceParticipants.values()) {
            resetParticipant(participant);
        }
        secondPlaceParticipants.clear();
        for (Participant participant : spectators.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        stopAdmins();
        killCounts.clear();
        deathCounts.clear();
        spectators.clear();
        gameManager.getEventManager().colossalCombatIsOver(winningTeam);
        Main.logger().info("Stopping Colossal Combat");
    }
    
    private void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant);
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
        topbar.hidePlayer(admin.getUniqueId());
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    public void onParticipantJoin(Participant participant) {
        if (!gameActive) {
            return;
        }
        String teamId = gameManager.getTeamId(participant.getUniqueId());
        if (firstTeamId.equals(teamId)) {
            if (descriptionShowing) {
                initializeFirstPlaceParticipant(participant);
            } else {
                firstPlaceParticipants.put(participant.getUniqueId(), participant);
                participant.setGameMode(GameMode.SPECTATOR);
                participant.teleport(config.getFirstPlaceSpawn());
                participant.setRespawnLocation(config.getFirstPlaceSpawn(), true);
                sidebar.addPlayer(participant);
                topbar.showPlayer(participant);
                topbar.linkToTeam(participant.getUniqueId(), firstTeamId);
                initializeKillCount(participant);
            }
        } else if (secondTeamId.equals(teamId)) {
            if (descriptionShowing) {
                initializeSecondPlaceParticipant(participant);
            } else {
                secondPlaceParticipants.put(participant.getUniqueId(), participant);
                participant.setGameMode(GameMode.SPECTATOR);
                participant.teleport(config.getSecondPlaceSpawn());
                participant.setRespawnLocation(config.getSecondPlaceSpawn(), true);
                sidebar.addPlayer(participant);
                topbar.showPlayer(participant);
                topbar.linkToTeam(participant.getUniqueId(), secondTeamId);
                initializeKillCount(participant);
            }
        } else {
            if (descriptionShowing) {
                initializeSpectator(participant);
            } else {
                spectators.put(participant.getUniqueId(), participant);
                participant.teleport(config.getSpectatorSpawn());
                participant.setRespawnLocation(config.getSpectatorSpawn(), true);
                sidebar.addPlayer(participant);
                topbar.showPlayer(participant);
                topbar.linkToTeam(participant.getUniqueId(), firstTeamId);
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
    
    public void onParticipantQuit(Participant participant) {
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
        String teamId = gameManager.getTeamId(participant.getUniqueId());
        if (firstTeamId.equals(teamId)) {
            firstPlaceParticipants.remove(participant.getUniqueId());
        } else if (secondTeamId.equals(teamId)) {
            secondPlaceParticipants.remove(participant.getUniqueId());
        } else {
            spectators.remove(participant.getUniqueId());
        }
    }
    
    @EventHandler
    public void onSpectatorGetDamaged(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!spectators.containsKey(event.getEntity().getUniqueId())) {
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
        Player player = ((Player) event.getWhoClicked());
        if (spectators.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (firstPlaceParticipants.containsKey(player.getUniqueId()) 
                || secondPlaceParticipants.containsKey(player.getUniqueId())) {
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
        if (firstPlaceParticipants.containsKey(participant.getUniqueId())
                || secondPlaceParticipants.containsKey(participant.getUniqueId())
                || spectators.containsKey(participant.getUniqueId())) {
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
        if (!firstPlaceParticipants.containsKey(participant.getUniqueId())
                && !secondPlaceParticipants.containsKey(participant.getUniqueId())
                && !spectators.containsKey(participant.getUniqueId())) {
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
        if (!firstPlaceParticipants.containsKey(participant.getUniqueId())
                && !secondPlaceParticipants.containsKey(participant.getUniqueId())
                && !spectators.containsKey(participant.getUniqueId())) {
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
        if (!firstPlaceParticipants.containsKey(participant.getUniqueId())
                && !secondPlaceParticipants.containsKey(participant.getUniqueId())
                && !spectators.containsKey(participant.getUniqueId())) {
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
        Component firstDisplayName = gameManager.getFormattedTeamDisplayName(firstTeamId);
        Component secondDisplayName = gameManager.getFormattedTeamDisplayName(secondTeamId);
        sidebar.updateLine("firstWinCount", toWinCountComponent(firstDisplayName, firstPlaceRoundWins));
        sidebar.updateLine("secondWinCount", toWinCountComponent(secondDisplayName, secondPlaceRoundWins));
        adminSidebar.updateLine("firstWinCount", toWinCountComponent(firstDisplayName, firstPlaceRoundWins));
        adminSidebar.updateLine("secondWinCount", toWinCountComponent(secondDisplayName, secondPlaceRoundWins));
    }
    
    private Component toWinCountComponent(Component teamDisplayName, int roundWins) {
        return Component.empty()
                .append(teamDisplayName)
                .append(Component.text(": "))
                .append(Component.text(roundWins))
                .append(Component.text("/"))
                .append(Component.text(config.getRequiredWins()))
                ;
    }
    
    private void initializeAdminSidebar() {
        Component firstDisplayName = gameManager.getFormattedTeamDisplayName(firstTeamId);
        Component secondDisplayName = gameManager.getFormattedTeamDisplayName(secondTeamId);
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", Component.empty()
                        .append(firstDisplayName)
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("secondWinCount", Component.empty()
                        .append(secondDisplayName)
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("round", Component.text("Round: 1")),
                new KeyLine("timer", Component.empty())
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        Component firstDisplayName = gameManager.getFormattedTeamDisplayName(firstTeamId);
        Component secondDisplayName = gameManager.getFormattedTeamDisplayName(secondTeamId);
        sidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", Component.empty()
                        .append(firstDisplayName)
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("secondWinCount", Component.empty()
                        .append(secondDisplayName)
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("round", Component.text("Round: 1"))
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
                gameManager.getTeamPowderColor(firstTeamId)
        );
        closeGate(
                config.getSecondPlaceClearArea(), 
                config.getSecondPlaceStone(), 
                config.getSecondPlacePlaceArea(), 
                gameManager.getTeamPowderColor(secondTeamId)
        );
        placeConcrete();
    }
    
    void placeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getFirstPlaceFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(firstTeamId));
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSecondPlaceFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(secondTeamId));
        }
    }
    
    void removeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getFirstPlaceFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(firstTeamId),
                    config.getReplaceBlock());
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSecondPlaceFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(secondTeamId),
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
        for (Participant participant : firstPlaceParticipants.values()) {
            participant.sendMessage(message);
        }
        for (Participant participant : secondPlaceParticipants.values()) {
            participant.sendMessage(message);
        }
        for (Participant participant : spectators.values()) {
            participant.sendMessage(message);
        }
    }
    
    public boolean isActive() {
        return gameActive;
    }
}
