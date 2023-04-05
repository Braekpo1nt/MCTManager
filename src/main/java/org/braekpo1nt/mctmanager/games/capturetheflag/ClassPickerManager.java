package org.braekpo1nt.mctmanager.games.capturetheflag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ClassPickerManager implements Listener {
    
    public static final Component TITLE = Component.empty()
            .append(Component.text("Pick a Class")
                    .color(NamedTextColor.DARK_GRAY))
            .append(Component.text(" (One per team)")
                    .color(NamedTextColor.GRAY));
    private final Map<String, List<BattleClass>> classTracker = new HashMap<>();
    private final List<UUID> participantsWhoPickedClasses = new ArrayList<>();
    private final GameManager gameManager;
    
    public ClassPickerManager(Main plugin, GameManager gameManager) {
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (!classTracker.containsKey(teamName)) {
            classTracker.put(teamName, new ArrayList<>());
        }
        event.setCancelled(true);
        List<BattleClass> teamClasses = classTracker.get(teamName);
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case STONE_SWORD:
                if (teamClasses.contains(BattleClass.KNIGHT)) {
                    participant.sendMessage(Component.text("Someone on your team already selected Knight").color(NamedTextColor.RED));
                    return;
                }
                teamClasses.add(BattleClass.KNIGHT);
                assignClass(participant, BattleClass.KNIGHT);
                break;
            case BOW:
                if (teamClasses.contains(BattleClass.ARCHER)) {
                    participant.sendMessage(Component.text("Someone on your team already selected Archer").color(NamedTextColor.RED));
                    return;
                }
                teamClasses.add(BattleClass.ARCHER);
                assignClass(participant, BattleClass.ARCHER);
                break;
            case IRON_SWORD:
                if (teamClasses.contains(BattleClass.ASSASSIN)) {
                    participant.sendMessage(Component.text("Someone on your team already selected Assassin").color(NamedTextColor.RED));
                    return;
                }
                teamClasses.add(BattleClass.ASSASSIN);
                assignClass(participant, BattleClass.ASSASSIN);
                break;
            case LEATHER_CHESTPLATE:
                if (teamClasses.contains(BattleClass.TANK)) {
                    participant.sendMessage(Component.text("Someone on your team already selected Tank").color(NamedTextColor.RED));
                    return;
                }
                teamClasses.add(BattleClass.TANK);
                assignClass(participant, BattleClass.TANK);
                break;
            default:
                return;
        }
        participantsWhoPickedClasses.add(participant.getUniqueId());
        participant.closeInventory();
    }
    
    /**
     * Assign the given class to the given participant. Setting their inventory
     * and armor to the appropriate items. 
     * @param participant the participant to assign a class to
     * @param battleClass the class to assign
     */
    public void assignClass(Player participant, BattleClass battleClass) {
        switch (battleClass) {
            case KNIGHT -> {
                participant.getInventory().clear();
                participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                participant.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                participant.sendMessage("Selected Knight");
            }
            case ARCHER -> {
                participant.getInventory().clear();
                participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                participant.getInventory().addItem(new ItemStack(Material.BOW));
                participant.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                participant.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
                participant.sendMessage("Selected Archer");
            }
            case ASSASSIN -> {
                participant.getInventory().clear();
                participant.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                participant.sendMessage("Selected Assassin");
            }
            case TANK -> {
                participant.getInventory().clear();
                participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                participant.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                participant.sendMessage("Selected Tank");
            }
        }
    }
    
    
    
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        Player participant = ((Player) event.getPlayer());
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        if (participantsWhoPickedClasses.contains(participant.getUniqueId())) {
            return;
        }
        participant.sendMessage(Component.text("You didn't pick a class. Your class will be randomly selected.").color(NamedTextColor.RED));
    }
    
    public void resetClassPickerTracker() {
        this.classTracker.clear();
        this.participantsWhoPickedClasses.clear();
    }
    
    /**
     * Shows the given participant the Class Picker gui
     * @param participant The participant to show the gui to
     */
    public void showClassPickerGui(Player participant) {
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
                Component.text("- No Sword")
        ));
        tank.setItemMeta(tankMeta);
        
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        newGui.setItem(getSlotIndex(1, 1), knight);
        newGui.setItem(getSlotIndex(1, 2), archer);
        newGui.setItem(getSlotIndex(1, 3), assassin);
        newGui.setItem(getSlotIndex(1, 4), tank);
        participant.openInventory(newGui);
    }
    
    public static int getSlotIndex(int line, int column) {
        int slotIndex = (line - 1) * 9 + (column - 1);
        return slotIndex;
    }
    
}
