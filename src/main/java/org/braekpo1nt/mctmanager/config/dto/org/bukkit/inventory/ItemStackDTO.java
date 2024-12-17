package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.enchantments.EnchantmentDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class ItemStackDTO implements Validatable {
    /**
     * The type of the item
     * TODO: make sure that it makes sense for id to be a possible type here. Look at Recipe format. misode.github.io/recipe/. Same for amount below
     */
    @SerializedName(value = "type", alternate = {"id"})
    private @Nullable Material type;
    /**
     * the amount of the item in the stack 
     * (values of 0 or less are treated as zero, resulting in no items in the stack)
     */
    @SerializedName(value = "amount", alternate = {"count"})
    private int amount = 1;
    /**
     * The ItemMeta of the item, can be null
     */
    private @Nullable ItemMetaDTO itemMeta;
    /**
     * The enchantments on the item. Each NamespacedKey represents an enchantment
     */
    private @Nullable List<@Nullable EnchantmentDTO> enchantments;
    
    /**
     * @return the ItemStack object which was represented by this DTO 
     */
    public @NotNull ItemStack toItemStack() {
        Preconditions.checkArgument(type != null, "type (Material) cannot be null");
        Preconditions.checkArgument(amount > 0, "amount must be greater than 0");
        ItemStack stack = ItemStack.of(type, amount);
        if (itemMeta != null) {
            stack.editMeta(meta -> itemMeta.toItemMeta(meta, type));
        }
        if (enchantments != null) {
            stack.addUnsafeEnchantments(EnchantmentDTO.toEnchantments(enchantments));
        }
        return stack;
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(type, "type");
        validator.validate(amount > 0, "amount must be greater than 0");
        if (itemMeta != null) {
            itemMeta.validate(validator.path("itemMeta"));
        }
        if (enchantments != null) {
            for (int i = 0; i < enchantments.size(); i++) {
                EnchantmentDTO enchantment = enchantments.get(i);
                validator.notNull(enchantment, "enchantments[%d]", i);
                enchantment.validate(validator.path("enchantments[%d]", i));
                Enchantment trueEnchantment = enchantment.toEnchantment();
                validator.validate(trueEnchantment != null, "enchantments[%d]: could not find enchantment for \"%s\"", i, enchantment.getNamespacedKey());
                validator.validate(trueEnchantment.canEnchantItem(new ItemStack(type)), "enchantments[%d]: enchantment %s is not applicable to item of type %s", i, enchantment.getNamespacedKey(), type);
            }
        }
    }
    
}
