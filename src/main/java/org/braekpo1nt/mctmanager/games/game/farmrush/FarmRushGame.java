package org.braekpo1nt.mctmanager.games.game.farmrush;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfigController;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.FarmRushState;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
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
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
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
    
    @SuppressWarnings("SpellCheckingInspection")
    public static final NamespacedKey HAS_SCORE_LORE = NamespacedKey.minecraft("hasscorelore");
    private static final List<InventoryType> STORAGE_TYPES = List.of(
            InventoryType.CHEST,
            InventoryType.DISPENSER,
            InventoryType.DROPPER,
            InventoryType.HOPPER,
            InventoryType.BARREL,
            InventoryType.SHULKER_BOX,
            InventoryType.ENDER_CHEST
    );
    
    private @Nullable FarmRushState state;
    
    private final Main plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;
    private final Component baseTitle = Component.empty()
            .append(Component.text("Farm Rush"))
            .color(NamedTextColor.BLUE);
    private final PowerupManager powerupManager = new PowerupManager(this);
    private Component title = baseTitle;
    private FarmRushConfig config;
    private final FarmRushConfigController configController;
    private final List<Player> admins = new ArrayList<>();
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final Map<UUID, Participant> participants = new HashMap<>();
    // TODO: Teams right now, we don't remove teams whose participants have left. Instead, we leave them in this list with no participants. When the team rejoins, we just use this existing one. Evaluate whether this is the best action for this task.
    private final Map<String, FarmRushTeam> teams = new HashMap<>();
    private @Nullable ItemStack materialBook;
    
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
    public void start(Collection<Team> newTeams, Collection<Participant> newParticipants, List<Player> newAdmins) {
        sidebar = gameManager.createSidebar();
        adminSidebar = gameManager.createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        Set<String> teamIds = Team.getTeamIds(newTeams);
        Map<String, Arena> arenas = createArenas(teamIds);
        materialBook = createMaterialBook();
        addRecipes();
        int arenaOrder = 0;
        for (Team team : newTeams) {
            Arena arena = arenas.get(team.getTeamId());
            FarmRushTeam farmRushTeam = new FarmRushTeam(team, arena, arenaOrder);
            arenaOrder++;
            teams.put(farmRushTeam.getTeamId(), farmRushTeam);
            Main.logger().info(String.format("FarmRush.start(): Team %s size is %d", farmRushTeam.getTeamId(), farmRushTeam.getMemberUUIDs().size()));
        }
        placeArenas(arenas.values());
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        startAdmins(newAdmins);
        initializeSidebar();
        setupTeamOptions();
        state = new DescriptionState(this);
        Main.logger().info("Starting Farm Rush game");
    }
    
    private @Nullable ItemStack createMaterialBook() {
        if (config.isDoNotGiveBookDebug()) {
            return null;
        }
        ItemStack materialBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta.BookMetaBuilder builder = ((BookMeta) materialBook.getItemMeta()).toBuilder();
        BookMeta bookMeta = builder
                .title(Component.text("Item Values"))
                .author(Component.text("Farm Rush"))
                .pages(createPages(config.getMaterialScores(), gameManager.matchProgressPointMultiplier()))
                .build();
        materialBook.setItemMeta(bookMeta);
        return materialBook;
    }
    
    public static List<Component> createPages(Map<Material, ItemSale> materialScores, double multiplier) {
        List<TextComponent> lines = createLines(materialScores, multiplier);
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> pages = new ArrayList<>(lines.size()/15);
        for (int i = 0; i < lines.size(); i += 10) {
            TextComponent.Builder builder = Component.text();
            int end = Math.min(lines.size(), i + 10);
            
            for (int j = i; j < end; j++) {
                TextComponent line = lines.get(j);
                double length = PlainTextComponentSerializer.plainText().serialize(line).length();
                int numberOfExtraLines = (int) Math.ceil(length / 21.0) - 1;
                j += numberOfExtraLines;
                builder.append(line);
                if (j < end - 1) {
                    builder.append(Component.newline());
                }
            }
            if (i+10 < lines.size()) {
                builder
                        .append(Component.newline())
                        .append(Component.text("..."));
            }
            pages.add(builder.build());
        }
        
        return pages;
    }
    
    public static @NotNull List<TextComponent> createLines(Map<Material, ItemSale> materialScores, double multiplier) {
        List<TextComponent> lines = new ArrayList<>();
        List<Map.Entry<Material, ItemSale>> entryList = materialScores.entrySet().stream().sorted((entry1, entry2) -> {
            int score1 = entry1.getValue().getScore();
            int score2 = entry2.getValue().getScore();
            if (score1 != score2) {
                return Integer.compare(score2, score1);
            }
            int requiredAmount1 = entry1.getValue().getRequiredAmount();
            int requiredAmount2 = entry2.getValue().getRequiredAmount();
            if (requiredAmount1 != requiredAmount2) {
                return Integer.compare(requiredAmount1, requiredAmount2);
            }
            return entry1.getKey().compareTo(entry2.getKey());
        }).toList();
        
        for (Map.Entry<Material, ItemSale> entry : entryList) {
            Material material = entry.getKey();
            ItemSale itemSale = entry.getValue();
            Component itemName = Component.translatable(material.translationKey());
            TextComponent.Builder line = Component.text();
            if (itemSale.getRequiredAmount() > 1) {
                line
                        .append(Component.text(itemSale.getRequiredAmount()))
                        .append(Component.space());
            }
            line
                    .append(itemName)
                    .append(Component.text(": "))
                    .append(Component.text((int) (itemSale.getScore() * multiplier))
                            .color(NamedTextColor.GOLD));
            lines.add(line.build());
        }
        return lines;
    }
    
    /**
     * Actually place the schematic file of the arenas and add any necessary additions,
     * such as the barrel for delivery
     * @param arenas the arenas to place copies of the schematic file on
     */
    public void placeArenas(@NotNull Collection<Arena> arenas) {
        if (config.shouldBuildArenas()) {
            File schematicFile = new File(plugin.getDataFolder(), config.getArenaFile());
            List<Vector> origins = arenas.stream().map(arena -> arena.getBounds().getMin()).toList();
            BlockPlacementUtils.placeSchematic(config.getWorld(), origins, schematicFile);
        }
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
            Chest starterChestState = (Chest) starterChest.getState();
            Inventory starterChestInventory = starterChestState.getBlockInventory();
            starterChestInventory.setContents(config.getStarterChestContents());
            if (materialBook != null) {
                starterChestInventory.addItem(materialBook);
            }
            ItemStack cropGrowerRecipeMap = config.getCropGrowerSpec().getRecipeMap();
            if (cropGrowerRecipeMap != null) {
                starterChestInventory.addItem(cropGrowerRecipeMap);
            }
            ItemStack animalGrowerRecipeMap = config.getAnimalGrowerSpec().getRecipeMap();
            if (animalGrowerRecipeMap != null) {
                starterChestInventory.addItem(animalGrowerRecipeMap);
            }
            starterChestInventory.addItem(config.getCropGrowerSpec().getCropGrowerItem());
            starterChestInventory.addItem(config.getAnimalGrowerSpec().getAnimalGrowerItem());
            arena.closeBarnDoor();
        }
    }
    
    /**
     * Fill the space that the arenas were placed with air
     * and removes all leftover items and non-player entities
     * @param arenas the arenas to remove
     */
    private void removeArenas(@NotNull List<Arena> arenas) {
        if (config.shouldClearArenas()) {
            List<BoundingBox> boxes = arenas.stream().map(Arena::getBounds).toList();
            for (Entity entity : config.getWorld().getEntities()) {
                if (!(entity instanceof Player)) {
                    for (BoundingBox box : boxes) {
                        if (box.contains(entity.getLocation().toVector())) {
                            entity.remove();
                        }
                    }
                }
            }
            BlockPlacementUtils.fillWithAir(config.getWorld(), boxes);
        }
    }
    
    private @NotNull Map<String, Arena> createArenas(Set<String> teamIds) {
        Map<String, Arena> arenas = new HashMap<>(teamIds.size());
        Arena arena = config.getFirstArena();
        Vector offset = new Vector(arena.getBounds().getWidthX() + 1, 0, 0);
        int i = 0;
        for (String teamId : teamIds) {
            arenas.put(teamId, arena);
            if (i < teamIds.size() - 1) {
                arena = arena.offset(offset);
            }
            i++;
        }
        return arenas;
    }
    
    /**
     * @return the team who is the last arena in the physical lineup of arenas
     */
    public @Nullable FarmRushTeam getLastTeamInLine() {
        return teams.values().stream()
                .max(Comparator.comparingInt(FarmRushTeam::getArenaOrder))
                .orElse(null);
    }
    
    public void initializeParticipant(Participant participant) {
        FarmRushTeam team = teams.get(participant.getTeamId());
        team.addParticipant(participant);
        participants.put(participant.getUniqueId(), participant);
        participant.setGameMode(GameMode.ADVENTURE);
        sidebar.addPlayer(participant);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        participant.getInventory().setContents(config.getLoadout());
        if (materialBook != null) {
            participant.getInventory().addItem(materialBook);
        }
        participant.teleport(team.getArena().getSpawn());
        participant.setRespawnLocation(team.getArena().getSpawn(), true);
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
        removeRecipes();
        for (Participant participant : participants.values()) {
            resetParticipant(participant);
        }
        clearSidebar();
        removeArenas(teams.values().stream().map(FarmRushTeam::getArena).toList());
        teams.clear();
        participants.clear();
        TopCommand.setEnabled(false);
        gameManager.gameIsOver();
        powerupManager.stop();
        Main.logger().info("Stopping Farm Rush game");
    }
    
    public void resetParticipant(Participant participant) {
        FarmRushTeam team = teams.get(participant.getTeamId());
        team.removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        sidebar.removePlayer(participant);
        participant.setGameMode(GameMode.SPECTATOR);
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
    public void onTeamJoin(Team team) {
        if (state == null) {
            return;
        }
        state.onTeamJoin(team);
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        if (state == null) {
            return;
        }
        state.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(Participant player) {
        if (state == null) {
            return;
        }
        Participant participant = participants.get(player.getUniqueId());
        if (participant == null) {
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
    public void onPortalCreate(PortalCreateEvent event) {
        List<BlockState> blocks = event.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }
        if (!blocks.getFirst().getWorld().equals(config.getWorld())) {
            return;
        }
        event.setCancelled(true);
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
            participant.teleport(arena.getSpawn());
            return;
        }
        if (!arena.getBounds().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
        if (state != null) {
            state.onPlayerMove(event, participant);
        }
    }
    
    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        Location delivery = teams.get(participant.getTeamId()).getArena().getDelivery();
        Block block = event.getBlock();
        if (block.getLocation().equals(delivery)) {
            event.setCancelled(true);
            return;
        }
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        powerupManager.onBlockBreak(block, event);
    }
    @EventHandler
    public void blockExplodeEvent(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            for (FarmRushTeam team : teams.values()) {
                Location delivery = team.getArena().getDelivery();
                if (block.getLocation().equals(delivery)) {
                    return true;
                }
            }
            return false;
        });
        List<Block> powerupBlocks = powerupManager.onBlocksBreak(event.blockList());
        event.blockList().removeAll(powerupBlocks);
    }
    @EventHandler
    public void entityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            for (FarmRushTeam team : teams.values()) {
                Location delivery = team.getArena().getDelivery();
                if (block.getLocation().equals(delivery)) {
                    return true;
                }
            }
            return false;
        });
        List<Block> powerupBlocks = powerupManager.onBlocksBreak(event.blockList());
        event.blockList().removeAll(powerupBlocks);
    }
    @EventHandler
    public void blockDestroyEvent(BlockDestroyEvent event) {
        onBlockDestroy(event.getBlock(), event);
    }
    @EventHandler
    public void blockBurnEvent(BlockBurnEvent event) {
        onBlockDestroy(event.getBlock(), event);
    }
    public void onBlockDestroy(Block block, Cancellable event) {
        if (state == null) {
            return;
        }
        for (FarmRushTeam team : teams.values()) {
            Location delivery = team.getArena().getDelivery();
            if (block.getLocation().equals(delivery)) {
                event.setCancelled(true);
                return;
            }
        }
        powerupManager.onBlockBreak(block, event);
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
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (state == null) {
            return;
        }
        if (!participants.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        state.onPlayerOpenInventory(event);
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
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state == null) {
            return;
        }
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Participant participant = participants.get(player.getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantDamage(event);
    }
    
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (state == null) {
            return;
        }
        Participant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onPlaceBlock(event, participant);
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
        ItemSale itemSale = config.getMaterialScores().get(type);
        if (itemSale == null) {
            return null;
        }
        return Component.empty()
                .append(Component.text("Price: "))
                .append(Component.text((int) (itemSale.getScore() * gameManager.matchProgressPointMultiplier())))
                .color(NamedTextColor.GOLD);
    }
    
    /**
     * Add the configured recipes to the server
     */
    private void addRecipes() {
        for (Recipe recipe : config.getRecipes()) {
            plugin.getServer().addRecipe(recipe);
        }
        plugin.getServer().updateRecipes();
    }
    
    /**
     * Remove the configured recipes from the server
     */
    private void removeRecipes() {
        for (NamespacedKey recipeKey : config.getRecipeKeys()) {
            plugin.getServer().removeRecipe(recipeKey);
        }
        plugin.getServer().updateRecipes();
    }
    
    @Override
    public void updatePersonalScore(Participant participant, Component contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.containsKey(participant.getUniqueId())) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    @Override
    public void updateTeamScore(Participant participant, Component contents) {
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
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).sendMessage(message);
    }
    
    public void titleAllParticipants(Title title) {
        Audience.audience(
                Audience.audience(admins),
                Audience.audience(participants.values())
        ).showTitle(title);
    }
}
