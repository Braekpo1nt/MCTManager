package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.SurvivalGamesState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.glow.GlowManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.topbar.ManyBattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The context for the state pattern
 */
@Getter
@Setter
public class SurvivalGamesGame extends GameBase<SurvivalGamesParticipant, SurvivalGamesTeam, SurvivalGamesParticipant.QuitData, SurvivalGamesTeam.QuitData, SurvivalGamesState> {
    
    private final @NotNull ManyBattleTopbar topbar;
    private final GlowManager glowManager;
    private final SurvivalGamesConfig config;
    private final WorldBorder worldBorder;
    
    public SurvivalGamesGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull SurvivalGamesConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(GameType.SURVIVAL_GAMES, plugin, gameManager, title, new InitialState());
        this.topbar = addUIManager(new ManyBattleTopbar());
        this.glowManager = addUIManager(new GlowManager(plugin));
        this.config = config;
        worldBorder = config.getWorld().getWorldBorder();
        glowManager.registerListeners();
        fillAllChests();
        initializeGlowManager();
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        start(newTeams, newParticipants, newAdmins);
        for (SurvivalGamesTeam team : teams.values()) {
            updateAliveCount(team);
        }
        initializeWorldBorder();
        createPlatformsAndTeleportTeams();
        Main.logger().info("Started Survival Games");
    }
    
    @Override
    protected @NotNull SurvivalGamesState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
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
    
    @Override
    public void cleanup() {
        clearFloorItems();
        clearAllChests();
        clearContainers();
        removePlatforms();
        worldBorder.reset();
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        super.onAdminJoin(admin);
        for (Participant participant : participants.values()) {
            glowManager.showGlowing(admin.getUniqueId(), participant.getUniqueId());
        }
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        // TODO: remove these showPlayer() uses in favor of GameBase._initializeAdmin()
        topbar.showPlayer(admin);
        glowManager.showPlayer(admin);
        admin.teleport(config.getAdminSpawn());
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        topbar.hidePlayer(admin.getUniqueId());
        glowManager.hidePlayer(admin);
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
    
    @Override
    protected void initializeSidebar() {
        topbar.setMiddle(Component.empty());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("timer", "")
        );
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull SurvivalGamesTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
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
    
    @Override
    protected @NotNull SurvivalGamesParticipant createParticipant(Participant participant, SurvivalGamesParticipant.QuitData quitData) {
        return new SurvivalGamesParticipant(participant, quitData);
    }
    
    @Override
    protected @NotNull SurvivalGamesParticipant createParticipant(Participant participant) {
        return new SurvivalGamesParticipant(participant, 0);
    }
    
    @Override
    protected @NotNull SurvivalGamesParticipant.QuitData getQuitData(SurvivalGamesParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
        initializeKillCount(participant);
    }
    
    @Override
    protected void initializeTeam(SurvivalGamesTeam team) {
        topbar.addTeam(team.getTeamId(), team.getColor());
    }
    
    @Override
    protected @NotNull SurvivalGamesTeam createTeam(Team team, SurvivalGamesTeam.QuitData quitData) {
        return new SurvivalGamesTeam(team, quitData.getScore());
    }
    
    @Override
    protected @NotNull SurvivalGamesTeam createTeam(Team team) {
        return new SurvivalGamesTeam(team, 0);
    }
    
    @Override
    protected @NotNull SurvivalGamesTeam.QuitData getQuitData(SurvivalGamesTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        // do nothing
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
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
    
    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
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
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
}
