package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Handles the class picking for a given team
 */
public class ClassPicker implements Listener {
    
    public static final Component TITLE = Component.empty()
            .append(Component.text("Pick a Class")
                    .color(NamedTextColor.DARK_GRAY))
            .append(Component.text(" (One per team)")
                    .color(NamedTextColor.GRAY));
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    private final Map<UUID, BattleClass> pickedBattleClasses = new HashMap<>();
    private final List<Player> teamMates = new ArrayList<>();
    private boolean classPickingActive = false;
    
    /**
     * Converts a {@link Material} to a {@link BattleClass}
     * @param material The material to get the associated battle class of. 
     * @return The battle class associated with the given material. Null if the given material is not one of the associated material.
     */
    private BattleClass materialToBattleClass(@NotNull Material material) {
        switch (material) {
            case STONE_SWORD -> {
                return BattleClass.KNIGHT;
            }
            case BOW -> {
                return BattleClass.ARCHER;
            }
            case IRON_SWORD -> {
                return BattleClass.ASSASSIN;
            }
            case LEATHER_CHESTPLATE -> {
                return BattleClass.TANK;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Gets the pretty name of the battle class
     * @param battleClass The battle class to get the name of
     * @return The name of the battle class. Null if the battle class is not one with a name (shouldn't ever return null)
     */
    private @NotNull String getBattleClassName(@NotNull BattleClass battleClass) {
        switch (battleClass) {
            case KNIGHT -> {
                return "Knight";
            }
            case ARCHER -> {
                return "Archer";
            }
            case ASSASSIN -> {
                return "Assassin";
            }
            case TANK -> {
                return "Tank";
            }
            default -> {
                return "";
            }
        }
    }
    
    /**
     * Registers event listeners, and starts the class picking phase for the given list of teammates
     * @param plugin The plugin
     * @param newTeamMates The list of teammates. They are assumed to be on the same team. Weird things will happen if they are not. 
     */
    public void start(Main plugin, List<Player> newTeamMates) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        teamMates.clear();
        teamMates.addAll(newTeamMates);
        classPickingActive = true;
        for (Player teamMate : teamMates) {
            teamMate.sendMessage(Component.text("Choose your class"));
            showClassPickerGui(teamMate);
        }
    }
    
    /**
     * Unregisters event listeners, assigns battle classes to teammates who don't have them (if desired), and closes inventories
     * @param assignBattleClasses If true, this will assign battle classes to teammates who haven't picked a battle class yet.
     */
    public void stop(boolean assignBattleClasses) {
        HandlerList.unregisterAll(this);
        classPickingActive = false;
        if (assignBattleClasses) {
            assignBattleClassesToTeamMatesWithoutBattleClasses();
        }
        for (Player teamMate : teamMates) {
            teamMate.closeInventory();
        }
    }
    
    public void addTeamMate(Player newTeamMate) {
        if (!classPickingActive) {
            return;
        }
        teamMates.add(newTeamMate);
        newTeamMate.sendMessage(Component.text("Choose your class"));
        showClassPickerGui(newTeamMate);
    }
    
    public void removeTeamMate(Player teamMate) {
        if (!classPickingActive) {
            return;
        }
        if (!teamMates.contains(teamMate)) {
            return;
        }
        if (pickedBattleClasses.containsKey(teamMate.getUniqueId())) {
            unAssignClass(teamMate);
            teamMate.getInventory().clear();
        }
        teamMates.remove(teamMate);
    }
    
    @EventHandler
    public void clickClassPickerInventory(InventoryClickEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.getClickedInventory() == null ||
                !event.getView().title().equals(TITLE) ||
                event.getCurrentItem() == null
        ) {
            return;
        }
        Player teamMate = ((Player) event.getWhoClicked());
        if (!teamMates.contains(teamMate)) {
            return;
        }
        event.setCancelled(true);
        Material itemType = event.getCurrentItem().getType();
        BattleClass battleClass = this.materialToBattleClass(itemType);
        if (battleClass == null) {
            return;
        }
        boolean playerSelectedBattleClass = selectBattleClass(teamMate, battleClass);
        if (!playerSelectedBattleClass) {
            return;
        }
        teamMate.closeInventory();
    }
    
    @EventHandler
    public void onCloseMenu(InventoryCloseEvent event) {
        if (!isActive()) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        Player teamMate = ((Player) event.getPlayer());
        if (!teamMates.contains(teamMate)) {
            return;
        }
        if (pickedBattleClasses.containsKey(teamMate.getUniqueId())) {
            return;
        }
        giveNetherStar(teamMate);
        teamMate.sendMessage(Component.text("You didn't pick a class. Use the nether star to pick.").color(NamedTextColor.DARK_RED));
    }
    
    @EventHandler
    public void clickNetherStar(InventoryClickEvent event) {
        if (!isActive()) {
            return;
        }
        ItemStack netherStar = event.getCurrentItem();
        if (netherStar == null ||
                !netherStar.getType().equals(Material.NETHER_STAR)) {
            return;
        }
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
            return;
        }
        Player teamMate = ((Player) event.getWhoClicked());
        if (!teamMates.contains(teamMate)) {
            return;
        }
        event.setCancelled(true);
        onClickNetherStar(teamMate, netherStar);
    }
    
    @EventHandler
    public void interactWithNetherStar(PlayerInteractEvent event) {
        if (!isActive()) {
            return;
        }
        Player teamMate = event.getPlayer();
        if (!teamMates.contains(teamMate)) {
            return;
        }
        ItemStack netherStar = event.getItem();
        if (netherStar == null || 
                !netherStar.getType().equals(Material.NETHER_STAR)) {
            return;
        }
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
            return;
        }
        event.setCancelled(true);
        onClickNetherStar(teamMate, netherStar);
    }
    
    private boolean selectBattleClass(@NotNull Player participant, @NotNull BattleClass battleClass) {
        if (pickedBattleClasses.containsValue(battleClass)) {
            participant.sendMessage(Component.empty()
                            .append(Component.text("Someone on your team already selected "))
                            .append(Component.text(getBattleClassName(battleClass)))
                            .color(NamedTextColor.DARK_RED));
            return false;
        }
        assignClass(participant, battleClass);
        return true;
    }
    
    private void giveNetherStar(@NotNull Player teamMate) {
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        netherStarMeta.displayName(NETHER_STAR_NAME);
        netherStar.setItemMeta(netherStarMeta);
        teamMate.getInventory().addItem(netherStar);
    }
    
    private void onClickNetherStar(@NotNull Player teamMate, @NotNull ItemStack netherStar) {
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null ||
                !netherStarMeta.hasDisplayName() ||
                !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)
        ) {
            return;
        }
        teamMate.getInventory().remove(netherStar);
        showClassPickerGui(teamMate);
    }
    
    /**
     *  Assigns a class to any teamMates that don't already have one. It will assign classes from the pool of unpicked classes for that team.
     */
    private void assignBattleClassesToTeamMatesWithoutBattleClasses() {
        for (Player teamMate : teamMates) {
            if (!pickedBattleClasses.containsKey(teamMate.getUniqueId())) {
                randomlyAssignClass(teamMate);
            }
        }
    }
    
    /**
     * Assign the given class to the given teamMate. Setting their inventory
     * and armor to the appropriate items. 
     * @param teamMate the teamMate to assign a class to
     * @param battleClass the class to assign
     */
    private void assignClass(Player teamMate, BattleClass battleClass) {
        pickedBattleClasses.put(teamMate.getUniqueId(), battleClass);
        teamMate.getInventory().clear();
        switch (battleClass) {
            case KNIGHT -> {
                teamMate.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                teamMate.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                teamMate.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                teamMate.sendMessage("Selected Knight");
            }
            case ARCHER -> {
                teamMate.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                teamMate.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                teamMate.getInventory().addItem(new ItemStack(Material.BOW));
                teamMate.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                teamMate.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
                teamMate.sendMessage("Selected Archer");
            }
            case ASSASSIN -> {
                teamMate.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                teamMate.sendMessage("Selected Assassin");
            }
            case TANK -> {
                teamMate.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                teamMate.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                teamMate.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                teamMate.sendMessage("Selected Tank");
            }
        }
        teamMate.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 8));
    }
    
    private void unAssignClass(Player teamMate) {
        pickedBattleClasses.remove(teamMate.getUniqueId());
        teamMate.getInventory().clear();
        giveNetherStar(teamMate);
        teamMate.sendMessage("Deselected class.");
    }
    
    /**
     * Randomly assign a class to the given participant from the pool of classes
     * that are left (classes that other players on their team didn't pick).
     * If there are no classes left unpicked to assign, this method does nothing.
     * @param participant the participant to assign a class to
     * @throws NullPointerException if the participant is not in the game state
     */
    private void randomlyAssignClass(Player participant) {
        for (BattleClass battleClass : BattleClass.values()) {
            if (!pickedBattleClasses.containsValue(battleClass)) {
                assignClass(participant, battleClass);
                return;
            }
        }
    }
    
    /**
     * Shows the given teamMate the Class Picker gui
     * @param teamMate The teamMate to show the gui to
     */
    private void showClassPickerGui(Player teamMate) {
        ItemStack knight = new ItemStack(Material.STONE_SWORD);
        ItemStack archer = new ItemStack(Material.BOW);
        ItemStack assassin = new ItemStack(Material.IRON_SWORD);
        ItemStack tank = new ItemStack(Material.LEATHER_CHESTPLATE);
        
        ItemMeta knightMeta = knight.getItemMeta();
        knightMeta.displayName(Component.text("Knight"));
        knightMeta.lore(Arrays.asList(
                Component.text("- Stone Sword"),
                Component.text("- Chest Plate"),
                Component.text("- Boots")
        ));
        knight.setItemMeta(knightMeta);
        
        ItemMeta archerMeta = archer.getItemMeta();
        archerMeta.displayName(Component.text("Archer"));
        archerMeta.lore(Arrays.asList(
                Component.text("- Bow"),
                Component.text("- 16 Arrows"),
                Component.text("- Wooden Sword"),
                Component.text("- Chest Plate"),
                Component.text("- Boots")
        ));
        archer.setItemMeta(archerMeta);
        
        ItemMeta assassinMeta = assassin.getItemMeta();
        assassinMeta.displayName(Component.text("Assassin"));
        assassinMeta.lore(Arrays.asList(
                Component.text("- Iron Sword"),
                Component.text("- No Armor")
        ));
        assassin.setItemMeta(assassinMeta);
        
        ItemMeta tankMeta = tank.getItemMeta();
        tankMeta.displayName(Component.text("Tank"));
        tankMeta.lore(Arrays.asList(
                Component.text("Comes with:"),
                Component.text("- Full Leather Armor"),
                Component.text("- Chainmail Leggings"),
                Component.text("- No Sword")
        ));
        tank.setItemMeta(tankMeta);
        
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        newGui.setItem(getSlotIndex(1, 1), knight);
        newGui.setItem(getSlotIndex(1, 2), archer);
        newGui.setItem(getSlotIndex(1, 3), assassin);
        newGui.setItem(getSlotIndex(1, 4), tank);
        teamMate.openInventory(newGui);
    }
    
    private int getSlotIndex(int line, int column) {
        return (line - 1) * 9 + (column - 1);
    }
    
    public boolean isActive() {
        return classPickingActive;
    }
    
    // Test methods
    
    /**
     * Returns a copy of the list of the teamMates in this class picker
     * @return A copy of the teamMates list
     */
    public @NotNull List<Player> getTeamMates() {
        return new ArrayList<>(teamMates);
    }
    
}
