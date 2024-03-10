package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Loadout {
    /**
     * The menu item to represent this loadout
     */
    private final ItemStack menuItem;
    /**
     * the inventory contents for this loadout
     */
    private final ItemStack[] inventory;
    
    /**
     * 
     * @param menuName The name to display for this menu item
     * @param menuMaterial The Material types to be used in the menu to represent this loadout
     * @param menuDescription The lore to be added to the menu item's meta to be used as a description for each loadout
     * @param contents The inventory contents of this loadout
     */
    public Loadout(String menuName, Material menuMaterial, List<Component> menuDescription, ItemStack[] contents) {
        this.inventory = contents;
        this.menuItem = new ItemStack(menuMaterial);
        ItemMeta menuItemMeta = this.menuItem.getItemMeta();
        menuItemMeta.displayName(Component.text(menuName));
        menuItemMeta.lore(menuDescription);
        this.menuItem.setItemMeta(menuItemMeta);
    }
    
    /**
     * @return The menu item to represent this loadout
     */
    public ItemStack getMenuItem() {
        return menuItem;
    }
    
    /**
     * @return the inventory contents for this loadout
     */
    public ItemStack[] getContents() {
        return inventory;
    }
}
