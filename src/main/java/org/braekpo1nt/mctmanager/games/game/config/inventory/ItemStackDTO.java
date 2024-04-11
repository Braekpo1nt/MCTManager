package org.braekpo1nt.mctmanager.games.game.config.inventory;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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
    public @NotNull ItemStack toItemStack() {
        Preconditions.checkArgument(type != null, "type (Material) cannot be null");
        ItemStack stack = new ItemStack(type, amount);
        if (itemMeta != null) {
            stack.editMeta(meta -> itemMeta.toItemMeta(meta, type));
        }
        return stack;
    }
    
    public void isValid() {
        Preconditions.checkArgument(type != null, "type can't be null");
        if (itemMeta != null) {
            itemMeta.isValid();
        }
    }
    
    public Material getType() {
        return type;
    }
}
