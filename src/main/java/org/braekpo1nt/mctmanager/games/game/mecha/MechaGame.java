package org.braekpo1nt.mctmanager.games.game.mecha;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfig;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfigController;
import org.braekpo1nt.mctmanager.games.game.mecha.states.MechaState;
import org.braekpo1nt.mctmanager.games.game.mecha.states.StartingState;
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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The context for the state pattern
 */
@Data
public class MechaGame implements MCTGame, Configurable, Listener {
    
    private MechaState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final @NotNull ManyBattleTopbar topbar;
    private final MechaConfigController configController;
    private final String baseTitle = ChatColor.BLUE+"MECHA";
    private final TimerManager timerManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private MechaConfig config;
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
    private String title = baseTitle;
    /**
     * the index of the border stage
     */
    private int borderStageIndex = 0;
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.gameManager = gameManager;
        this.configController = new MechaConfigController(plugin.getDataFolder());
        this.topbar = new ManyBattleTopbar();
    }
    
    @Override
    public void setTitle(@NotNull String title) {
        this.title = title;
        if (sidebar != null) {
            sidebar.updateLine("title", title);
        }
        if (adminSidebar != null) {
            adminSidebar.updateLine("title", title);
        }
    }
    
    @Override
    public @NotNull String getBaseTitle() {
        return baseTitle;
    }
    
    @Override
    public GameType getType() {
        return GameType.MECHA;
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        state = new StartingState(this);
        this.participants = new ArrayList<>(newParticipants.size());
        livingPlayers = new ArrayList<>(newParticipants.size());
        deadPlayers = new ArrayList<>();
        livingMembers = new HashMap<>(gameManager.getTeamNames(newParticipants).size());
        killCounts = new HashMap<>(newParticipants.size());
        deathCounts = new HashMap<>(newParticipants.size());
        worldBorder = config.getWorld().getWorldBorder();
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        List<String> teams = gameManager.getTeamNames(newParticipants);
        setUpTopbarTeams(teams);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        borderStageIndex = 0;
        fillAllChests();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        createPlatforms(teams);
        teleportTeams(teams);
        initializeSidebar();
        setUpTeamOptions();
        initializeWorldBorder();
        startAdmins(newAdmins);
        Bukkit.getLogger().info("Started mecha");
    }
    
    private void setUpTopbarTeams(List<String> newTeamIds) {
        for (String teamId : newTeamIds) {
            NamedTextColor color = gameManager.getTeamNamedTextColor(teamId);
            topbar.addTeam(teamId, color);
        }
    }
    
    private void initializeParticipant(Player participant) {
        state.initializeParticipant(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        borderStageIndex = 0;
        cancelAllTasks();
        clearFloorItems();
        clearAllChests();
        clearContainers();
        removePlatforms();
        worldBorder.reset();
        stopAdmins();
        for (Player participant : participants) {
            state.resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        livingPlayers.clear();
        deadPlayers.clear();
        livingMembers.clear();
        killCounts.clear();
        deathCounts.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopped mecha");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        state.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        state.onParticipantQuit(participant);
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
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
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
     * Fill all chests in the mecha world, map chests and spawn chests
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
        LootTable lootTable = MathUtils.getWeightedRandomValue(config.getWeightedMechaLootTables());
        chest.setLootTable(lootTable);
        chest.update();
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
            String team = gameManager.getTeamName(participant.getUniqueId());
            Location spawn = teamSpawnLocations.get(team);
            participant.teleport(spawn);
            participant.setBedSpawnLocation(spawn, true);
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
                String participantTeamId = gameManager.getTeamName(participant.getUniqueId());
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
        int deaths = deathCounts.get(participant.getUniqueId());
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
        Bukkit.getLogger().info("Clearing containers");
        List<Chunk> chunks = getChunksInBoundingBox(config.getWorld(), config.getRemoveArea());
        int count = 0;
        for (Chunk chunk : chunks) {
            Collection<BlockState> blockStates = chunk.getTileEntities(block -> block.getState() instanceof InventoryHolder, false);
            count += blockStates.size();
            for (BlockState blockState : blockStates) {
                ((InventoryHolder) blockState).getInventory().clear();
            }
        }
        Bukkit.getLogger().info(String.format("%s chunks found, %s InventoryHolders", chunks.size(), count));
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
    
    private void removePlatforms() {
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
}
