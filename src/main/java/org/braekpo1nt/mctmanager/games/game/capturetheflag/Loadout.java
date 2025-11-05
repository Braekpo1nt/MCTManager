package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    
    public Loadout(Component name, ItemStack menuItem, @NotNull ItemStack[] contents) {
        this.name = name;
        this.inventory = contents;
        this.menuItem = menuItem;
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
     * @return The name of this loadout. Used as the item display name for its menu item, and in chat messages to the
     * participants.
     */
    public Component getName() {
        return name;
    }
}
