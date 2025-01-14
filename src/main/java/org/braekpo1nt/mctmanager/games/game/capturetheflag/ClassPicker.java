package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
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
    private final Map<UUID, String> pickedBattleClasses = new HashMap<>();
    private final List<Player> teamMates = new ArrayList<>();
    private final GameManager gameManager;
    private boolean classPickingActive = false;
    private Map<String, Loadout> loadouts = new HashMap<>();
    private Map<Material, String> materialToBattleClass = new HashMap<>();
    
    public ClassPicker(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    /**
     * Registers event listeners, and starts the class picking phase for the given list of teammates
     * 
     * @param plugin The plugin
     * @param newTeamMates The list of teammates. They are assumed to be on the same team. Weird things will happen if they are not.
     * @param loadouts the loadouts for each BattleClass
     */
    public void start(Main plugin, Collection<Player> newTeamMates, 
                      Map<String, Loadout> loadouts) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.materialToBattleClass = new HashMap<>();
        for (Map.Entry<String, Loadout> entry : loadouts.entrySet()) {
            this.materialToBattleClass.put(entry.getValue().getMenuItem().getType(), entry.getKey());
        }
        this.loadouts = loadouts;
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
    public void onClickInventory(InventoryClickEvent event) {
        if (!classPickingActive) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        Player teamMate = ((Player) event.getWhoClicked());
        if (!teamMates.contains(teamMate)) {
            return;
        }
        if (event.getView().title().equals(TITLE)) {
            event.setCancelled(true);
            onClickClassPickerInventory(teamMate, clickedItem);
            return;
        }
        Material clickedType = clickedItem.getType();
        if (clickedType.equals(Material.NETHER_STAR)) {
            ItemMeta netherStarMeta = clickedItem.getItemMeta();
            if (netherStarMeta != null 
                    && netherStarMeta.hasDisplayName() 
                    && Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
                event.setCancelled(true);
                onClickNetherStar(teamMate, clickedItem);
            }
        }
        // don't let them remove their armor
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }
    
    /**
     * To be called when the given teamMate clicks an item in the class picker inventory
     * @param teamMate the player who clicked
     * @param clickedItem the item they clicked on
     */
    public void onClickClassPickerInventory(Player teamMate, ItemStack clickedItem) {
        Material itemType = clickedItem.getType();
        String battleClass = materialToBattleClass.get(itemType);
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
        if (!classPickingActive) {
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
    public void interactWithNetherStar(PlayerInteractEvent event) {
        if (!classPickingActive) {
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
    
    private boolean selectBattleClass(@NotNull Player participant, @NotNull String battleClass) {
        if (pickedBattleClasses.containsValue(battleClass)) {
            Loadout loadout = loadouts.get(battleClass);
            participant.sendMessage(Component.empty()
                            .append(Component.text("Someone on your team already selected "))
                            .append(loadout.getName())
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
    
    /**
     * To be called when a given teamMate clicks on, drops, or otherwise interacts with a nether star item
     * @param teamMate the player who used the nether star
     * @param netherStar the nether star item
     */
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
    private void assignClass(Player teamMate, @NotNull String battleClass) {
        pickedBattleClasses.put(teamMate.getUniqueId(), battleClass);
        teamMate.getInventory().clear();
        Loadout loadout = loadouts.get(battleClass);
        ItemStack[] contents = loadout.getContents();
        teamMate.getInventory().setContents(contents);
        GameManagerUtils.colorLeatherArmor(gameManager, teamMate);
        teamMate.sendMessage(Component.text("Selected ")
                .append(loadout.getName()));
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
        for (String battleClass : loadouts.keySet()) {
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
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        int column = 1;
        for (String battleClass : loadouts.keySet()) {
            ItemStack menuItem = loadouts.get(battleClass).getMenuItem();
            newGui.setItem(getSlotIndex(1, column), menuItem);
            column++;
        }
        teamMate.openInventory(newGui);
    }
    
    private int getSlotIndex(int line, int column) {
        return (line - 1) * 9 + (column - 1);
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
