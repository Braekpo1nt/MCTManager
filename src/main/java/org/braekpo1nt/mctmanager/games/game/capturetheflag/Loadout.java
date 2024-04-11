package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Loadout {
    /**
     * The name of this loadout (used in chat messages to communicate to the player which loadout they chose)
     */
    private final Component name;
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
     * @param name The name of this loadout (used in chat messages to communicate to the player which loadout they chose)
     * @param menuItem the menuItem to be displayed in the class picker menu. Use display name and lore to tell the user about it. The amount will be set to 1. The dislpayName of the item will be set to the given name.
     * @param inventory The inventory contents of this loadout.
     */
    public Loadout(Component name, ItemStack menuItem, ItemStack[] inventory) {
        this.name = name;
        this.menuItem = menuItem.clone();
        this.menuItem.setAmount(1);
        this.menuItem.editMeta(meta -> meta.displayName(this.name));
        this.inventory = inventory;
    }
    
    /**
     * 
     * @param name The name of this loadout. Used as the item display name for its menu item, and in chat messages to the participants. 
     * @param menuMaterial The Material types to be used in the menu to represent this loadout
     * @param menuDescription The lore to be added to the menu item's meta to be used as a description for each loadout
     * @param contents The inventory contents of this loadout
     */
    public Loadout(Component name, Material menuMaterial, List<Component> menuDescription, @NotNull ItemStack[] contents) {
        this.inventory = contents;
        this.menuItem = new ItemStack(menuMaterial);
        this.name = name;
        ItemMeta menuItemMeta = this.menuItem.getItemMeta();
        menuItemMeta.displayName(name);
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
    
    /**
     * @return The name of this loadout. Used as the item display name for its menu item, and in chat messages to the participants.
     */
    public Component getName() {
        return name;
    }
}
