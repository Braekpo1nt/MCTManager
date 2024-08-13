package org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.persistence.PersistentDataContainerDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ItemMetaDTOImpl implements ItemMetaDTO, Validatable {
    /**
     * A JsonElement to be converted to a Component
     */
    protected @Nullable Component displayName;
    /**
     * A list of JsonElements to be converted to a list of Components
     */
    protected @Nullable List<@Nullable Component> lore;
    protected @Nullable Set<ItemFlag> itemFlags;
    protected boolean unbreakable;
    protected @Nullable Map<Attribute, List<AttributeModifier>> attributeModifiers;
    protected @Nullable Integer customModelData;
    protected @Nullable PersistentDataContainerDTO persistentDataContainer;
    
    @Override
    public Component displayName() {
        return displayName;
    }
    
    @Override
    public void displayName(final @Nullable Component displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public boolean hasDisplayName() {
        return this.displayName != null;
    }
    
    @Override
    public List<Component> lore() {
        return lore;
    }
    
    @Override
    public void lore(@Nullable List<Component> lore) {
        this.lore = lore;
    }
    
    @Override
    public boolean hasLore() {
        return this.lore != null;
    }
    
    @Override
    public boolean hasCustomModelData() {
        return this.customModelData != null;
    }
    
    /**
     * Check if this ItemMetaDTOImpl has customModelData first with the {@link ItemMetaDTOImpl#hasCustomModelData()}
     * @return the customModelData, if this ItemMetaDTOImpl has one
     * @throws IllegalStateException if {@link ItemMetaDTOImpl#customModelData} is null
     */
    public int getCustomModelData() {
        if (this.customModelData == null) {
            throw new IllegalStateException("customModelData is null (this ItemMetaDTO does not have customModelData)");
        }
        return this.customModelData;
    }
    
    @Override
    public @Nullable PersistentDataContainerDTO getPersistentDataContainerDTO() {
        return persistentDataContainer;
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        // nothing to validate
    }
    
    /**
     * Imbues the provided ItemMeta with the attributes of this ItemMetaDTO
     * @param meta the ItemMeta to be modified
     * @return the provided ItemMeta, after all the attributes have been set 
     * to this ItemMetaDTO's attributes
     */
    public ItemMeta toItemMeta(ItemMeta meta, Material type) {
        if (displayName != null) {
            meta.displayName(displayName);
        }
        if (lore != null) {
            meta.lore(lore);
        }
        if (itemFlags != null) {
            meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        }
        meta.setUnbreakable(unbreakable);
        if (attributeModifiers != null) {
            for (Map.Entry<Attribute, List<AttributeModifier>> entry : attributeModifiers.entrySet()) {
                for (AttributeModifier modifier : entry.getValue()) {
                    meta.addAttributeModifier(entry.getKey(), modifier);
                }
            }
        }
        if (persistentDataContainer != null) {
            persistentDataContainer.toPersistentDataContainer(meta.getPersistentDataContainer());
        }
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
        return meta;
    }
    
}
