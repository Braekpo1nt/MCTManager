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
     * The menu item to represent this loadout
     */
    private final ItemStack menuItem;
    /**
     * The name of this loadout (used in chat messages to communicate to the player which loadout they chose)
     */
    private final Component name;
    /**
     * the inventory contents for this loadout
     */
    private final ItemStack[] inventory;
    
    /**
     * 
     * @param name The name of this loadout (used in chat messages to communicate to the player which loadout they chose)
     * @param menuItem the menuItem to be displayed in the class picker menu. Use display name and lore to tell the user about it. Only 1 item will be in the item stack. If an item with an amount not equal to 1 is given, then a clone will be assigned with an amount of 1.
     * @param inventory The inventory contents of this loadout.
     */
    public Loadout(Component name, ItemStack menuItem, ItemStack[] inventory) {
        Preconditions.checkArgument(menuItem.getAmount() == 1, "only 1 item can be in the menuItem. Found %s", menuItem.getAmount());
        if (menuItem.getAmount() == 1) {
            this.menuItem = menuItem;
        } else {
            this.menuItem = menuItem.clone();
            this.menuItem.setAmount(1);
        }
        this.name = name;
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
