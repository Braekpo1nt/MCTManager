package org.braekpo1nt.mctmanager.games.game.config.inventory;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.games.game.config.enchantments.EnchantmentDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStackDTO {
    /**
     * The type of the item
     */
    private @Nullable Material type;
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
     * The enchantments on the item. Each NamespacedKey represents an enchantment
     * @see org.bukkit.enchantments.Enchantment#getByKey(NamespacedKey) 
     */
    private @Nullable List<@Nullable EnchantmentDTO> enchantments;
    
    /**
     * @return the ItemStack object which was represented by this DTO 
     */
    public @NotNull ItemStack toItemStack() {
        Preconditions.checkArgument(type != null, "type (Material) cannot be null");
        ItemStack stack = new ItemStack(type, amount);
        if (itemMeta != null) {
            stack.editMeta(meta -> itemMeta.toItemMeta(meta, type));
        }
        if (enchantments != null) {
            stack.addUnsafeEnchantments(EnchantmentDTO.toEnchantments(enchantments));
        }
        return stack;
    }
    
    public void isValid() {
        Preconditions.checkArgument(type != null, "type can't be null");
        if (itemMeta != null) {
            itemMeta.isValid();
        }
        if (enchantments != null) {
            for (int i = 0; i < enchantments.size(); i++) {
                EnchantmentDTO enchantment = enchantments.get(i);
                Preconditions.checkArgument(enchantment != null, "enchantments[%s] can't be null", i);
                enchantment.isValid();
                Enchantment trueEnchantment = enchantment.toEnchantment();
                Preconditions.checkArgument(trueEnchantment != null, "could not find enchantment for %s", enchantment.getNamespacedKey());
                Preconditions.checkArgument(trueEnchantment.canEnchantItem(new ItemStack(type)), "enchantment %s is not applicable to item of type %s", enchantment.getNamespacedKey(), type);
            }
        }
    }
    
}
