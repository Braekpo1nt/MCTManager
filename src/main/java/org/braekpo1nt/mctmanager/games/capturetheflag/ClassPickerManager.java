package org.braekpo1nt.mctmanager.games.capturetheflag;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ClassPickerManager implements Listener {
    
    public static final Component TITLE = Component.text("Choose your Class");
    private final Map<String, List<BattleClass>> classTracker = new HashMap<>();
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
        Player player = ((Player) event.getWhoClicked());
        if (!gameManager.isParticipant(player.getUniqueId())) {
            return;
        }
        String teamName = gameManager.getTeamName(player.getUniqueId());
        if (!classTracker.containsKey(teamName)) {
            classTracker.put(teamName, new ArrayList<>());
        }
        event.setCancelled(true);
        List<BattleClass> teamClasses = classTracker.get(teamName);
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case STONE_SWORD:
                if (teamClasses.contains(BattleClass.KNIGHT)) {
                    player.sendMessage(Component.text("Someone on your team already selected Knight"));
                    return;
                }
                teamClasses.add(BattleClass.KNIGHT);
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                player.sendMessage("Selected Knight");
                break;
            case BOW:
                if (teamClasses.contains(BattleClass.ARCHER)) {
                    player.sendMessage(Component.text("Someone on your team already selected Archer"));
                    return;
                }
                teamClasses.add(BattleClass.ARCHER);
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().addItem(new ItemStack(Material.BOW));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
                player.sendMessage("Selected Archer");
                break;
            case IRON_SWORD:
                if (teamClasses.contains(BattleClass.ASSASSIN)) {
                    player.sendMessage(Component.text("Someone on your team already selected Assassin"));
                    return;
                }
                teamClasses.add(BattleClass.ASSASSIN);
                player.getInventory().clear();
                player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                player.sendMessage("Selected Assassin");
                break;
            case LEATHER_CHESTPLATE:
                if (teamClasses.contains(BattleClass.TANK)) {
                    player.sendMessage(Component.text("Someone on your team already selected Tank"));
                    return;
                }
                teamClasses.add(BattleClass.TANK);
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.sendMessage("Selected Tank");
                break;
            default:
                return;
        }
        player.closeInventory();
    }
    
    public void resetClassPickerTracker() {
        this.classTracker.clear();
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
