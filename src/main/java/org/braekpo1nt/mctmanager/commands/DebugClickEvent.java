package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class DebugClickEvent implements Listener {
    
    private final Inventory gui;
    
    public DebugClickEvent(Main plugin, Inventory gui) {
        this.gui = gui;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void clickEvent(InventoryClickEvent e) {
        if (!e.getClickedInventory().equals(gui)) {
            return;
        }
        
        e.setCancelled(true);
    }
    
}
