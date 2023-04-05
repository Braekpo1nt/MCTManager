package org.braekpo1nt.mctmanager.games.capturetheflag;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClassPickerManager implements Listener {
    
    private final Inventory gui;
    
    public ClassPickerManager(Main plugin) {
        this.gui = createGui();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        Inventory clickedInventory = event.getClickedInventory();
        if (!clickedInventory.equals(gui)) {
            return;
        }
        Player player = ((Player) event.getWhoClicked());
        if (event.getCurrentItem() == null) {
            return;
        }
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case STONE_SWORD:
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                player.sendMessage("Selected Knight");
                break;
            case BOW:
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().addItem(new ItemStack(Material.BOW));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
                player.sendMessage("Selected Archer");
                break;
            case IRON_SWORD:
                player.getInventory().clear();
                player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                player.sendMessage("Selected Assassin");
                break;
            case LEATHER_CHESTPLATE:
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.sendMessage("Selected Tank");
                break;
            default:
                return;
        }
        clickedInventory.close();
        
        event.setCancelled(true);
    }
    
    private Inventory createGui() {
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
        
        ItemStack[] menuItems = {knight, archer, assassin, tank};
        Inventory newGui = Bukkit.createInventory(null, 9, Component.text(ChatColor.BOLD+"Select a Class"));
        newGui.setContents(menuItems);
        return newGui;
    }
    
}
