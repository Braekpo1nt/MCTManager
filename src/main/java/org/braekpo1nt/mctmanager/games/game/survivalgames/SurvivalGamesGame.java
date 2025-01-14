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
import org.braekpo1nt.mctmanager.ui.glow.GlowManager;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
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
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The context for the state pattern
 */
@Data
public class SurvivalGamesGame implements MCTGame, Configurable, Listener, Headerable {
    
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
    public List<Player> participants;
    private List<Player> admins = new ArrayList<>();
    private WorldBorder worldBorder;
    private List<UUID> livingPlayers = new ArrayList<>();
    /**
     * a map of all teamIds to how many members are alive on that team
     */
    private Map<String, Integer> livingMembers = new HashMap<>();
    private List<UUID> deadPlayers = new ArrayList<>();
    private Map<UUID, Integer> killCounts = new HashMap<>();
    private Map<UUID, Integer> deathCounts = new HashMap<>();
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
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(Collection<Participant> newParticipants, List<Player> newAdmins) {
        this.participants = new ArrayList<>(newParticipants.size());
        livingPlayers = new ArrayList<>(newParticipants.size());
        deadPlayers = new ArrayList<>();
        livingMembers = new HashMap<>(gameManager.getTeamIds(newParticipants).size());
        killCounts = new HashMap<>(newParticipants.size());
        deathCounts = new HashMap<>(newParticipants.size());
        worldBorder = config.getWorld().getWorldBorder();
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        List<String> teams = gameManager.getTeamIds(newParticipants);
        setUpTopbarTeams(teams);
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
        for (Player participant : participants) {
            for (Player target : participants) {
                if (!participant.equals(target)) {
                    String teamId = gameManager.getTeamId(participant.getUniqueId());
                    String targetTeamId = gameManager.getTeamId(target.getUniqueId());
                    if (teamId.equals(targetTeamId)) {
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
    public void initializeGlowing(Player participant) {
        String teamId = gameManager.getTeamId(participant.getUniqueId());
        for (Player other : participants) {
            if (!other.equals(participant)) {
                String otherTeamId = gameManager.getTeamId(other.getUniqueId());
                if (teamId.equals(otherTeamId)) {
                    glowManager.showGlowing(participant, other);
                    glowManager.showGlowing(other, participant);
                }
            }
        }
        for (Player admin : admins) {
            glowManager.showGlowing(admin, participant);
        }
    }
    
    public void initializeParticipant(Participant participant) {
        participants.add(participant);
        livingPlayers.add(participant.getUniqueId());
        String teamId = gameManager.getTeamId(participant.getUniqueId());
        livingMembers.putIfAbsent(teamId, 0);
        int oldAliveCount = livingMembers.get(teamId);
        livingMembers.put(teamId, oldAliveCount + 1);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        topbar.linkToTeam(participant.getUniqueId(), teamId);
        glowManager.addPlayer(participant);
        updateAliveCount(teamId);
        initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    public void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.removePlayer(participant.getUniqueId());
        topbar.hidePlayer(participant.getUniqueId());
        glowManager.removePlayer(participant);
    }
    
    private void setUpTopbarTeams(List<String> newTeamIds) {
        for (String teamId : newTeamIds) {
            NamedTextColor color = gameManager.getTeamColor(teamId);
            topbar.addTeam(teamId, color);
        }
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
        for (Player participant : participants) {
            if (state != null) {
                state.resetParticipant(participant);
            } else {
                resetParticipant(participant);
            }
        }
        clearSidebar();
        glowManager.clear();
        participants.clear();
        livingPlayers.clear();
        deadPlayers.clear();
        livingMembers.clear();
        killCounts.clear();
        deathCounts.clear();
        gameManager.gameIsOver();
        state = null;
        Main.logger().info("Stopped Survival Games");
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        if (state != null) {
            state.onParticipantJoin(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        if (state != null) {
            state.onParticipantQuit(participant);
        }
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
        for (Player participant : participants) {
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
        List<String> teams = gameManager.getTeamIds(participants);
        createPlatforms(teams);
        teleportTeams(teams);
    }
    
    /**
     * Creates platforms for teams to spawn on made of a hollow rectangle of Barrier blocks where the bottom layer is Concrete that matches the color of the team
     * <br>
     * For n teams and m platforms in storageUtil.getPlatformBarriers():<br>
     * - place n platforms, but no more than m platforms
     * @param teams the teams that will be teleported
     */
    private void createPlatforms(List<String> teams) {
        List<BoundingBox> platformBarriers = config.getPlatformBarriers();
        World world = config.getWorld();
        for (int i = 0; i < teams.size(); i++) {
            String team = teams.get(i);
            int platformIndex = MathUtils.wrapIndex(i, platformBarriers.size());
            BoundingBox barrierArea = platformBarriers.get(platformIndex);
            BoundingBox concreteArea = new BoundingBox(
                    barrierArea.getMinX()+1,
                    barrierArea.getMinY(),
                    barrierArea.getMinZ()+1,
                    barrierArea.getMaxX()-1,
                    barrierArea.getMinY(),
                    barrierArea.getMaxZ()-1);
            Material concreteColor = gameManager.getTeamConcreteColor(team);
            BlockPlacementUtils.createHollowCube(world, barrierArea, Material.BARRIER);
            BlockPlacementUtils.createCube(world, concreteArea, concreteColor);
        }
    }
    
    /**
     * For n teams and m platforms in storageUtil.getPlatformBarriers():<br>
     * - teleport teams to their designated platforms. If n is greater than m, then it will start wrapping around and teleporting different teams to the same platforms, until all teams have a platform. 
     * @param teams the teams to teleport (players will be selected from the participants list)
     */
    private void teleportTeams(List<String> teams) {
        List<Location> platformSpawns = config.getPlatformSpawns();
        Map<String, Location> teamSpawnLocations = new HashMap<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            String team = teams.get(i);
            int platformIndex = MathUtils.wrapIndex(i, platformSpawns.size());
            Location platformSpawn = platformSpawns.get(platformIndex);
            teamSpawnLocations.put(team, platformSpawn);
        }
        for (Player participant : participants) {
            String team = gameManager.getTeamId(participant.getUniqueId());
            Location spawn = teamSpawnLocations.get(team);
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
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("timer", "")
        );
    }
    
    private void setUpTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
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
     * @param teamId the team to update
     */
    public void updateAliveCount(@NotNull String teamId) {
        int alive = livingMembers.get(teamId);
        int dead = getDeadMembers(teamId);
        topbar.setMembers(teamId, alive, dead);
    }
    
    /**
     * @param teamId the teamId of the team to count the dead members of
     * @return the number of dead players on the given team in this game
     */
    private int getDeadMembers(String teamId) {
        int count = 0;
        for (Player participant : participants) {
            if (deadPlayers.contains(participant.getUniqueId())) {
                String participantTeamId = gameManager.getTeamId(participant.getUniqueId());
                if (teamId.equals(participantTeamId)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public void initializeKillCount(Player participant) {
        killCounts.putIfAbsent(participant.getUniqueId(), 0);
        deathCounts.putIfAbsent(participant.getUniqueId(), 0);
        int kills = killCounts.get(participant.getUniqueId());
        int deaths;
        if (!config.showDeathCount()) {
            deaths = -1;
        } else {
            deaths = deathCounts.get(participant.getUniqueId());
        }
        topbar.setKillsAndDeaths(participant.getUniqueId(), kills, deaths);
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
        Audience.audience(participants).sendMessage(message);
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
        Player clicker = event.getPlayer();
        if (!participants.contains(clicker)) {
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
        Player clicker = event.getPlayer();
        if (!participants.contains(clicker)) {
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
            if (!participants.contains(clicker)) {
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
        Player clicker = (Player) event.getRemover();
        if (!participants.contains(clicker)) {
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
        if (!participants.contains(event.getPlayer())) {
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
        if (!participants.contains(event.getPlayer())) {
            return;
        }
        if (!event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!config.getSpectatorArea().contains(event.getFrom().toVector())) {
            event.getPlayer().teleport(config.getPlatformSpawns().get(0));
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
    
    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (state == null) {
            return;
        }
        if (!config.lockOtherInventories()) {
            return;
        }
        if (!(event.getPlayer() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
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
        if (!(event.getPlayer() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        List<HumanEntity> viewers = inventory.getViewers();
        if (countParticipantViewers(viewers) > 1) {
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
    
    private int countParticipantViewers(List<HumanEntity> viewers) {
        int count = 0;
        for (HumanEntity viewer : viewers) {
            if (viewer instanceof Player participant) {
                if (participants.contains(participant)) {
                    if (livingPlayers.contains(participant.getUniqueId())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    // State-specific callers
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == null) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        state.onPlayerDeath(event);
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
}
