package org.braekpo1nt.mctmanager.games.game.farmrush;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.PowerupManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.FarmRushState;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.InitialState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
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
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class FarmRushGame extends GameBase<FarmRushParticipant, FarmRushTeam, FarmRushParticipant.QuitData, FarmRushTeam.QuitData, FarmRushState> {
    
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
    private @Nullable final ItemStack materialBook;
    private final FarmRushConfig config;
    private final @NotNull List<Arena> arenas;
    
    
    public FarmRushGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull FarmRushConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(GameType.FARM_RUSH, plugin, gameManager, title, new InitialState());
        this.config = config;
        this.materialBook = createMaterialBook();
        this.arenas = new ArrayList<>(newTeams.size());
        addRecipes();
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
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
    
    private @Nullable ItemStack createMaterialBook() {
        if (config.isDoNotGiveBookDebug()) {
            return null;
        }
        ItemStack materialBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta.BookMetaBuilder builder = ((BookMeta) materialBook.getItemMeta()).toBuilder();
        BookMeta bookMeta = builder
                .title(Component.text("Item Values"))
                .author(Component.text("Farm Rush"))
                .pages(createPages(config.getMaterialScores(), gameManager.getMultiplier()))
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
    protected void cleanup() {
        removeRecipes();
        removeArenas(teams.values().stream().map(FarmRushTeam::getArena).toList());
        TopCommand.setEnabled(false);
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
        if (materialBook != null) {
            participant.getInventory().addItem(materialBook);
        }
        participant.teleport(team.getArena().getSpawn());
    }
    
    @Override
    protected void initializeTeam(FarmRushTeam team) {
        
    }
    
    @Override
    protected @NotNull FarmRushTeam createTeam(Team team) {
        Arena arena = createArena();
        return new FarmRushTeam(team, arena, 0);
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
                .append(Component.text((int) (itemSale.getScore() * gameManager.getMultiplier())))
                .color(NamedTextColor.GOLD);
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
