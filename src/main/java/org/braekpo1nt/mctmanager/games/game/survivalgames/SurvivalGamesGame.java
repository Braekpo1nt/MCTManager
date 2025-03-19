package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Data;
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
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfigController;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.SurvivalGamesState;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.glow.GlowManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.ManyBattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The context for the state pattern
 */
@Data
public class SurvivalGamesGame implements MCTGame, Configurable, Listener {
    
    private @Nullable SurvivalGamesState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final @NotNull ManyBattleTopbar topbar;
    private final SurvivalGamesConfigController configController;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Survival Games"))
            .color(NamedTextColor.BLUE);
    private final TimerManager timerManager;
    private final GlowManager glowManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private SurvivalGamesConfig config;
    public Map<UUID, SurvivalGamesParticipant> participants = new HashMap<>();
    public Map<UUID, SurvivalGamesParticipant.QuitData> quitDatas = new HashMap<>();
    public Map<String, SurvivalGamesTeam> teams = new HashMap<>();
    private Map<String, SurvivalGamesTeam.QuitData> teamQuitDatas = new HashMap<>();
    private List<Player> admins = new ArrayList<>();
    private WorldBorder worldBorder;
    private Component title = baseTitle;
    
    public SurvivalGamesGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.configController = new SurvivalGamesConfigController(plugin.getDataFolder());
        this.topbar = new ManyBattleTopbar();
        this.glowManager = new GlowManager(plugin);
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
        return GameType.SURVIVAL_GAMES;
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(Collection<Team> newTeams, Collection<Participant> newParticipants, List<Player> newAdmins) {
        this.teams = new HashMap<>(newTeams.size());
        for (Team team : newTeams) {
            teams.put(team.getTeamId(), new SurvivalGamesTeam(team, 0));
            topbar.addTeam(team.getTeamId(), team.getColor());
        }
        this.participants = new HashMap<>(newParticipants.size());
        this.quitDatas = new HashMap<>();
        this.teamQuitDatas = new HashMap<>();
        worldBorder = config.getWorld().getWorldBorder();
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        glowManager.registerListeners();
        gameManager.getTimerManager().register(timerManager);
        fillAllChests();
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        createPlatformsAndTeleportTeams();
        initializeSidebar();
        setUpTeamOptions();
        startAdmins(newAdmins);
        initializeGlowManager();
        initializeWorldBorder();
        state = new DescriptionState(this);
        Main.logger().info("Started Survival Games");
    }
    
    /**
     * Set up all the appropriate glowing effects for the start of the game
     */
    private void initializeGlowManager() {
        for (Participant participant : participants.values()) {
            for (Participant target : participants.values()) {
                if (!participant.equals(target)) {
                    if (participant.getTeamId().equals(target.getTeamId())) {
                        glowManager.showGlowing(participant, target);
                    }
                }
            }
            for (Player admin : admins) {
                glowManager.showGlowing(admin, participant);
            }
        }
    }
    
    /**
     * Make the participant glow to their teammates, and their teammates glow to them
     * (but don't glow to themselves). Also makes the participant glow to the admins.
     * @param participant the participant to show their teammates to
     */
    public void initializeGlowing(Participant participant) {
        for (SurvivalGamesParticipant other : participants.values()) {
            if (!other.equals(participant)) {
                if (participant.getTeamId().equals(other.getTeamId())) {
                    glowManager.showGlowing(participant, other);
                    glowManager.showGlowing(other, participant);
                }
            }
        }
        for (Player admin : admins) {
            glowManager.showGlowing(admin, participant);
        }
    }
    
    public void initializeParticipant(Participant newParticipant) {
        SurvivalGamesParticipant participant = new SurvivalGamesParticipant(newParticipant, 0);
        participants.put(participant.getUniqueId(), participant);
        SurvivalGamesTeam team = teams.get(participant.getTeamId());
        team.addParticipant(participant);
        String teamId = participant.getTeamId();
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        topbar.linkToTeam(participant.getUniqueId(), teamId);
        glowManager.addPlayer(participant);
        updateAliveCount(team);
        initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    public void resetParticipant(Participant participant) {
        teams.get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.removePlayer(participant.getUniqueId());
        topbar.hidePlayer(participant.getUniqueId());
        glowManager.removePlayer(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        glowManager.unregisterListeners();
        cancelAllTasks();
        clearFloorItems();
        clearAllChests();
        clearContainers();
        removePlatforms();
        worldBorder.reset();
        stopAdmins();
        saveScores();
        for (Participant participant : participants.values()) {
            if (state != null) {
                state.resetParticipant(participant);
            } else {
                resetParticipant(participant);
            }
        }
        clearSidebar();
        glowManager.clear();
        participants.clear();
        teams.clear();
        quitDatas.clear();
        teamQuitDatas.clear();
        gameManager.gameIsOver();
        state = null;
        Main.logger().info("Stopped Survival Games");
    }
    
    private void saveScores() {
        Map<String, Integer> teamScores = new HashMap<>();
        Map<UUID, Integer> participantScores = new HashMap<>();
        for (SurvivalGamesTeam team : teams.values()) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        for (SurvivalGamesParticipant participant : participants.values()) {
            participantScores.put(participant.getUniqueId(), participant.getScore());
        }
        for (Map.Entry<String, SurvivalGamesTeam.QuitData> entry : teamQuitDatas.entrySet()) {
            teamScores.put(entry.getKey(), entry.getValue().getScore());
        }
        for (Map.Entry<UUID, SurvivalGamesParticipant.QuitData> entry : quitDatas.entrySet()) {
            participantScores.put(entry.getKey(), entry.getValue().getScore());
        }
        gameManager.addScores(teamScores, participantScores);
    }
    
    public void onTeamJoin(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            return;
        }
        SurvivalGamesTeam.QuitData quitData = teamQuitDatas.get(team.getTeamId());
        if (quitData != null) {
            teams.put(team.getTeamId(), new SurvivalGamesTeam(team, quitData.getScore()));
        } else {
            teams.put(team.getTeamId(), new SurvivalGamesTeam(team, 0));
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        if (state != null) {
            state.onParticipantJoin(participant, team);
        }
    }
    
    @Override
    public void onParticipantQuit(UUID participantUUID, String teamId) {
        SurvivalGamesParticipant sgParticipant = participants.get(participantUUID);
        if (sgParticipant == null) {
            return;
        }
        if (state != null) {
            state.onParticipantQuit(sgParticipant);
        }
    }
    
    public void onTeamQuit(SurvivalGamesTeam team) {
        if (team.size() > 0) {
            return;
        }
        SurvivalGamesTeam removed = teams.remove(team.getTeamId());
        teamQuitDatas.put(team.getTeamId(), removed.getQuitData());
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
        for (Participant participant : participants.values()) {
            glowManager.showGlowing(admin.getUniqueId(), participant.getUniqueId());
        }
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        topbar.showPlayer(admin);
        glowManager.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getAdminSpawn());
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
        topbar.hidePlayer(admin.getUniqueId());
        glowManager.removePlayer(admin);
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    /**
     * Fill all chests in the survivalgames world, map chests and spawn chests
     */
    private void fillAllChests() {
        fillSpawnChests();
        fillMapChests();
    }
    
    private void fillSpawnChests() {
        for (org.bukkit.util.Vector coords : config.getSpawnChestCoords()) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.setLootTable(config.getSpawnLootTable());
            chest.update();
        }
    }
    
    private void fillMapChests() {
        for (Vector coords : config.getMapChestCoords()) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            fillMapChest(((Chest) block.getState()));
        }
    }
    
    /**
     * Fills the given chest with a random loot table
     * @param chest The chest to fill
     */
    private void fillMapChest(Chest chest) {
        LootTable lootTable = MathUtils.getWeightedRandomValue(config.getWeightedLootTables());
        chest.setLootTable(lootTable);
        chest.update();
    }
    
    public void createPlatformsAndTeleportTeams() {
        Set<String> teams = Participant.getTeamIds(participants);
        createPlatforms(teams);
        teleportTeams(teams);
    }
    
    /**
     * Creates platforms for teamIds to spawn on made of a hollow rectangle of Barrier blocks where the bottom layer is Concrete that matches the color of the team
     * <br>
     * For n teamIds and m platforms in storageUtil.getPlatformBarriers():<br>
     * - place n platforms, but no more than m platforms
     * @param teamIds the teamIds that will be teleported
     */
    private void createPlatforms(Set<String> teamIds) {
        List<BoundingBox> platformBarriers = config.getPlatformBarriers();
        World world = config.getWorld();
        int i = 0;
        for (String teamId : teamIds) {
            int platformIndex = MathUtils.wrapIndex(i, platformBarriers.size());
            BoundingBox barrierArea = platformBarriers.get(platformIndex);
            BoundingBox concreteArea = new BoundingBox(
                    barrierArea.getMinX()+1,
                    barrierArea.getMinY(),
                    barrierArea.getMinZ()+1,
                    barrierArea.getMaxX()-1,
                    barrierArea.getMinY(),
                    barrierArea.getMaxZ()-1);
            Material concreteColor = gameManager.getTeamConcreteColor(teamId);
            BlockPlacementUtils.createHollowCube(world, barrierArea, Material.BARRIER);
            BlockPlacementUtils.createCube(world, concreteArea, concreteColor);
            i++;
        }
    }
    
    /**
     * For n teams and m platforms in storageUtil.getPlatformBarriers():<br>
     * - teleport teams to their designated platforms. If n is greater than m, then it will start wrapping around and teleporting different teams to the same platforms, until all teams have a platform. 
     * @param teamIds the teams to teleport (players will be selected from the participants list)
     */
    private void teleportTeams(Set<String> teamIds) {
        List<Location> platformSpawns = config.getPlatformSpawns();
        Map<String, Location> teamSpawnLocations = new HashMap<>(teamIds.size());
        int i = 0;
        for (String teamId : teamIds) {
            int platformIndex = MathUtils.wrapIndex(i, platformSpawns.size());
            Location platformSpawn = platformSpawns.get(platformIndex);
            teamSpawnLocations.put(teamId, platformSpawn);
            i++;
        }
        for (Participant participant : participants.values()) {
            Location spawn = teamSpawnLocations.get(participant.getTeamId());
            participant.teleport(spawn);
            participant.setRespawnLocation(spawn, true);
        }
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title)
        );
        topbar.setMiddle(Component.empty());
        for (SurvivalGamesTeam team : teams.values()) {
            displayScore(team);
        }
        for (SurvivalGamesParticipant participant : participants.values()) {
            displayScore(participant);
        }
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("timer", "")
        );
    }
    
    private void setUpTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }
    }
    
    private void initializeWorldBorder() {
        worldBorder.setCenter(config.getWorldBorderCenterX(), config.getWorldBorderCenterZ());
        worldBorder.setSize(config.getInitialBorderSize());
        worldBorder.setDamageAmount(config.getWorldBorderDamageAmount());
        worldBorder.setDamageBuffer(config.getWorldBorderDamageBuffer());
        worldBorder.setWarningDistance(config.getWorldBorderWarningDistance());
        worldBorder.setWarningTime(config.getWorldBorderWarningTime());
    }
    
    /**
     * updates the number of living and dead players in the topbar
     * @param team the team to update
     */
    public void updateAliveCount(@NotNull SurvivalGamesTeam team) {
        topbar.setMembers(team.getTeamId(), team.getAlive(), team.getDead());
    }
    
    public void initializeKillCount(SurvivalGamesParticipant participant) {
        int deaths;
        if (!config.showDeathCount()) {
            deaths = -1;
        } else {
            deaths = participant.getDeaths();
        }
        topbar.setKillsAndDeaths(participant.getUniqueId(), participant.getKills(), deaths);
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    private void clearFloorItems() {
        for (Item item : config.getWorld().getEntitiesByClass(Item.class)) {
            if (config.getRemoveArea().contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }
    
    private void clearAllChests() {
        List<Vector> allChestCoords = new ArrayList<>(config.getSpawnChestCoords());
        allChestCoords.addAll(config.getMapChestCoords());
        for (Vector coords : allChestCoords) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.getBlockInventory().clear();
        }
    }
    
    private void clearContainers() {
        if (!config.shouldClearContainers()) {
            return;
        }
        Main.logger().info("Clearing containers");
        List<Chunk> chunks = getChunksInBoundingBox(config.getWorld(), config.getRemoveArea());
        int count = 0;
        for (Chunk chunk : chunks) {
            Collection<BlockState> blockStates = chunk.getTileEntities(block -> block.getState() instanceof InventoryHolder, false);
            count += blockStates.size();
            for (BlockState blockState : blockStates) {
                ((InventoryHolder) blockState).getInventory().clear();
            }
        }
        Main.logger().info(String.format("%s chunks found, %s InventoryHolders", chunks.size(), count));
    }
    
    public static List<Chunk> getChunksInBoundingBox(World world, BoundingBox boundingBox) {
        List<Chunk> chunksInBoundingBox = new ArrayList<>();
        
        int minX = (int) boundingBox.getMinX();
        int maxX = (int) boundingBox.getMaxX();
        int minZ = (int) boundingBox.getMinZ();
        int maxZ = (int) boundingBox.getMaxZ();
        
        for (int x = minX; x <= maxX; x += 16) {
            for (int z = minZ; z <= maxZ; z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunksInBoundingBox.contains(chunk)) {
                    chunksInBoundingBox.add(chunk);
                }
            }
        }
        
        return chunksInBoundingBox;
    }
    
    public void removePlatforms() {
        List<BoundingBox> platformBarriers = config.getPlatformBarriers();
        for (BoundingBox barrier : platformBarriers) {
            BlockPlacementUtils.createCube(config.getWorld(), barrier, Material.AIR);
        }
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
        topbar.removeAllTeams();
        topbar.hideAllPlayers();
    }
    
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        Audience.audience(participants.values()).sendMessage(message);
    }
    
    // EventHandlers
    /**
     * Called when:
     * Right-clicking an armor stand
     * Right-clicking an item frame (also; onPlayerInteractEntity() )
     * <p>
     * Not called when:
     * Left-clicking an armor stand
     * Left-clicking an item frame with an item in it
     * Left-clicking an item frame without an item in it
     */
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (event.getRightClicked() instanceof ArmorStand || event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Called when:
     * Right-clicking an item frame (also; onPlayerInteractAtEntity() )
     * <p>
     * Not called when:
     * Right-clicking an armor stand
     * Left-clicking an armor stand
     * Left-clicking an item frame with an item in it
     * Left-clicking an item frame without an item in it
     */
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Called when:
     * Left-clicking an armor stand
     * Left-clicking an item frame with an item in it
     * <p>
     * Not called when:
     * Right-clicking an armor stand
     * Right-clicking an item frame
     * Left-clicking an item frame without an item in it
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player clicker) {
            if (!participants.containsKey(clicker.getUniqueId())) {
                return;
            }
        }
        if (event.getEntity() instanceof ArmorStand || event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Called when:
     * Left-clicking an item frame without an item in it
     * <p>
     * Not called when:
     * Right-clicking an armor stand
     * Left-clicking an armor stand
     * Right-clicking an item frame
     * Left-clicking an item frame with an item in it
     * Left-clicking an item frame without an item in it
     */
    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!participants.containsKey(event.getRemover().getUniqueId())) {
            return;
        }
        if (event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
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
        if (config.getSpectatorArea() == null){
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getPlatformSpawns().getFirst());
            return;
        }
        if (!config.getSpectatorArea().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
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
    
    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (state == null) {
            return;
        }
        if (!config.lockOtherInventories()) {
            return;
        }
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof BlockInventoryHolder blockInventoryHolder)) {
            return;
        }
        Location location = blockInventoryHolder.getBlock().getLocation();
        Vector pos = location.toVector();
        if (config.getSpawnChestCoords().contains(pos)
                || config.getMapChestCoords().contains(pos)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        if (state == null) {
            return;
        }
        if (!event.getInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof Chest chest)) {
            return;
        }
        if (!inventory.isEmpty()) {
            return;
        }
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        List<HumanEntity> viewers = inventory.getViewers();
        if (countLivingParticipantViewers(viewers) > 1) {
            return;
        }
        Block block = chest.getBlock();
        Vector chestPos = block.getLocation().toVector();
        if (!config.getSpawnChestCoords().contains(chestPos)
                && !config.getMapChestCoords().contains(chestPos)) {
            return;
        }
        block.setType(Material.AIR);
    }
    
    /**
     * @param viewers the viewers list containing entities who are viewing one inventory
     * @return the number of {@link HumanEntity}s in the viewers list who are living participants
     */
    private long countLivingParticipantViewers(List<HumanEntity> viewers) {
        return viewers.stream()
                .map(viewer -> participants.get(viewer.getUniqueId()))
                .filter(Objects::nonNull)
                .filter(SurvivalGamesParticipant::isAlive)
                .count();
    }
    
    // State-specific callers
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state == null) {
            return;
        }
        if (!participants.containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == null) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        state.onParticipantDeath(event);
    }
    
    // make this private if above used
    public void displayScore(SurvivalGamesTeam team) {
        Component contents = Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD));
        for (UUID memberUUID : team.getMemberUUIDs()) {
            sidebar.updateLine(memberUUID, "personalTeam", contents);
        }
    }
    
    // make this private if above used
    public void displayScore(SurvivalGamesParticipant participant) {
        sidebar.updateLine(participant.getUniqueId(), "personalScore", Component.empty()
                .append(Component.text("Personal: "))
                .append(Component.text(participant.getScore()))
                .color(NamedTextColor.GOLD));
    }
}
