package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParkourPathwayGame implements MCTGame, Configurable, Listener, Headerable {

    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final ParkourPathwayConfigController configController;
    private ParkourPathwayConfig config;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Parkour Pathway"))
            .color(NamedTextColor.BLUE);
    private Component title = baseTitle;
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private int statusEffectsTaskId;
    private @Nullable Timer mercryRuleCountdown;
    private boolean gameActive = false;
    private boolean parkourHasStarted = false;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    /**
     * Participants who have reached the finish line
     */
    private List<UUID> finishedParticipants;
    /**
     * Holds the {@link TeamSpawn}s for this game
     */
    private @Nullable Map<String, @NotNull TeamSpawn> teamSpawns = new HashMap<>();
    /**
     * The index of the puzzle each participant is solving
     */
    private Map<UUID, Integer> currentPuzzles;
    /**
     * holds the number of skips a participant should have if they quit
     * mid-game and come back
     */
    private Map<UUID, Integer> quitParticipantsSkips;
    /**
     * The index of the checkpoint that a player is associated with for their current puzzle (since there can be multiple)
     */
    private Map<UUID, Integer> currentPuzzleCheckpoints;
    private boolean descriptionShowing = false;
    private final TimerManager timerManager;
    
    public ParkourPathwayGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder());
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
        if (sidebar != null) {
            sidebar.updateLine("title", title);
        }
        if (adminSidebar != null) {
            adminSidebar.updateLine("title", title);
        }
    }
    
    @Override
    public @NotNull Component getBaseTitle() {
        return baseTitle;
    }
    
    @Override
    public GameType getType() {
        return GameType.PARKOUR_PATHWAY;
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        participants = new ArrayList<>(newParticipants.size());
        currentPuzzles = new HashMap<>(newParticipants.size());
        quitParticipantsSkips = new HashMap<>();
        currentPuzzleCheckpoints = new HashMap<>(newParticipants.size());
        finishedParticipants = new ArrayList<>();
        List<String> teams = gameManager.getTeamIds(newParticipants);
        teamSpawns = getTeamSpawns(teams);
        closeTeamSpawns();
        closeGlassBarrier();
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        parkourHasStarted = false;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        startStatusEffectsTask();
        setupTeamOptions();
        displayDescription();
        gameActive = true;
        startDescriptionPeriod();
        Main.logger().info("Starting Parkour Pathway game");
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        currentPuzzles.put(participant.getUniqueId(), 0);
        currentPuzzleCheckpoints.put(participant.getUniqueId(), 0);
        sidebar.addPlayer(participant);
        if (teamSpawns == null) {
            participant.teleport(config.getStartingLocation());
        } else {
            String team = gameManager.getTeamId(participant.getUniqueId());
            teamSpawns.get(team).teleport(participant);
        }
        participant.setRespawnLocation(config.getStartingLocation(), true);
        ParticipantInitializer.clearInventory(participant);
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        openGlassBarrier();
        openTeamSpawns();
        clearTeamSpawns();
        clearSidebar();
        stopAdmins();
        participants.clear();
        finishedParticipants.clear();
        descriptionShowing = false;
        parkourHasStarted = false;
        gameActive = false;
        gameManager.gameIsOver();
        quitParticipantsSkips = null;
        Main.logger().info("Stopping Parkour Pathway game");
    }
    
    private void resetParticipant(Player participant) {
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        sidebar.removePlayer(participant);
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
    
    @Override
    public void onParticipantJoin(Player participant) {
        participants.add(participant);
        UUID uniqueId = participant.getUniqueId();
        currentPuzzles.putIfAbsent(uniqueId, 0);
        currentPuzzleCheckpoints.putIfAbsent(uniqueId, 0);
        sidebar.addPlayer(participant);
        ParticipantInitializer.clearInventory(participant);
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        if (parkourHasStarted) {
            Location respawn = config.getPuzzle(currentPuzzles.get(uniqueId)).checkPoints().get(currentPuzzleCheckpoints.get(uniqueId)).respawn();
            Main.logger().info(String.format("teleported %s to %s", participant.getName(), respawn.toString()));
            participant.teleport(respawn);
            Integer unusedSkips = quitParticipantsSkips.remove(uniqueId);
            if (unusedSkips != null) {
                if (unusedSkips > 0) {
                    giveSkipItem(participant, unusedSkips);
                }
            } else {
                giveSkipItem(participant, config.getNumOfSkips());
            }
        } else {
            if (teamSpawns == null) {
                Main.logger().info(String.format("teleported %s to %s", participant.getName(), config.getStartingLocation().toString()));
                participant.teleport(config.getStartingLocation());
            } else {
                String team = gameManager.getTeamId(participant.getUniqueId());
                TeamSpawn teamSpawn = teamSpawns.get(team);
                if (teamSpawn == null) {
                    Main.logger().info("re-setup team-spawns");
                    reSetUpTeamSpawns();
                } else {
                    Main.logger().info(String.format("teleported %s to %s", participant.getName(), teamSpawn.getSpawnLocation().toString()));
                    teamSpawn.teleport(participant);
                }
            }
        }
        participant.setRespawnLocation(config.getStartingLocation(), true);
        sidebar.updateLine(uniqueId, "title", title);
        updateCheckpointSidebar(participant);
    }
    
    /**
     * meant to be called when a new team joins the game while the team spawns 
     * countdown is still going on
     */
    private void reSetUpTeamSpawns() {
        List<String> teams = gameManager.getTeamIds(participants);
        teamSpawns = getTeamSpawns(teams);
        if (teamSpawns == null) {
            return;
        }
        closeTeamSpawns();
        for (Player participant : participants) {
            String team = gameManager.getTeamId(participant.getUniqueId());
            TeamSpawn teamSpawn = teamSpawns.get(team);
            Main.logger().info(String.format("teleported %s to %s", participant.getName(), teamSpawn.getSpawnLocation().toString()));
            teamSpawn.teleport(participant);
        }
    }
    
    /**
     * If {@link ParkourPathwayGame#teamSpawns} is not null, sets all barrierMaterials to {@link Material#AIR} and clears the map of elements.
     */
    private void clearTeamSpawns() {
        if (teamSpawns == null) {
            return;
        }
        for (TeamSpawn teamSpawn : teamSpawns.values()) {
            teamSpawn.setBarrierMaterial(Material.AIR);
        }
        teamSpawns.clear();
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (parkourHasStarted) {
            int unusedSkips = calculateUnusedSkips(participant);
            quitParticipantsSkips.put(participant.getUniqueId(), unusedSkips);
        }
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        timerManager.start(Timer.builder()
                .duration(config.getDescriptionDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    descriptionShowing = false;
                    if (teamSpawns != null) {
                        startTeamSpawnsCountDown();
                    } else {
                        startStartGameCountDown();
                    }
                })
                .build());
    }
    
    /**
     * The countdown which takes place while the teams are in their respective spawns
     */
    private void startTeamSpawnsCountDown() {
        timerManager.start(Timer.builder()
                .duration(config.getTeamSpawnsDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Opening in: "))
                .onCompletion(() -> {
                    if (config.getTeamSpawnsOpenMessage() != null) {
                        messageAllParticipants(config.getTeamSpawnsOpenMessage());
                    }
                    openTeamSpawns();
                    startStartGameCountDown();
                })
                .build());
    }
    
    /**
     * The countdown which takes place while the participants are waiting for the big glass barrier to drop
     */
    private void startStartGameCountDown() {
        timerManager.start(Timer.builder()
                .duration(config.getStartingDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Starting: "))
                .titleAudience(Audience.audience(participants))
                .onCompletion(() -> {
                    if (config.getGlassBarrierOpenMessage() != null) {
                        messageAllParticipants(config.getGlassBarrierOpenMessage());
                    }
                    openGlassBarrier();
                    startParkourPathwayTimer();
                    restartMercyRuleCountdown();
                })
                .build());
    }
    
    /**
     * gives all players the initial appropriate number of skip items
     */
    private void giveSkipItems() {
        if (config.getNumOfSkips() <= 0) {
            return;
        }
        for (Player participant : participants) {
            giveSkipItem(participant, config.getNumOfSkips());
        }
    }
    
    /**
     * Gives the appropriate number of skips to the given participant
     * @param participant the participant to receive skips
     */
    private void giveSkipItem(Player participant, int numOfSkips) {
        participant.getInventory().setItem(8, config.getSkipItem().asQuantity(numOfSkips));
    }
    
    private void closeGlassBarrier() {
        BoundingBox glassBarrier = config.getGlassBarrier();
        if (glassBarrier == null) {
            return;
        }
        BlockPlacementUtils.createCubeReplace(config.getWorld(), glassBarrier, Material.AIR, Material.GLASS);
        BlockPlacementUtils.updateDirection(config.getWorld(), glassBarrier);
    }
    
    private void openGlassBarrier() {
        BoundingBox glassBarrier = config.getGlassBarrier();
        if (glassBarrier == null) {
            return;
        }
        BlockPlacementUtils.createCubeReplace(config.getWorld(), glassBarrier, Material.GLASS, Material.AIR);
    }
    
    private void closeTeamSpawns() {
        if (teamSpawns == null) {
            return;
        }
        for (TeamSpawn teamSpawn : teamSpawns.values()) {
            teamSpawn.close();
        }
    }
    
    private void openTeamSpawns() {
        if (teamSpawns == null) {
            return;
        }
        for (TeamSpawn teamSpawn : teamSpawns.values()) {
            teamSpawn.open();
        }
    }
    
    /**
     * @param teams the teams to get the spawns for. If there are more teams than spawns in the config, some teams will be in the same spawn.
     * @return a map of teamIds to their {@link TeamSpawn}s. Null if the config never specified a list of {@link TeamSpawn}s.
     */
    private @Nullable Map<String, @NotNull TeamSpawn> getTeamSpawns(@NotNull List<String> teams) {
        List<TeamSpawn> teamSpawns = config.getTeamSpawns();
        if (teamSpawns == null) {
            return null;
        }
        Map<String, TeamSpawn> result = new HashMap<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            String team = teams.get(i);
            int teamSpawnIndex = MathUtils.wrapIndex(i, teamSpawns.size());
            TeamSpawn teamSpawn = teamSpawns.get(teamSpawnIndex);
            teamSpawn.setBarrierMaterial(gameManager.getTeamStainedGlassColor(team));
            result.put(team, teamSpawn);
        }
        return result;
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!gameActive) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "ParkourPathwayGame.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    /**
     * Stop players from removing their equipment
     */
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
        if (!participants.contains(participant)) {
            return;
        }
        event.setCancelled(true);
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
        if (!participants.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!gameActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        onParticipantMove(participant);
        if (participant.getGameMode().equals(GameMode.SPECTATOR)) {
            keepSpectatorsInArea(participant, event);
        }
    }
    
    /**
     * Handle when a participant moves
     * @param participant the participant. Assumed to be a valid participant in this game
     */
    private void onParticipantMove(@NotNull Player participant) {
        UUID uuid = participant.getUniqueId();
        if (finishedParticipants.contains(uuid)) {
            return;
        }
        int currentPuzzleIndex = currentPuzzles.get(uuid);
        int nextPuzzleIndex = currentPuzzleIndex + 1;
        if (nextPuzzleIndex >= config.getPuzzlesSize()) {
            // should not occur because of above check
            return;
        }
        Puzzle currentPuzzle = config.getPuzzle(currentPuzzleIndex);
        if (!currentPuzzle.isInBounds(participant.getLocation().toVector())) {
            onParticipantOutOfBounds(participant, currentPuzzle);
            return;
        }
        Puzzle nextPuzzle = config.getPuzzle(nextPuzzleIndex);
        int nextPuzzleCheckPointIndex = participantReachedCheckPoint(participant.getLocation().toVector(), nextPuzzle);
        if (nextPuzzleCheckPointIndex >= 0) {
            onParticipantReachCheckpoint(participant, nextPuzzleIndex, nextPuzzleCheckPointIndex);
            return;
        }
        int parallelCheckPointIndex = participantReachedCheckPoint(participant.getLocation().toVector(), currentPuzzle);
        if (parallelCheckPointIndex >= 0) {
            int currentCheckpoint = currentPuzzleCheckpoints.get(uuid);
            if (parallelCheckPointIndex == currentCheckpoint) {
                return;
            }
            currentPuzzleCheckpoints.put(uuid, parallelCheckPointIndex);
        }
    }
    
    /**
     * Prevent spectators from leaving the spectatorArea
     * @param participant the participant (assumed to be a valid participant of this game in the SPECTATOR gamemode
     * @param event the event which may be cancelled in order to keep the given participant in the spectator area
     */
    private void keepSpectatorsInArea(@NotNull Player participant, PlayerMoveEvent event) {
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            participant.teleport(config.getStartingLocation());
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        Action action = event.getAction();
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            preventBlockInteractions(event);
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getItemMeta().equals(config.getSkipItem().getItemMeta())) {
            performCheckpointSkip(participant);
        }
    }
    
    private void preventBlockInteractions(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
    }
    
    private void performCheckpointSkip(Player participant) {
        UUID uuid = participant.getUniqueId();
        if (finishedParticipants.contains(uuid)) {
            return;
        }
        int currentPuzzleIndex = currentPuzzles.get(uuid);
        int nextPuzzleIndex = currentPuzzleIndex + 1;
        if (nextPuzzleIndex >= config.getPuzzlesSize()) {
            // should not occur because of above check
            return;
        }
        participant.getInventory().removeItemAnySlot(config.getSkipItem());
        onParticipantSkippedToCheckpoint(participant, nextPuzzleIndex);
    }
    
    private void onParticipantSkippedToCheckpoint(Player participant, int puzzleIndex) {
        UUID uuid = participant.getUniqueId();
        currentPuzzles.put(uuid, puzzleIndex);
        currentPuzzleCheckpoints.put(uuid, 0);
        updateCheckpointSidebar(participant);
        Puzzle newPuzzle = config.getPuzzle(puzzleIndex);
        participant.teleport(newPuzzle.checkPoints().getFirst().respawn());
        if (puzzleIndex >= config.getPuzzlesSize()-1) {
            onParticipantFinish(participant, false);
        } else {
            Component checkpointNum = Component.empty()
                    .append(Component.text(puzzleIndex))
                    .append(Component.text("/"))
                    .append(Component.text(config.getPuzzlesSize()-1));
            messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" skipped to checkpoint "))
                    .append(checkpointNum));
            participant.showTitle(UIUtils.defaultTitle(
                    Component.empty(),
                    Component.empty()
                            .append(Component.text("Checkpoint "))
                            .append(checkpointNum)
                            .color(NamedTextColor.YELLOW)
            ));
            
            if (config.getMaxSkipPuzzle() > 0) {
                if (puzzleIndex == config.getMaxSkipPuzzle()) {
                    participant.sendMessage(Component.empty()
                            .append(Component.text("Skips are not allowed after checkpoint "))
                            .append(Component.text(config.getMaxSkipPuzzle())));
                    awardPointsForUnusedSkips(participant);
                }
            }
        }
        if (allParticipantsHaveFinished()) {
            for (Player p : participants) {
                awardPointsForUnusedSkips(p);
                p.setGameMode(GameMode.SPECTATOR);
            }
            stop();
            return;
        }
        restartMercyRuleCountdown();
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!gameActive) {
            return;
        }
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.contains(event.getPlayer())) {
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
    
    private void onParticipantOutOfBounds(Player participant, Puzzle currentPuzzle) {
        CheckPoint currentCheckPoint = currentPuzzle.checkPoints().get(currentPuzzleCheckpoints.get(participant.getUniqueId()));
        Location respawn = currentCheckPoint.respawn().setDirection(participant.getLocation().getDirection());
        participant.teleport(respawn);
    }
    
    private void onParticipantReachCheckpoint(Player participant, int puzzleIndex, int puzzleCheckPointIndex) {
        UUID uuid = participant.getUniqueId();
        currentPuzzles.put(uuid, puzzleIndex);
        currentPuzzleCheckpoints.put(uuid, puzzleCheckPointIndex);
        updateCheckpointSidebar(participant);
        if (puzzleIndex >= config.getPuzzlesSize()-1) {
            onParticipantFinish(participant, true);
        } else {
            Component checkpointNum = Component.empty()
                    .append(Component.text(puzzleIndex))
                    .append(Component.text("/"))
                    .append(Component.text(config.getPuzzlesSize()-1));
            messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" reached checkpoint "))
                    .append(checkpointNum));
            participant.showTitle(UIUtils.defaultTitle(
                    Component.empty(),
                    Component.empty()
                            .append(Component.text("Checkpoint "))
                            .append(checkpointNum)
                            .color(NamedTextColor.YELLOW)
            ));
            int playersCheckpoint = currentPuzzles.get(uuid);
            int points = calculatePointsForPuzzle(playersCheckpoint, config.getCheckpointScore());
            gameManager.awardPointsToParticipant(participant, points);
            
            if (config.getMaxSkipPuzzle() > 0) {
                if (puzzleIndex == config.getMaxSkipPuzzle()) {
                    participant.sendMessage(Component.empty()
                            .append(Component.text("Skips are not allowed after checkpoint "))
                            .append(Component.text(config.getMaxSkipPuzzle())));
                    awardPointsForUnusedSkips(participant);
                }
            }
        }
        if (allParticipantsHaveFinished()) {
            for (Player p : participants) {
                awardPointsForUnusedSkips(p);
                p.setGameMode(GameMode.SPECTATOR);
            }
            stop();
            return;
        }
        restartMercyRuleCountdown();
    }
    
    /**
     * Check if the given location is inside the given puzzle's check points.
     * @param v the location to check if it's inside the puzzle's detection areas or not.
     * @param puzzle the puzzle to check if the player reached
     * @return -1 if v isn't inside the given puzzle's detection areas. Otherwise, returns the index of the puzzle's CheckPoint that v is inside.
     */
    private static int participantReachedCheckPoint(Vector v, Puzzle puzzle) {
        for (int i = 0; i < puzzle.checkPoints().size(); i++) {
            CheckPoint nextCheckPoint = puzzle.checkPoints().get(i);
            if (nextCheckPoint.detectionArea().contains(v)) {
                return i;
            }
        }
        return -1;
    }
    
    private void onParticipantFinish(Player participant, boolean awardPoints) {
        participant.showTitle(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text("You finished!"))
                        .color(NamedTextColor.GREEN),
                Component.empty()
                        .append(Component.text("Well done"))
                        .color(NamedTextColor.GREEN)
        ));
        messageAllParticipants(Component.empty()
                .append(Component.text(participant.getName()))
                .append(Component.text(" finished!"))
                .color(NamedTextColor.GREEN)
        );
        if (awardPoints) {
            int points = calculatePointsForWin(config.getWinScore());
            gameManager.awardPointsToParticipant(participant, points);
        }
        awardPointsForUnusedSkips(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        finishedParticipants.add(participant.getUniqueId());
    }
    
    /**
     * Checks how many skips the player has, and awards them points for any remaining
     * skips. Removes them from their inventory as well.
     */
    private void awardPointsForUnusedSkips(Player participant) {
        if (config.getUnusedSkipScore() <= 0.0) {
            return;
        }
        int unusedSkips = calculateUnusedSkips(participant);
        if (unusedSkips > 0) {
            participant.sendMessage(Component.empty()
                    .append(Component.text(unusedSkips))
                    .append(Component.text(" unused skips"))
                    .color(NamedTextColor.GREEN));
            gameManager.awardPointsToParticipant(participant, 
                    unusedSkips * config.getUnusedSkipScore());
        }
        ParticipantInitializer.clearInventory(participant);
    }
    
    /**
     * @param participant the participant
     * @return the number of unused skips in the participant's inventory
     */
    private int calculateUnusedSkips(Player participant) {
        return Arrays.stream(participant.getInventory().getContents()).filter(itemStack -> {
            if (itemStack == null) {
                return false;
            }
            return itemStack.getItemMeta().equals(config.getSkipItem().getItemMeta());
        }).mapToInt(ItemStack::getAmount).sum();
    }
    
    private boolean allParticipantsHaveFinished() {
        for (Player participant : participants) {
            int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
            if (currentPuzzleIndex < config.getPuzzlesSize() - 1) {
                //at least one player is still playing
                return false;
            }
        }
        //all players are at finish line
        return true;
    }
    
    /**
     * Calculates the points for playersPuzzle based on how many players have reached or passed that playersPuzzle. If puzzleScores has x elements, the nth player to arrive at playersPuzzle gets the puzzleScores[n-1], unless n is greater than or equal to x, in which case they get puzzleScores[x-1]
     * @param playersPuzzle the index of the puzzle to get the points for
     * @param puzzleScores the scores to progress through. The last score is to give to everyone who didn't make the one of the other specified scores.
     * @return the points for playersPuzzle
     */
    private int calculatePointsForPuzzle(int playersPuzzle, int[] puzzleScores) {
        int numWhoReachedOrPassedCheckpoint = 0;
        for (int puzzleIndex : currentPuzzles.values()) {
            if (puzzleIndex >= playersPuzzle) {
                numWhoReachedOrPassedCheckpoint++;
            }
        }
        if (numWhoReachedOrPassedCheckpoint < puzzleScores.length) {
            return puzzleScores[numWhoReachedOrPassedCheckpoint - 1];
        } else {
            return puzzleScores[puzzleScores.length - 1];
        }
    }
    
    /**
     * Calculates the number of points for a win, based on how many players have currently won. If winScores has x elements, the nth player to win will get winScores[n-1] points, unless n is greater than or equal to x in which case they get winScores[x-1]
     * @param winScores the scores to progress through. The last score is to give to everyone who didn't make one of the other specified scores. 
     * @return the points for the most recent player win
     */
    private int calculatePointsForWin(int[] winScores) {
        int numberOfWins = 0;
        for (int puzzleIndex : currentPuzzles.values()) {
            if (puzzleIndex >= config.getPuzzlesSize() - 1) {
                numberOfWins++;
            }
        }
        if (numberOfWins < winScores.length) {
            return winScores[numberOfWins - 1];
        } else {
            return winScores[winScores.length - 1];
        }
    }
    
    private void restartMercyRuleCountdown() {
        if (this.mercryRuleCountdown != null) {
            this.mercryRuleCountdown.cancel();
        }
        sidebar.updateLine("ending", "");
        adminSidebar.updateLine("ending", "");
        this.mercryRuleCountdown = Timer.builder()
                .duration(config.getMercyRuleDuration())
                .completionSeconds(config.getMercyRuleAlertDuration())
                .withSidebar(adminSidebar, "ending")
                .sidebarPrefix(Component.text("Mercy Rule: "))
                .onCompletion(() -> {
                    Component timeLeft = TimeStringUtils.getTimeComponent(config.getMercyRuleAlertDuration());
                    messageAllParticipants(Component.text("No one has reached a new checkpoint in the last ")
                            .append(TimeStringUtils.getTimeComponent(config.getMercyRuleDuration()))
                            .append(Component.text(". Ending in "))
                            .append(timeLeft)
                            .append(Component.text("."))
                            .color(NamedTextColor.RED));
                    Audience.audience(participants).showTitle(UIUtils.defaultTitle(
                            Component.empty(), 
                            Component.empty()
                                .append(timeLeft)
                                .append(Component.text(" left"))
                                .color(NamedTextColor.RED))
                    );
                    startMercyRuleFinalCountdown();
                })
                .build().start(plugin);
    }
    
    private void startMercyRuleFinalCountdown() {
        if (this.mercryRuleCountdown != null) {
            this.mercryRuleCountdown.cancel();
        }
        this.mercryRuleCountdown = Timer.builder()
                .duration(config.getMercyRuleAlertDuration())
                .withSidebar(sidebar, "ending")
                .withSidebar(adminSidebar, "ending")
                .sidebarPrefix(Component.text("Ending in: ")
                        .color(NamedTextColor.RED))
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> {
                    messageAllParticipants(Component.text("No one has reached a new checkpoint in the last ")
                            .append(TimeStringUtils.getTimeComponent(config.getMercyRuleDuration()))
                            .append(Component.text(". Stopping early")));
                    for (Player participant : participants) {
                        awardPointsForUnusedSkips(participant);
                        participant.setGameMode(GameMode.SPECTATOR);
                    }
                    stop();
                })
                .build().start(plugin);
    }
    
    /**
     * The time limit for the entire match
     */
    private void startParkourPathwayTimer() {
        parkourHasStarted = true;
        giveSkipItems();
        timerManager.start(Timer.builder()
                .duration(config.getTimeLimitDuration())
                .completionSeconds(config.getMercyRuleAlertDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .onCompletion(this::startEndingGameTimer)
                .build());
    }
    
    /**
     * A different timer color for the last 30 seconds (or whatever is configured)
     */
    private void startEndingGameTimer() {
        messageAllParticipants(Component.empty()
                .append(Component.text("Ending in "))
                .append(TimeStringUtils.getTimeComponent(config.getMercyRuleAlertDuration()))
                .color(NamedTextColor.RED));
        if (mercryRuleCountdown != null) {
            mercryRuleCountdown.cancel();
        }
        timerManager.start(Timer.builder()
                .duration(config.getMercyRuleAlertDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> {
                    for (Player participant : participants) {
                        awardPointsForUnusedSkips(participant);
                        participant.setGameMode(GameMode.SPECTATOR);
                    }
                    stop();
                })
                .build());
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player participant : participants) {
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void giveBoots(Player participant) {
        Color teamColor = gameManager.getTeamColor(participant.getUniqueId());
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
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
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        if (mercryRuleCountdown != null) {
            mercryRuleCountdown.cancel();
        }
        timerManager.cancel();
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("timer", ""),
                new KeyLine("ending", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }

    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("timer", ""),
                new KeyLine("checkpoint", String.format("0/%s", config.getPuzzlesSize() - 1)),
                new KeyLine("ending", "")
        );
    }
    
    private void updateCheckpointSidebar(Player participant) {
        int currentCheckpoint = currentPuzzles.get(participant.getUniqueId());
        int lastCheckpoint = config.getPuzzlesSize()-1;
        sidebar.updateLine(participant.getUniqueId(), "checkpoint", String.format("%s/%s", currentCheckpoint, lastCheckpoint));
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    @Override
    public void updateTeamScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    @Override
    public void updatePersonalScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
}
