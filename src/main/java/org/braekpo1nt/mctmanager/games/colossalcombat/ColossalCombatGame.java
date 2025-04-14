package org.braekpo1nt.mctmanager.games.colossalcombat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
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
    private Map<UUID, ColossalParticipant> firstPlaceParticipants = new HashMap<>();
    private Map<UUID, ColossalParticipant> secondPlaceParticipants = new HashMap<>();
    private Map<UUID, ColossalQuitData> quitDatas = new HashMap<>();
    private Map<UUID, Participant> spectators = new HashMap<>();
    private List<Player> admins = new ArrayList<>();
    private List<ColossalCombatRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    private int firstPlaceRoundWins = 0;
    private int secondPlaceRoundWins = 0;
    private ColossalTeam northTeam;
    private ColossalTeam southTeam;
    private boolean descriptionShowing = false;
    private boolean gameActive = false;
    private final TimerManager timerManager;
    
    public ColossalCombatGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new ColossalCombatConfigController(plugin.getDataFolder(), "colossal-combat");
        this.topbar = new BattleTopbar();
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig(configFile);
        if (gameActive) {
            for (ColossalCombatRound round : rounds) {
                round.setConfig(this.config);
            }
        }
    }
    
    /**
     * Start the game with the first and second place teams, and the spectators.
     *
     * @param newFirstPlaceParticipants  The participants in the first place team
     * @param newSecondPlaceParticipants The participants in the second place team
     * @param newSpectators              The participants who are third place and on, who should spectate the game
     * @param newAdmins                  The admins
     */
    public void start(Team newFirst, Team newSecond, Collection<Participant> newFirstPlaceParticipants, Collection<Participant> newSecondPlaceParticipants, Collection<Participant> newSpectators, List<Player> newAdmins) {
        this.northTeam = new ColossalTeam(newFirst);
        this.southTeam = new ColossalTeam(newSecond);
        firstPlaceRoundWins = 0;
        secondPlaceRoundWins = 0;
        closeGates();
        firstPlaceParticipants = new HashMap<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new HashMap<>(newSecondPlaceParticipants.size());
        quitDatas = new HashMap<>();
        spectators = new HashMap<>(newSpectators.size());
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
    
    private void initializeFirstPlaceParticipant(Participant newParticipant) {
        initializeFirstPlaceParticipant(newParticipant, 0, 0);
    }
    
    private void initializeFirstPlaceParticipant(Participant newParticipant, int kills, int deaths) {
        ColossalParticipant participant = new ColossalParticipant(newParticipant, kills, deaths, ColossalCombatRound.Affiliation.FIRST);
        firstPlaceParticipants.put(participant.getUniqueId(), participant);
        northTeam.addParticipant(participant);
        participant.teleport(config.getNorthSpawn());
        participant.setRespawnLocation(config.getNorthSpawn(), true);
        initializeParticipant(participant);
        initializeKillCount(participant);
    }
    
    private void initializeKillCount(ColossalParticipant participant) {
        topbar.setKillsAndDeaths(participant.getUniqueId(), participant.getKills(), participant.getDeaths());
    }
    
    private void initializeSecondPlaceParticipant(Participant newParticipant) {
        initializeSecondPlaceParticipant(newParticipant, 0, 0);
    }
    
    private void initializeSecondPlaceParticipant(Participant newParticipant, int kills, int deaths) {
        ColossalParticipant participant = new ColossalParticipant(newParticipant, kills, deaths, ColossalCombatRound.Affiliation.SECOND);
        southTeam.addParticipant(participant);
        secondPlaceParticipants.put(participant.getUniqueId(), participant);
        participant.teleport(config.getSouthSpawn());
        participant.setRespawnLocation(config.getSouthSpawn(), true);
        initializeParticipant(participant);
        initializeKillCount(participant);
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
        nextRound.start(firstPlaceParticipants.values(), secondPlaceParticipants.values(), spectators.values(), northTeam, southTeam);
        sidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
        adminSidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
    }
    
    private void setUpTopbarForRound() {
        topbar.removeAllTeamPairs();
        topbar.addTeam(northTeam.getTeamId(), northTeam.getColor());
        topbar.addTeam(southTeam.getTeamId(), southTeam.getColor());
        topbar.linkTeamPair(northTeam.getTeamId(), southTeam.getTeamId());
        for (Participant firstPlaceParticipant : firstPlaceParticipants.values()) {
            topbar.linkToTeam(firstPlaceParticipant.getUniqueId(), northTeam.getTeamId());
        }
        for (Participant secondPlaceParticipant : secondPlaceParticipants.values()) {
            topbar.linkToTeam(secondPlaceParticipant.getUniqueId(), southTeam.getTeamId());
        }
        for (Participant spectator : spectators.values()) {
            topbar.linkToTeam(spectator.getUniqueId(), northTeam.getTeamId());
        }
        topbar.setMembers(northTeam.getTeamId(), firstPlaceParticipants.size(), 0);
        topbar.setMembers(southTeam.getTeamId(), secondPlaceParticipants.size(), 0);
    }
    
    public void onFirstPlaceWinRound() {
        firstPlaceRoundWins++;
        updateRoundWinSidebar();
        if (firstPlaceRoundWins >= config.getRequiredWins()) {
            stop(northTeam);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void onSecondPlaceWinRound() {
        secondPlaceRoundWins++;
        updateRoundWinSidebar();
        if (secondPlaceRoundWins >= config.getRequiredWins()) {
            stop(southTeam);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void stop(@Nullable ColossalTeam winningTeam) {
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
            northTeam.removeParticipant(participant.getUniqueId());
        }
        firstPlaceParticipants.clear();
        for (Participant participant : secondPlaceParticipants.values()) {
            resetParticipant(participant);
            southTeam.removeParticipant(participant.getUniqueId());
        }
        secondPlaceParticipants.clear();
        for (Participant participant : spectators.values()) {
            resetParticipant(participant);
        }
        quitDatas.clear();
        clearSidebar();
        stopAdmins();
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
        ColossalCombatRound.Affiliation affiliation;
        if (northTeam.getTeamId().equals(participant.getTeamId())) {
            affiliation = ColossalCombatRound.Affiliation.FIRST;
        } else if (southTeam.getTeamId().equals(participant.getTeamId())) {
            affiliation = ColossalCombatRound.Affiliation.SECOND;
        } else {
            affiliation = null;
        }
        if (affiliation != null) {
            ColossalQuitData quitData = quitDatas.remove(participant.getUniqueId());
            int kills;
            int deaths;
            if (quitData != null) {
                kills = quitData.getKills();
                deaths = quitData.getDeaths();
            } else {
                kills = 0;
                deaths = 0;
            }
            ColossalParticipant ccParticipant;
            if (affiliation == ColossalCombatRound.Affiliation.FIRST) {
                initializeFirstPlaceParticipant(participant, kills, deaths);
                ccParticipant = firstPlaceParticipants.get(participant.getUniqueId());
            } else {
                initializeSecondPlaceParticipant(participant, kills, deaths);
                ccParticipant = secondPlaceParticipants.get(participant.getUniqueId());
            }
            participant.setGameMode(GameMode.SPECTATOR);
            if (!descriptionShowing) {
                topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
            }
            if ( 0 <= currentRoundIndex && currentRoundIndex < rounds.size()) {
                ColossalCombatRound currentRound = rounds.get(currentRoundIndex);
                if (currentRound.isActive()) {
                    currentRound.onParticipantJoin(ccParticipant);
                }
            }
        } else {
            initializeSpectator(participant);
            if (!descriptionShowing) {
                topbar.linkToTeam(participant.getUniqueId(), northTeam.getTeamId());
            }
            if ( 0 <= currentRoundIndex && currentRoundIndex < rounds.size()) {
                ColossalCombatRound currentRound = rounds.get(currentRoundIndex);
                if (currentRound.isActive()) {
                    currentRound.onSpectatorJoin(participant);
                }
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
        ColossalParticipant ccParticipant;
        if (northTeam.getTeamId().equals(participant.getTeamId())) {
            ccParticipant = firstPlaceParticipants.get(participant.getUniqueId());
        } else if (southTeam.getTeamId().equals(participant.getTeamId())) {
            ccParticipant = secondPlaceParticipants.get(participant.getUniqueId());
        } else {
            resetParticipant(participant);
            spectators.remove(participant.getUniqueId());
            return;
        }
        quitDatas.put(ccParticipant.getUniqueId(), ccParticipant.getQuitData());
        resetParticipant(ccParticipant);
        if (ccParticipant.getAffiliation() == ColossalCombatRound.Affiliation.FIRST) {
            firstPlaceParticipants.remove(participant.getUniqueId());
            northTeam.removeParticipant(participant.getUniqueId());
        } else {
            secondPlaceParticipants.remove(participant.getUniqueId());
            southTeam.removeParticipant(participant.getUniqueId());
        }
    }
    
    @EventHandler
    public void onSpectatorGetDamaged(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_DAMAGE_CAUSES.contains(event.getCause())) {
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
        if (config.getSpectatorBoundary() == null){
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
        if (!config.getSpectatorBoundary().contains(event.getFrom().toVector())) {
            participant.teleport(config.getSpectatorSpawn());
            return;
        }
        if (!config.getSpectatorBoundary().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorBoundary() == null){
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
        if (!config.getSpectatorBoundary().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    private void updateRoundWinSidebar() {
        sidebar.updateLine("northWinCount", toWinCountComponent(northTeam.getFormattedDisplayName(), firstPlaceRoundWins));
        sidebar.updateLine("southWinCount", toWinCountComponent(southTeam.getFormattedDisplayName(), secondPlaceRoundWins));
        adminSidebar.updateLine("northWinCount", toWinCountComponent(northTeam.getFormattedDisplayName(), firstPlaceRoundWins));
        adminSidebar.updateLine("southWinCount", toWinCountComponent(southTeam.getFormattedDisplayName(), secondPlaceRoundWins));
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
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("northWinCount", Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("southWinCount", Component.empty()
                        .append(southTeam.getFormattedDisplayName())
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
        sidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("northWinCount", Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("southWinCount", Component.empty()
                        .append(southTeam.getFormattedDisplayName())
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
    
    void setKills(UUID uuid, ColossalCombatRound.Affiliation affiliation, int kills) {
        if (affiliation == ColossalCombatRound.Affiliation.FIRST) {
            firstPlaceParticipants.get(uuid).setKills(kills);
        } else {
            secondPlaceParticipants.get(uuid).setKills(kills);
        }
    }
    
    void setDeaths(UUID uuid, ColossalCombatRound.Affiliation affiliation, int kills) {
        if (affiliation == ColossalCombatRound.Affiliation.FIRST) {
            firstPlaceParticipants.get(uuid).setDeaths(kills);
        } else {
            secondPlaceParticipants.get(uuid).setDeaths(kills);
        }
    }
    
    void closeGates() {
        closeGate(
                config.getNorthClearArea(), 
                config.getNorthStone(), 
                config.getNorthPlaceArea(), 
                gameManager.getTeamPowderColor(northTeam.getTeamId())
        );
        closeGate(
                config.getSouthClearArea(), 
                config.getSouthStone(), 
                config.getSouthPlaceArea(), 
                gameManager.getTeamPowderColor(southTeam.getTeamId())
        );
        placeConcrete();
    }
    
    void placeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getNorthFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(northTeam.getTeamId()));
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSouthFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(southTeam.getTeamId()));
        }
    }
    
    void removeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getNorthFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(northTeam.getTeamId()),
                    config.getReplaceBlock());
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSouthFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(southTeam.getTeamId()),
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
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
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
