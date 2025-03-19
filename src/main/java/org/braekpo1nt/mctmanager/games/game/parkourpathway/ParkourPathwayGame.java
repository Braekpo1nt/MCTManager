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
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParkourPathwayGame implements MCTGame, Configurable, Listener {

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
    private Map<UUID, ParkourParticipant> participants = new HashMap<>();
    private Map<UUID, ParkourParticipant.QuitData> quitDatas = new HashMap<>();
    private Map<String, ParkourTeam> teams = new HashMap<>();
    private Map<String, ParkourTeam.QuitData> teamQuitDatas = new HashMap<>();
    private List<Player> admins = new ArrayList<>();
    /**
     * Holds the {@link TeamSpawn}s for this game
     */
    private @Nullable Map<String, @NotNull TeamSpawn> teamSpawns = new HashMap<>();
    private boolean descriptionShowing = false;
    private final TimerManager timerManager;
    
    public ParkourPathwayGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder(), getType().getId());
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
    public void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig(configFile);
    }
    
    @Override
    public void start(Collection<Team> newTeams, Collection<Participant> newParticipants, List<Player> newAdmins) {
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        for (Team newTeam : newTeams) {
            ParkourTeam team = new ParkourTeam(newTeam, 0);
            this.teams.put(team.getTeamId(), team);
        }
        teamSpawns = getTeamSpawns(Team.getTeamIds(teams));
        closeTeamSpawns();
        closeGlassBarrier();
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        parkourHasStarted = false;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Participant participant : newParticipants) {
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
    
    private void initializeParticipant(Participant newParticipant) {
        ParkourParticipant participant = new ParkourParticipant(newParticipant, 0);
        teams.get(participant.getTeamId()).addParticipant(participant);
        participants.put(participant.getUniqueId(), participant);
        sidebar.addPlayer(participant);
        if (teamSpawns == null) {
            participant.teleport(config.getStartingLocation());
        } else {
            teamSpawns.get(participant.getTeamId()).teleport(participant);
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
        saveScores();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        openGlassBarrier();
        openTeamSpawns();
        clearTeamSpawns();
        clearSidebar();
        stopAdmins();
        participants.clear();
        teams.clear();
        quitDatas.clear();
        teamQuitDatas.clear();
        descriptionShowing = false;
        parkourHasStarted = false;
        gameActive = false;
        gameManager.gameIsOver();
        Main.logger().info("Stopping Parkour Pathway game");
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (ParkourTeam team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (ParkourParticipant participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (Map.Entry<String, ParkourTeam.QuitData> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, ParkourParticipant.QuitData> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    private void resetParticipant(Participant participant) {
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
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
    
    private void onTeamJoin(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        ParkourTeam.QuitData quitData = teamQuitDatas.get(team.getTeamId());
        if (quitData != null) {
            teams.put(team.getTeamId(), new ParkourTeam(team, quitData.getScore()));
        } else {
            teams.put(team.getTeamId(), new ParkourTeam(team, 0));
        }
    }
    
    
    @Override
    public void onParticipantJoin(Participant newParticipant, Team team) {
        onTeamJoin(team);
        ParkourParticipant.QuitData quitData = quitDatas.remove(newParticipant.getUniqueId());
        ParkourParticipant participant;
        if (quitData != null) {
            participant = new ParkourParticipant(newParticipant, quitData);
        } else {
            participant = new ParkourParticipant(newParticipant, 0);
        }
        // initialize participant start
        participants.put(participant.getUniqueId(), participant);
        sidebar.addPlayer(participant);
        ParticipantInitializer.clearInventory(participant);
        giveBoots(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        // initialize participant end
        if (parkourHasStarted) {
            if (quitData != null) {
                if (quitData.getNumOfSkips() > 0) {
                    giveSkipItem(participant, quitData.getNumOfSkips());
                }
            }
            Location respawn = config.getPuzzle(participant.getCurrentPuzzle()).checkPoints().get(participant.getCurrentPuzzleCheckpoint()).respawn();
            participant.teleport(respawn);
        } else {
            if (teamSpawns == null) {
                participant.teleport(config.getStartingLocation());
            } else {
                TeamSpawn teamSpawn = teamSpawns.get(participant.getTeamId());
                if (teamSpawn == null) {
                    reSetUpTeamSpawns();
                } else {
                    teamSpawn.teleport(participant);
                }
            }
        }
        participant.setRespawnLocation(config.getStartingLocation(), true);
        sidebar.updateLine(participant.getUniqueId(), "title", title);
        updateCheckpointSidebar(participant);
        displayScore(participants.get(participant.getUniqueId()));
        displayScore(teams.get(team.getTeamId()));
    }
    
    /**
     * meant to be called when a new team joins the game while the team spawns 
     * countdown is still going on
     */
    private void reSetUpTeamSpawns() {
        Set<String> teams = Participant.getTeamIds(participants);
        teamSpawns = getTeamSpawns(teams);
        if (teamSpawns == null) {
            return;
        }
        closeTeamSpawns();
        for (Participant participant : participants.values()) {
            TeamSpawn teamSpawn = teamSpawns.get(participant.getTeamId());
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
    
    private void onTeamQuit(ParkourTeam team) {
        if (team.size() > 0) {
            return;
        }
        ParkourTeam removed = teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), removed.getQuitData());
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        ParkourParticipant participant = participants.get(participantUUID);
        if (participant == null) {
            return;
        }
        if (parkourHasStarted) {
            int unusedSkips = calculateUnusedSkips(participant);
            quitDatas.put(participantUUID, participant.getQuitData(unusedSkips));
        }
        resetParticipant(participant);
        participants.remove(participant.getUniqueId());
        onTeamQuit(teams.get(teamId));
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
                .titleAudience(Audience.audience(participants.values()))
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
        for (Participant participant : participants.values()) {
            giveSkipItem(participant, config.getNumOfSkips());
        }
    }
    
    /**
     * Gives the appropriate number of skips to the given participant
     * @param participant the participant to receive skips
     */
    private void giveSkipItem(Participant participant, int numOfSkips) {
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
     * @param teamIds the teamIds to get the spawns for. If there are more teamIds than spawns in the config, some teamIds will be in the same spawn.
     * @return a map of teamIds to their {@link TeamSpawn}s. Null if the config never specified a list of {@link TeamSpawn}s.
     */
    private @Nullable Map<String, @NotNull TeamSpawn> getTeamSpawns(@NotNull Set<String> teamIds) {
        List<TeamSpawn> teamSpawns = config.getTeamSpawns();
        if (teamSpawns == null) {
            return null;
        }
        Map<String, TeamSpawn> result = new HashMap<>(teamIds.size());
        int i = 0;
        for (String teamId : teamIds) {
            int teamSpawnIndex = MathUtils.wrapIndex(i, teamSpawns.size());
            TeamSpawn teamSpawn = teamSpawns.get(teamSpawnIndex);
            teamSpawn.setBarrierMaterial(gameManager.getTeamStainedGlassColor(teamId));
            result.put(teamId, teamSpawn);
            i++;
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
        if (!participants.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "ParkourPathwayGame.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        Participant participant = participants.get(event.getEntity().getUniqueId());
        if (participant == null) {
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
        if (!participants.containsKey(event.getWhoClicked().getUniqueId())) {
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
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!gameActive) {
            return;
        }
        ParkourParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
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
    private void onParticipantMove(@NotNull ParkourParticipant participant) {
        if (participant.isFinished()) {
            return;
        }
        int currentPuzzleIndex = participant.getCurrentPuzzle();
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
            int currentCheckpoint = participant.getCurrentPuzzleCheckpoint();
            if (parallelCheckPointIndex == currentCheckpoint) {
                return;
            }
            participant.setCurrentPuzzleCheckpoint(parallelCheckPointIndex);
        }
    }
    
    /**
     * Prevent spectators from leaving the spectatorArea
     * @param participant the participant (assumed to be a valid participant of this game in the SPECTATOR gamemode
     * @param event the event which may be cancelled in order to keep the given participant in the spectator area
     */
    private void keepSpectatorsInArea(@NotNull Participant participant, PlayerMoveEvent event) {
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
        ParkourParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
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
    
    private void performCheckpointSkip(ParkourParticipant participant) {
        if (participant.isFinished()) {
            return;
        }
        int currentPuzzleIndex = participant.getCurrentPuzzle();
        int nextPuzzleIndex = currentPuzzleIndex + 1;
        if (nextPuzzleIndex >= config.getPuzzlesSize()) {
            // should not occur because of above check
            return;
        }
        participant.getInventory().removeItemAnySlot(config.getSkipItem());
        onParticipantSkippedToCheckpoint(participant, nextPuzzleIndex);
    }
    
    private void onParticipantSkippedToCheckpoint(ParkourParticipant participant, int puzzleIndex) {
        participant.setCurrentPuzzle(puzzleIndex);
        participant.setCurrentPuzzleCheckpoint(0);
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
            for (ParkourParticipant p : participants.values()) {
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
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
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
    
    private void onParticipantOutOfBounds(ParkourParticipant participant, Puzzle currentPuzzle) {
        CheckPoint currentCheckPoint = currentPuzzle.checkPoints().get(participant.getCurrentPuzzleCheckpoint());
        Location respawn = currentCheckPoint.respawn().setDirection(participant.getLocation().getDirection());
        participant.teleport(respawn);
    }
    
    private void onParticipantReachCheckpoint(ParkourParticipant participant, int puzzleIndex, int puzzleCheckPointIndex) {
        participant.setCurrentPuzzle(puzzleIndex);
        participant.setCurrentPuzzleCheckpoint(puzzleCheckPointIndex);
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
            int multiplied = (int) (gameManager.getMultiplier() *
                    calculatePointsForPuzzle(puzzleIndex, config.getCheckpointScore()));
            participant.awardPoints(multiplied);
            ParkourTeam team = teams.get(participant.getTeamId());
            team.addPoints(multiplied);
            displayScore(participant);
            displayScore(team);
            
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
            for (ParkourParticipant p : participants.values()) {
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
    
    private void onParticipantFinish(ParkourParticipant participant, boolean awardPoints) {
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
            int multiplied = (int) (gameManager.getMultiplier() *
                    calculatePointsForWin(config.getWinScore()));
            participant.awardPoints(multiplied);
            ParkourTeam team = teams.get(participant.getTeamId());
            team.addPoints(multiplied);
            displayScore(participant);
            displayScore(team);
        }
        awardPointsForUnusedSkips(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        participant.setFinished(true);
    }
    
    /**
     * Checks how many skips the player has, and awards them points for any remaining
     * skips. Removes them from their inventory as well.
     */
    private void awardPointsForUnusedSkips(ParkourParticipant participant) {
        if (config.getUnusedSkipScore() <= 0.0) {
            return;
        }
        int unusedSkips = calculateUnusedSkips(participant);
        if (unusedSkips > 0) {
            participant.sendMessage(Component.empty()
                    .append(Component.text(unusedSkips))
                    .append(Component.text(" unused skips"))
                    .color(NamedTextColor.GREEN));
            int multiplied = (int) (gameManager.getMultiplier() *
                    unusedSkips * config.getUnusedSkipScore());
            participant.awardPoints(multiplied);
            ParkourTeam team = teams.get(participant.getTeamId());
            team.addPoints(multiplied);
            displayScore(participant);
            displayScore(team);
        }
        ParticipantInitializer.clearInventory(participant);
    }
    
    /**
     * @param participant the participant
     * @return the number of unused skips in the participant's inventory
     */
    private int calculateUnusedSkips(Participant participant) {
        return Arrays.stream(participant.getInventory().getContents()).filter(itemStack -> {
            if (itemStack == null) {
                return false;
            }
            return itemStack.getItemMeta().equals(config.getSkipItem().getItemMeta());
        }).mapToInt(ItemStack::getAmount).sum();
    }
    
    private boolean allParticipantsHaveFinished() {
        for (ParkourParticipant participant : participants.values()) {
            int currentPuzzleIndex = participant.getCurrentPuzzle();
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
        for (ParkourParticipant participant : participants.values()) {
            int puzzleIndex = participant.getCurrentPuzzle();
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
        for (ParkourParticipant participant : participants.values()) {
            int puzzleIndex = participant.getCurrentPuzzle();
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
                    Audience.audience(participants.values()).showTitle(UIUtils.defaultTitle(
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
                    for (ParkourParticipant participant : participants.values()) {
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
                    for (ParkourParticipant participant : participants.values()) {
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
                for (Participant participant : participants.values()) {
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    private void giveBoots(Participant participant) {
        Color teamColor = gameManager.getTeam(participant).getBukkitColor();
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
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
        
        for (ParkourTeam team : teams.values()) {
            displayScore(team);
        }
        for (ParkourParticipant participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    public void displayScore(ParkourTeam team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
    }
    
    public void displayScore(ParkourParticipant participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
    
    private void updateCheckpointSidebar(ParkourParticipant participant) {
        int lastCheckpoint = config.getPuzzlesSize()-1;
        sidebar.updateLine(participant.getUniqueId(), "checkpoint",
                Component.empty()
                        .append(Component.text(participant.getCurrentPuzzle()))
                        .append(Component.text("/"))
                        .append(Component.text(lastCheckpoint)));
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    private void messageAllParticipants(Component message) {
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).sendMessage(message);
    }
}
