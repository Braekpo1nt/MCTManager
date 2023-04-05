package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DebugClickEvent implements Listener {
    
    private final Inventory gui;
    
    public DebugClickEvent(Main plugin, Inventory gui) {
        this.gui = gui;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void clickEvent(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }
        Inventory clickedInventory = e.getClickedInventory();
        if (!clickedInventory.equals(gui)) {
            return;
        }
        Player player = ((Player) e.getWhoClicked());
        if (e.getCurrentItem() == null) {
            return;
        }
        Material clickedItem = e.getCurrentItem().getType();
        switch (clickedItem) {
            case BREAD:
                player.setFoodLevel(Math.min(player.getFoodLevel()+1, 20));
                player.sendMessage("Yum");
                break;
            case DIAMOND_SWORD:
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                player.sendMessage("Don't slice yourself");
                break;
        }
        clickedInventory.close();
        
        e.setCancelled(true);
    }
    
}
