package org.braekpo1nt.mctmanager.games.game.farmrush;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.WandsGameBase;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.FarmRushState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.InitialState;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class FarmRushGame extends WandsGameBase<FarmRushParticipant, FarmRushTeam, FarmRushParticipant.QuitData, FarmRushTeam.QuitData, FarmRushState> {
    
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
    
    private final PowerupManager powerupManager = new PowerupManager(this);
    private final FarmRushConfig config;
    private final @NotNull List<Arena> arenas;
    
    public FarmRushGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull FarmRushConfig config,
            @NotNull String configFile,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(new GameInstanceId(GameType.FARM_RUSH, configFile), plugin, gameManager, title, new InitialState());
        this.config = config;
        this.arenas = new ArrayList<>(newTeams.size());
        this.saleGuiItems = createGuiItems();
        addRecipes();
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        addWand(Wand.<FarmRushParticipant>builder()
                .wandItem(Wand.createWandItem(Material.BOOK, "Info", List.of(
                        Component.text("Item sale prices")
                )))
                .onRightClick((event, participant) -> {
                    state.showMaterialGui(participant);
                    return CommandResult.success();
                })
                .build());
        start(newTeams, newParticipants, newAdmins);
        placeArenas(arenas);
        Main.logger().info("Starting Farm Rush game");
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull FarmRushState getStartState() {
        return new DescriptionState(this);
    }
    
    public void showMaterialGui(FarmRushParticipant participant) {
        int rows = 6;
        ChestGui gui = new ChestGui(rows, "Sale Prices");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        
        PaginatedPane pages = new PaginatedPane(0, 0, 9, rows - 1);
        pages.populateWithGuiItems(saleGuiItems);
        gui.addPane(pages);
        
        OutlinePane background = background(rows - 1);
        gui.addPane(background);
        
        StaticPane navigation = new StaticPane(0, rows - 1, 9, 1);
        navigation.addItem(new GuiItem(withName(Material.RED_DYE, "Previous"), event -> {
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                gui.update();
            }
        }), 0, 0);
        navigation.addItem(new GuiItem(withName(Material.LIME_DYE, "Next"), event -> {
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        }), 8, 0);
        navigation.addItem(new GuiItem(withName(Material.BARRIER, "Close"), event ->
                event.getWhoClicked().closeInventory()), 4, 0);
        gui.addPane(navigation);
        
        gui.show(participant.getPlayer());
    }
    
    /**
     * Convenience method for creating items for the gui
     * @param material the material type of the item
     * @param name the custom name of the item
     * @return the item with the appropriate details
     */
    private static ItemStack withName(Material material, String name) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> {
            meta.customName(Component.text(name));
        });
        return itemStack;
    }
    
    /**
     * @param y the y position of the pane
     * @return a background pane with black stained-glass panes and low priority
     */
    private static @NotNull OutlinePane background(int y) {
        OutlinePane background = new OutlinePane(0, y, 9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);
        return background;
    }
    
    private final @NotNull List<GuiItem> saleGuiItems;
    
    private List<GuiItem> createGuiItems() {
        List<ItemSale> itemSales = config.getMaterialScores().values().stream()
                .sorted((a, b) -> {
                    int scoreCompare = Integer.compare(b.getScore(), a.getScore());
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    
                    int amountCompare = Integer.compare(a.getRequiredAmount(), b.getRequiredAmount());
                    if (amountCompare != 0) {
                        return scoreCompare;
                    }
                    
                    return a.getMaterial().compareTo(b.getMaterial());
                })
                .toList();
        List<GuiItem> items = new ArrayList<>(itemSales.size());
        for (ItemSale itemSale : itemSales) {
            ItemStack itemStack = createGuiItem(itemSale);
            items.add(new GuiItem(itemStack));
        }
        return items;
    }
    
    private @NotNull ItemStack createGuiItem(ItemSale itemSale) {
        ItemStack itemStack = new ItemStack(itemSale.getMaterial(), itemSale.getRequiredAmount());
        itemStack.editMeta(meta -> {
            Component scoreLore = itemSale.toScoreLore(gameManager.getMultiplier());
            List<Component> originalLore = meta.lore();
            if (originalLore == null) {
                meta.lore(Collections.singletonList(scoreLore));
            } else {
                List<Component> newLore = new ArrayList<>(originalLore);
                newLore.add(scoreLore);
                meta.lore(newLore);
            }
            meta.getPersistentDataContainer().set(FarmRushGame.HAS_SCORE_LORE, PersistentDataType.BOOLEAN, true);
        });
        return itemStack;
    }
    
    /**
     * Actually place the schematic file of the arenas and add any necessary additions,
     * such as the barrel for delivery
     * @param arenas the arenas to place copies of the schematic file on
     */
    public void placeArenas(@NotNull Collection<Arena> arenas) {
        if (config.shouldBuildArenas()) {
            File schematicFile = new File(new File(plugin.getDataFolder(), getType().getId()), config.getArenaFile());
            List<Vector> schematicOrigins = arenas.stream().map(Arena::getSchematicOrigin).toList();
            BlockPlacementUtils.placeSchematic(config.getWorld(), schematicOrigins, schematicFile);
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
            starterChestInventory.addItem(getWandItems());
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
    
    /**
     * @return a new arena, offset from the last arena in line (last entry
     * in {@link #arenas}
     */
    private @NotNull Arena createArena() {
        if (arenas.isEmpty()) {
            Arena arena = config.getFirstArena();
            arenas.add(arena);
            return arena;
        }
        Arena lastInLine = arenas.getLast();
        Vector offset = new Vector(lastInLine.getBounds().getWidthX() + 1, 0, 0);
        Arena arena = lastInLine.offset(offset);
        arenas.add(arena);
        return arena;
    }
    
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getAdminLocation());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("timer", Component.empty())
        );
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("timer", Component.empty())
        );
    }
    
    @Override
    public void stop() {
        removeArenas(teams.values().stream().map(FarmRushTeam::getArena).toList());
        super.stop();
    }
    
    @Override
    protected void cleanup() {
        removeRecipes();
        powerupManager.stop();
    }
    
    @Override
    protected @NotNull FarmRushParticipant createParticipant(Participant participant) {
        return new FarmRushParticipant(participant, 0);
    }
    
    @Override
    protected @NotNull FarmRushParticipant createParticipant(Participant participant, FarmRushParticipant.QuitData quitData) {
        participant.getInventory().setContents(quitData.getInventory());
        return new FarmRushParticipant(participant, quitData);
    }
    
    @Override
    protected @NotNull FarmRushParticipant.QuitData getQuitData(FarmRushParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(FarmRushParticipant participant, FarmRushTeam team) {
        participant.getInventory().setContents(config.getLoadout());
        participant.getInventory().addItem(getWandItems());
        participant.teleport(team.getArena().getSpawn());
    }
    
    @Override
    protected void initializeTeam(FarmRushTeam team) {
        
    }
    
    @Override
    protected @NotNull FarmRushTeam createTeam(Team team) {
        Arena arena = createArena();
        return new FarmRushTeam(team, arena, 0, 0);
    }
    
    @Override
    protected @NotNull FarmRushTeam createTeam(Team team, FarmRushTeam.QuitData quitData) {
        return new FarmRushTeam(team, quitData);
    }
    
    @Override
    protected @NotNull FarmRushTeam.QuitData getQuitData(FarmRushTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(FarmRushParticipant participant, FarmRushTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull FarmRushTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
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
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        // intentionally null, participants should not be in spectator during the game
        return null;
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
        for (FarmRushTeam team : teams.values()) {
            Location delivery = team.getArena().getDelivery();
            if (block.getLocation().equals(delivery)) {
                event.setCancelled(true);
                return;
            }
        }
        powerupManager.onBlockBreak(block, event);
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
    
    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        FarmRushParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantOpenInventory(event, participant);
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
     * <p>
     * This is an idempotent operation, meaning running it on the same item twice will
     * result in only 1 score line being added to the lore. It marks items that have been
     * modified with a persistent data container boolean using {@link #HAS_SCORE_LORE}
     * as the namespaced key.
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
        return itemSale.toScoreLore(gameManager.getMultiplier());
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        FarmRushParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantCloseInventory(event, participant);
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        FarmRushParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantPlaceBlock(event, participant);
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
}
