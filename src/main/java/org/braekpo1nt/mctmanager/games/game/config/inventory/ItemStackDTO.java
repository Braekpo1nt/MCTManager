package org.braekpo1nt.mctmanager.games.game.config.inventory;

import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class ItemStackDTO {
    /**
     * The type of the item
     */
    private Material type;
    /**
     * the amount of the item in the stack 
     * (values of 0 or less are treated as zero, resulting in no items in the stack)
     */
    private int amount = 1;
    /**
     * The ItemMeta of the item, can be null
     */
    private @Nullable ItemMetaDTO itemMeta;
    
    /**
     * @return the ItemStack object which was represented by this DTO 
     */
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(type, amount);
        if (itemMeta != null) {
            ItemMeta meta = stack.getItemMeta();
            stack.setItemMeta(itemMeta.toItemMeta(meta));
        }
        return stack;
    }
}
