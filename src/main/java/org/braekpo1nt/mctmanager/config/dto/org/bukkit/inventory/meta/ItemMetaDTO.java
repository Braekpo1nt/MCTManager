package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public interface ItemMetaDTO extends Validatable {
    ItemMeta toItemMeta(ItemMeta meta, Material type);
    
    
    /**
     * @deprecated in favor of {@link Validatable}
     */
    @Deprecated
    void isValid();
}
