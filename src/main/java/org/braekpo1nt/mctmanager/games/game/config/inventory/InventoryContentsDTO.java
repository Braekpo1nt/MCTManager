package org.braekpo1nt.mctmanager.games.game.config.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @param contents a map of the inventory indexes to ItemStackDTOs, representing the contents of an inventory. Keys must not be negative. 
 */
public record InventoryContentsDTO(Map<Integer, ItemStackDTO> contents) {
    /**
     * @return a list containing the ItemStack values of the contents at their Integer key indexes, with all other indexes being null. The list will be of size of the maximum value of the contents map keyset. Returns null if contents is null.
     * @throws IndexOutOfBoundsException if the max index in the contents keyset is negative
     */
    public ItemStack[] toInventoryContents() {
        if (contents == null) {
            return null;
        }
        int maxIndex = contents.keySet().stream().max(Integer::compareTo).orElse(0);
        ItemStack[] result = new ItemStack[maxIndex + 1];
        for (int i = 0; i <= maxIndex; i++) {
            ItemStackDTO itemStackDTO = contents.get(i);
            if (itemStackDTO != null) {
                result[i] = itemStackDTO.toItemStack();
            }
        }
        return result;
    }
}
