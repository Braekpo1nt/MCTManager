package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.persistence.PersistentDataHolderDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The interface which is implemented by all ItemMeta DTOs for the purposes of json serialization
 */
public interface ItemMetaDTO extends Validatable, PersistentDataHolderDTO {
    /**
     * Sets the fields of the given ItemMeta to the fields contained in this DTO
     * @param meta the ItemMeta to assign the values to
     * @param type the type of Material this ItemMeta belongs to
     * @return the modified ItemMeta
     */
    ItemMeta toItemMeta(ItemMeta meta, Material type);
    
    @Nullable Component displayName();
    
    void displayName(final @Nullable Component displayName);
    
    boolean hasDisplayName();
    
    @Nullable List<Component> lore();
    
    void lore(final @Nullable List<Component> lore);
    
    boolean hasLore();
    
    /**
     * @deprecated because paper is deprecating {@link ItemMeta#hasCustomModelData()}
     */
    @Deprecated
    boolean hasCustomModelData();
    
    boolean isUnbreakable();
    
    void setUnbreakable(boolean unbreakable);
    
    /**
     * @deprecated because paper is deprecating {@link ItemMeta#getCustomModelData()}
     */
    @Deprecated
    int getCustomModelData();
    
    /**
     * @deprecated because paper is deprecating {@link ItemMeta#setCustomModelData(Integer)}
     */
    @Deprecated
    void setCustomModelData(@Nullable Integer customModelData);
    
}
