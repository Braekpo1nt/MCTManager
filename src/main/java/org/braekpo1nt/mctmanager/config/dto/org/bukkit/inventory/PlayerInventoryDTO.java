package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory;

import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlayerInventoryDTO extends InventoryDTO {
    
    private static final int MAX_SIZE = 41;
    
    @Override
    public void validate(@NotNull Validator validator) {
        super.validate(validator);
        validator.notNull(contents, "contents");
        validator.validate(contents.size() <= MAX_SIZE, "contents can't contain more than %s entries", MAX_SIZE);
        for (Map.Entry<Integer, ItemStackDTO> entry : contents.entrySet()) {
            int slot = entry.getKey();
            validator.validate(0 <= slot && slot <= MAX_SIZE - 1, "contents[%d] must be between 0 and %d (inclusive)", slot, MAX_SIZE - 1);
            ItemStackDTO item = entry.getValue();
            if (item != null) {
                item.validate(validator.path("contents[%s]", slot));
            }
        }
    }
    
}
