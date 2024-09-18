package org.braekpo1nt.mctmanager.games.game.farmrush;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfigController;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.FarmRushState;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.List;

@Data
public class FarmRushGame implements MCTGame, Configurable, Headerable, Listener {
    
    public static final NamespacedKey HAS_SCORE_LORE = NamespacedKey.minecraft("hasscorelore");
    
    private @Nullable FarmRushState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Farm Rush"))
            .color(NamedTextColor.BLUE);
    private Component title = baseTitle;
    private FarmRushConfig config;
    private final FarmRushConfigController configController;
    private final List<Player> admins = new ArrayList<>();
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<String, Team> teams = new HashMap<>();
    
    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Participant {
        /**
         * The player object that this participant represents
         */
        @EqualsAndHashCode.Include
        private final @NotNull Player player;
        private final @NotNull String teamId;
        private boolean alive = true;
        
        public boolean isAlive() {
            return alive;
        }
        
        /**
         * @return the UUID of the player this Participant represents
         */
        public UUID getUniqueId() {
            return player.getUniqueId();
        }
    }
    
    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Team {
        @EqualsAndHashCode.Include
        private final @NotNull String teamId;
        private final @NotNull List<UUID> members = new ArrayList<>();
        private final Arena arena;
    }
    
    public FarmRushGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.timerManager = new TimerManager(plugin);
        this.configController = new FarmRushConfigController(plugin.getDataFolder());
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public GameType getType() {
        return GameType.FARM_RUSH;
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        List<String> teamIds = gameManager.getTeamIds(newParticipants);
        List<Arena> arenas = createArenas(teamIds);
        for (int i = 0; i < teamIds.size(); i++) {
            String teamId = teamIds.get(i);
            Arena arena = arenas.get(i);
            teams.put(teamId, new Team(teamId, arena));
        }
        placeArenas(arenas);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startAdmins(newAdmins);
        initializeSidebar();
        setupTeamOptions();
        state = new DescriptionState(this);
        Main.logger().info("Starting Farm Rush game");
    }
    
    /**
     * Actually place the schematic file of the arenas and add any necessary additions,
     * such as the barrel for delivery
     * @param arenas the arenas to place copies of the schematic file on
     */
    private void placeArenas(@NotNull List<Arena> arenas) {
        File schematicFile = new File(plugin.getDataFolder(), config.getArenaFile());
        List<Vector> origins = arenas.stream().map(arena -> arena.getBounds().getMin()).toList();
        BlockPlacementUtils.placeSchematic(config.getWorld(), origins, schematicFile);
        for (Arena arena : arenas) {
            Block delivery = arena.getDelivery().getBlock();
            delivery.setType(Material.BARREL);
            BlockData deliveryBlockData = delivery.getBlockData();
            ((Directional) deliveryBlockData).setFacing(arena.getDeliveryBlockFace());
            delivery.setBlockData(deliveryBlockData);   
            
            Block starterChest = arena.getStarterChest().getBlock();
            starterChest.setType(Material.CHEST);
            BlockData starterChestBlockData = starterChest.getBlockData();
            ((Directional) starterChestBlockData).setFacing(arena.getStarterChestBlockFace());
            starterChest.setBlockData(starterChestBlockData);
            ((Chest) starterChest.getState()).getBlockInventory().setContents(config.getStarterChestContents());
        }
    }
    
    /**
     * Fill the space that the arenas were placed with air
     * @param arenas the arenas to remove
     */
    private void removeArenas(@NotNull List<Arena> arenas) {
        List<BoundingBox> boxes = arenas.stream().map(Arena::getBounds).toList();
        BlockPlacementUtils.fillWithAir(config.getWorld(), boxes);
    }
    
    private @NotNull List<Arena> createArenas(List<String> teamIds) {
        List<Arena> arenas = new ArrayList<>(teamIds.size());
        Arena arena = config.getFirstArena();
        Vector offset = new Vector(arena.getBounds().getWidthX() + 1, 0, 0);
        for (int i = 0; i < teamIds.size(); i++) {
            arenas.add(arena);
            if (i < teamIds.size() - 1) {
                arena = arena.offset(offset);
            }
        }
        return arenas;
    }
    
    private void initializeParticipant(Player player) {
        String teamId = gameManager.getTeamId(player.getUniqueId());
        Participant participant = new Participant(player, teamId);
        participants.put(player.getUniqueId(), participant);
        Team team = teams.get(teamId);
        team.getMembers().add(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE);
        sidebar.addPlayer(player);
        ParticipantInitializer.clearInventory(player);
        ParticipantInitializer.clearStatusEffects(player);
        ParticipantInitializer.resetHealthAndHunger(player);
        player.getInventory().setContents(config.getLoadout());
        player.teleport(team.getArena().getSpawn());
        player.setRespawnLocation(team.getArena().getSpawn(), true);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(config.getAdminLocation());
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("timer", Component.empty())
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        stopAdmins();
        cancelAllTasks();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        removeArenas(teams.values().stream().map(Team::getArena).toList());
        teams.clear();
        participants.clear();
        gameManager.gameIsOver();
        Main.logger().info("Stopping Farm Rush game");
    }
    
    public void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant.getPlayer());
        ParticipantInitializer.clearStatusEffects(participant.getPlayer());
        ParticipantInitializer.resetHealthAndHunger(participant.getPlayer());
        sidebar.removePlayer(participant.getPlayer());
        participant.getPlayer().setGameMode(GameMode.SPECTATOR);
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (state == null) {
            return;
        }
        state.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (state == null) {
            return;
        }
        state.onParticipantQuit(participant);
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
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    @Override
    public @NotNull Component getBaseTitle() {
        return baseTitle;
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
    }
    
    @EventHandler
    public void postRespawn(PlayerPostRespawnEvent event) {
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        Location spawn = teams.get(participant.getTeamId()).getArena().getSpawn();
        event.getPlayer().teleport(spawn);
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        BoundingBox bounds = teams.get(participant.getTeamId()).getArena().getBounds();
        if (!bounds.contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        Arena arena = teams.get(participant.getTeamId()).getArena();
        if (!arena.getBounds().contains(event.getFrom().toVector())) {
            participant.getPlayer().teleport(arena.getSpawn());
            return;
        }
        if (!arena.getBounds().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        Location delivery = teams.get(participant.getTeamId()).getArena().getDelivery();
        if (!event.getBlock().getLocation().equals(delivery)) {
            return;
        }
        event.setCancelled(true);
    }
    @EventHandler
    public void blockDestroyEvent(BlockDestroyEvent event) {
        onBlockDestroy(event.getBlock(), event);
    }
    @EventHandler
    public void blockExplodeEvent(BlockExplodeEvent event) {
        onBlockDestroy(event.getBlock(), event);
    }
    @EventHandler
    public void blockBurnEvent(BlockBurnEvent event) {
        onBlockDestroy(event.getBlock(), event);
    }
    @EventHandler
    public void entityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            for (Team team : teams.values()) {
                Location delivery = team.getArena().getDelivery();
                if (block.getLocation().equals(delivery)) {
                    return true;
                }
            }
            return false;
        });
    }
    public void onBlockDestroy(Block block, Cancellable event) {
        for (Team team : teams.values()) {
            Location delivery = team.getArena().getDelivery();
            if (block.getLocation().equals(delivery)) {
                event.setCancelled(true);
                return;
            }
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
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemStack item = itemEntity.getItemStack();
        addScoreLore(item);
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        addScoreLore(event.getCurrentItem());
    }
    
    @EventHandler
    public void onBrew(BrewEvent event) {
        for (ItemStack item : event.getResults()) {
            addScoreLore(item);
        }
    }
    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        ItemStack[] contents = event.getInventory().getContents();
        if (contents.length == 0) {
            return;
        }
        ItemStack result = contents[0];
        //slot 0 has result, might be null or AIR
        //1-4 are crafting area for player inventory
        //1-9 for crafting table
        addScoreLore(result);
    }
    @EventHandler
    public void onPrepareResult(PrepareResultEvent event) {
        addScoreLore(event.getResult());
    }
    
    private static final List<InventoryType> STORAGE_TYPES = List.of(
            InventoryType.CHEST,
            InventoryType.DISPENSER,
            InventoryType.DROPPER,
            InventoryType.HOPPER,
            InventoryType.BARREL,
            InventoryType.SHULKER_BOX,
            InventoryType.ENDER_CHEST
    );
    
    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        if (!STORAGE_TYPES.contains(event.getInventory().getType())) {
            return;
        }
        for (ItemStack item : event.getInventory().getContents()) {
            addScoreLore(item);
        }
    }
    
    /**
     * If the given item has a score associated with its Material type in the config,
     * this method adds a line to the item's lore showing how many points it's worth.<br>
     * 
     * This is an idempotent operation, meaning running it on the same item twice will
     * result in only 1 score line being added to the lore. It marks items that have been
     * modified with a persistent data container boolean using {@link #HAS_SCORE_LORE}
     * as the namespaced key.
     * 
     * @param item the item to add the score to, if it exists
     */
    private void addScoreLore(@Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return;
        }
        Component scoreLore = getScoreLore(item.getType());
        if (scoreLore == null) {
            return;
        }
        item.editMeta(meta -> {
            if (meta.getPersistentDataContainer().has(HAS_SCORE_LORE, PersistentDataType.BOOLEAN)) {
                return;
            }
            List<Component> originalLore = meta.lore();
            if (originalLore == null) {
                meta.lore(Collections.singletonList(scoreLore));
            } else {
                List<Component> newLore = new ArrayList<>(originalLore);
                newLore.add(scoreLore);
                meta.lore(newLore);
            }
            meta.getPersistentDataContainer().set(HAS_SCORE_LORE, PersistentDataType.BOOLEAN, true);
        });
    }
    
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (state == null) {
            return;
        }
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onCloseInventory(event, participant);
    }
    
    /**
     * @param type the type to get the score lore of
     * @return the lore line describing how many points the given item type is worth.
     * null if the given type is not listed in the config.
     */
    private @Nullable Component getScoreLore(@Nullable Material type) {
        if (type == null) {
            return null;
        }
        Integer score = config.getMaterialScores().get(type);
        if (score == null) {
            return null;
        }
        return Component.empty()
                .append(Component.text("Price: "))
                .append(Component.text(score))
                .color(NamedTextColor.GOLD);
    }
    
    @Override
    public void updatePersonalScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    @Override
    public void updateTeamScore(Player participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("timer", Component.empty())
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }
    }
    
    public void messageAllParticipants(Component message) {
        Audience.audience(admins
        ).sendMessage(message);
        for (Participant participant : participants.values()) {
            participant.getPlayer().sendMessage(message);
        }
    }
}
