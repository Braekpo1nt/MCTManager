package org.braekpo1nt.mctmanager.config.dto.inventory;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@Getter
public class InventoryDTO implements Validatable {
    
    /**
     * a map of the inventory indexes to ItemStackDTOs, representing the contents of an inventory. Keys must not be negative. Keys should not be greater than or equal to the size of the inventory they are destined for.
     */
    protected @Nullable Map<@Nullable Integer, @Nullable ItemStackDTO> contents;
    
    /**
     * @return a list containing the ItemStack values of the contents at their Integer key indexes, with all other indexes being null. The list will be of size of the maximum value of the contents map keyset. Returns null if contents is null.
     * @throws IndexOutOfBoundsException if the max index in the contents keyset is negative
     */
    public @NotNull ItemStack[] toInventoryContents() {
        if (contents == null) {
            return null;
        }
        int maxIndex = contents.keySet().stream().filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
        ItemStack[] result = new ItemStack[maxIndex + 1];
        for (int i = 0; i <= maxIndex; i++) {
            ItemStackDTO itemStackDTO = contents.get(i);
            if (itemStackDTO != null) {
                result[i] = itemStackDTO.toItemStack();
            }
        }
        return result;
    }
    
    @Override
    public void validate(Validator validator) {
        validator.notNull(contents, "contents");
    }
    
    /**
     * @deprecated in favor of {@link Validatable}
     */
    @Deprecated
    public void isValid() {
        Preconditions.checkArgument(contents != null, "contents can't be null");
    }
}
