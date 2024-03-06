package org.braekpo1nt.mctmanager.games.game.config.inventory;

import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackDTO {
    private Material type;
    private int amount;
    private ItemMetaDTO itemMeta;
    
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(type, amount);
        if (itemMeta != null) {
            ItemMeta meta = stack.getItemMeta();
            stack.setItemMeta(itemMeta.toItemMeta(meta));
        }
        return stack;
    }
}
