package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.survivalgames.states.SurvivalGamesState;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
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
    private final Random random;
    
    /**
     * the index of the border stage
     */
    private int borderStageIndex;
    /**
     * A list of task ids representing when participants should be set to gliding mode,
     * but need to be cancelled if anything stops the game early
     */
    private final List<Integer> glideTaskIds;
    private int currentRound;
    
    public SurvivalGamesGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull SurvivalGamesConfig config,
            @NotNull String configFile,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(new GameInstanceId(GameType.SURVIVAL_GAMES, configFile), plugin, gameManager, title, new InitialState());
        this.topbar = addUIManager(new ManyBattleTopbar());
        this.glowManager = addUIManager(new GlowManager(plugin));
        this.config = config;
        this.glideTaskIds = new ArrayList<>();
        this.random = new Random();
        this.currentRound = 1;
        this.borderStageIndex = 0;
        worldBorder = config.getWorld().getWorldBorder();
        glowManager.registerListeners();
        fillAllChests();
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        if (newTeams.size() < 2) {
            messageAllParticipants(Component.empty()
                    .append(Component.text(GameType.SURVIVAL_GAMES.getTitle()))
                    .append(Component.text(" doesn't end correctly unless there are 2 or more teams online. use ")
                            .append(Component.text("/mct game stop")
                                    .clickEvent(ClickEvent.suggestCommand("/mct game stop"))
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" to stop the game."))
                            .color(NamedTextColor.RED)));
        }
        start(newTeams, newParticipants, newAdmins);
        Main.logger().info("Started Survival Games");
    }
    
    /**
     * Convenience method for getting the current border stage 
     * @return teh current border stage
     */
    public BorderStage getCurrentBorderStage() {
        return config.getBorderStages().get(borderStageIndex);
    }
    
    @Override
    protected @NotNull SurvivalGamesState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
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
        admin.teleport(config.getAdminSpawn());
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    /**
     * Fill all chests in the survivalgames world, map chests and spawn chests
     */
    public void fillAllChests() {
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
        createPlatforms();
        teleportTeams();
    }
    
    /**
     * Creates platforms for teams to spawn on made of a hollow rectangle of Barrier blocks where the bottom layer is Concrete that matches the color of the team
     * <br>
     * For n teamIds and m platforms in storageUtil.getPlatformBarriers():<br>
     * - place n platforms, but no more than m platforms
     */
    private void createPlatforms() {
        List<BoundingBox> platformBarriers = config.getPlatformBarriers();
        World world = config.getWorld();
        int i = 0;
        for (SurvivalGamesTeam team : teams.values()) {
            int platformIndex = MathUtils.wrapIndex(i, platformBarriers.size());
            BoundingBox barrierArea = platformBarriers.get(platformIndex);
            BoundingBox concreteArea = new BoundingBox(
                    barrierArea.getMinX()+1,
                    barrierArea.getMinY(),
                    barrierArea.getMinZ()+1,
                    barrierArea.getMaxX()-1,
                    barrierArea.getMinY(),
                    barrierArea.getMaxZ()-1);
            BlockPlacementUtils.createHollowCube(world, barrierArea, Material.BARRIER);
            BlockPlacementUtils.createCube(world, concreteArea, team.getColorAttributes().getConcrete());
            i++;
        }
    }
    
    /**
     * For n teams and m platforms in storageUtil.getPlatformBarriers():<br>
     * - teleport teams to their designated platforms. If n is greater than m, then it will start wrapping around and teleporting different teams to the same platforms, until all teams have a platform. 
     */
    private void teleportTeams() {
        List<Location> platformSpawns = config.getPlatformSpawns();
        Map<String, Location> teamSpawnLocations = new HashMap<>(teams.size());
        int i = 0;
        for (SurvivalGamesTeam team : teams.values()) {
            int platformIndex = MathUtils.wrapIndex(i, platformSpawns.size());
            Location platformSpawn = platformSpawns.get(platformIndex);
            teamSpawnLocations.put(team.getTeamId(), platformSpawn);
            i++;
        }
        for (Participant participant : participants.values()) {
            Location spawn = teamSpawnLocations.get(participant.getTeamId());
            participant.teleport(spawn);
            participant.setRespawnLocation(spawn, true);
        }
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("round", Component.empty()),
                new KeyLine("timer", Component.empty()),
                new KeyLine("respawn", Component.empty())
        );
    }
    
    @Override
    protected void initializeSidebar() {
        topbar.setMiddle(Component.empty());
        sidebar.addLines(
                new KeyLine("round", Component.empty()),
                new KeyLine("respawn", Component.empty())
        );
    }
    
    /**
     * Update the sidebars to reflect the current round
     */
    public void updateRoundLine() {
        if (config.getRounds() <= 1) {
            return;
        }
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(currentRound))
                .append(Component.text("/"))
                .append(Component.text(config.getRounds()));
        sidebar.updateLine("round", roundLine);
        adminSidebar.updateLine("round", roundLine);
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull SurvivalGamesTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
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
    
    public void clearFloorItems() {
        for (Item item : config.getWorld().getEntitiesByClass(Item.class)) {
            if (config.getRemoveArea().contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }
    
    public void clearAllChests() {
        List<Vector> allChestCoords = new ArrayList<>(config.getSpawnChestCoords());
        allChestCoords.addAll(config.getMapChestCoords());
        for (Vector coords : allChestCoords) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.getBlockInventory().clear();
        }
    }
    
    public void clearContainers() {
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
